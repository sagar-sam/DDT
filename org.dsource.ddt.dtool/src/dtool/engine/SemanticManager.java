/*******************************************************************************
 * Copyright (c) 2014, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.engine;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ExecutionException;

import melnorme.utilbox.concurrency.ITaskAgent;
import dtool.dub.BundlePath;
import dtool.dub.DubBundle;
import dtool.dub.DubBundleDescription;
import dtool.dub.DubBundleDescription.DubDescribeAnalysis;
import dtool.dub.DubHelper.RunDubDescribeCallable;
import dtool.dub.ResolvedManifest;
import dtool.engine.BundleResolution.CommonResolvedModule;
import dtool.engine.ModuleParseCache.ParseSourceException;
import dtool.engine.modules.NullModuleResolver;
import dtool.engine.util.FileCachingEntry;
import dtool.engine.util.CachingRegistry;
import dtool.parser.DeeParserResult.ParsedModule;

/**
 * A caching hierarchical registry.
 * This class could be generalized, but it would be a bit of a mess to handle all the parameterized types.
 */
abstract class AbstractSemanticManager {
	
	public AbstractSemanticManager() {
	}
	
	protected final CachingRegistry<BundlePath, BundleInfo> infos = new CachingRegistry<BundlePath, BundleInfo>() {
		@Override
		protected BundleInfo createEntry(BundlePath bundlePath) {
			return new BundleInfo(bundlePath);
		}
	};
	
	protected final Object entriesLock = new Object();
	
	/* -----------------  ----------------- */
	
	protected class BundleInfo {
		
		protected final BundlePath bundlePath;
		
		protected final FileCachingEntry<ResolvedManifest> manifestEntry;
		protected volatile BundleResolution bundleResolution;
		
		public BundleInfo(BundlePath bundlePath) {
			this.bundlePath = bundlePath;
			this.manifestEntry = new FileCachingEntry<>(bundlePath.path);
		}
		
		public ResolvedManifest getManifest() {
			return manifestEntry.getValue();
		}
		
		public BundleResolution getSemanticResolution() {
			return bundleResolution;
		}
		
		public boolean checkIsManifestStale() {
			if(manifestEntry.isStale()) {
				return true;
			}
			
			synchronized(entriesLock) {
				
				for(ResolvedManifest depBundle : getManifest().getBundleDeps()) {
					BundleInfo depBundleInfo = getInfo(depBundle.getBundlePath());
					if(depBundle != depBundleInfo.getManifest()) {
						// Dep manifest is not stale, but was updated in the meanwhile. 
						// Therefore parent is stale.
						return true; 
					}
					if(depBundleInfo.checkIsManifestStale()) {
						return true;
					}
				}
				return false;
			}
		}
		
		public boolean checkIsResolutionStale() {
			return
					getSemanticResolution() == null ||
					checkIsManifestStale() ||					
					getSemanticResolution().checkIsModuleListStaleInTree() ||
					getSemanticResolution().checkIsModuleContentsStaleInTree();
		}
		
	}
	
	protected BundleInfo getInfo(BundlePath bundlePath) {
		return infos.getEntry(bundlePath);
	}
	
	public ResolvedManifest getStoredManifest(BundlePath bundlePath) {
		BundleInfo info = infos.getEntryOrNull(bundlePath);
		return info != null ? info.getManifest() : null;
	}
	
	public boolean checkIsManifestStale(BundlePath bundlePath) {
		BundleInfo info = infos.getEntryOrNull(bundlePath);
		return info == null ? true : info.checkIsManifestStale();
	}
	
	public ResolvedManifest getUpdatedManifest(BundlePath bundlePath) throws ExecutionException {
		BundleInfo info = getInfo(bundlePath);
		if(info.checkIsManifestStale()) {
			return updateManifestEntry(bundlePath);
		}
		return info.getManifest();
	}
	
	protected final Object updateOperationLock = new Object();
	
	protected ResolvedManifest updateManifestEntry(BundlePath bundlePath) throws ExecutionException {
		synchronized(updateOperationLock) {
			// Recheck stale status after acquiring lock, it might have been updated in the meanwhile.
			// Otherwise unnecessary updates might occur after one other.
			BundleInfo info = getInfo(bundlePath);
			if(info.checkIsManifestStale() == false)
				return info.getManifest();
			
			return doUpdateManifest(bundlePath);
		}
	}
	
	protected abstract ResolvedManifest doUpdateManifest(BundlePath bundlePath) throws ExecutionException;
	
}

/**
 * Maintains a registry of parsed bundle manifests, indexed by bundle path.
 */
public class SemanticManager extends AbstractSemanticManager {
	
	protected final DToolServer dtoolServer;
	protected final ITaskAgent dubProcessAgent;
	protected final ModuleParseCache parseCache;
	
	public SemanticManager(DToolServer dtoolServer) {
		this.dtoolServer = dtoolServer;
		this.parseCache = new ModuleParseCache(dtoolServer);
		this.dubProcessAgent = dtoolServer.new DToolTaskAgent("DSE.DubProcessAgent");
	}
	
	public void shutdown() {
		dubProcessAgent.shutdownNow();
	}	
	
	
	@Override
	protected ResolvedManifest doUpdateManifest(BundlePath bundlePath) throws ExecutionException {
		RunDubDescribeCallable dubDescribeTask = new RunDubDescribeCallable(bundlePath, false);
		DubBundleDescription bundleDesc = dubDescribeTask.submitAndGet(dubProcessAgent);
		
		FileTime dubStartTimeStamp = dubDescribeTask.getStartTimeStamp();
		DubDescribeAnalysis dubDescribeAnalyzer = new DubDescribeAnalysis(bundleDesc);
		
		synchronized(entriesLock) {
			for(ResolvedManifest newManifestValue : dubDescribeAnalyzer.getAllManifests()) {
				dtoolServer.logMessage("Resolved new manifest for: " + newManifestValue.bundlePath + 
					"\n  timestamp: " + dubStartTimeStamp.toString());
				
				// We cap the maximum timestamp because DUB describe is only guaranteed to have read the 
				// manifest files up to dubStartTimeStamp
				getInfo(newManifestValue.bundlePath).manifestEntry.updateValue(newManifestValue, dubStartTimeStamp);
			}
		}
		return dubDescribeAnalyzer.mainManifest;
	}
	
	/* ----------------- Semantic Resolution and module list ----------------- */
	
	public BundleResolution getStoredResolution(BundlePath bundlePath) {
		BundleInfo info = infos.getEntryOrNull(bundlePath);
		return info != null ? info.getSemanticResolution() : null;
	}
	
	public boolean checkIsResolutionStale(BundlePath bundlePath) {
		BundleInfo info = infos.getEntryOrNull(bundlePath);
		return info == null ? true : info.checkIsResolutionStale();
	}
	
	public BundleResolution getUpdatedResolution(BundlePath bundlePath) throws ExecutionException {
		BundleInfo info = getInfo(bundlePath);
		if(info.checkIsResolutionStale()) {
			return updateSemanticResolutionEntry(info).getSemanticResolution();
		}
		return info.getSemanticResolution();
	}
	
	/* ----------------- file updates handling ----------------- */
	
	/*BUG here review, deduplicate */
	protected BundleInfo updateSemanticResolutionEntry(BundleInfo staleInfo) throws ExecutionException {
		BundlePath bundlePath = staleInfo.bundlePath;
		synchronized(updateOperationLock) {
			// Recheck stale status after acquiring lock, it might have been updated in the meanwhile.
			// Otherwise unnecessary update operatons might occur if two threads tried to update at the same time.
			if(staleInfo.checkIsResolutionStale() == false)
				return staleInfo; // No longer stale
			
			ResolvedManifest manifest = getUpdatedManifest(bundlePath);
			
			BundleResolution bundleRes = new BundleResolution(this, manifest);
			
			synchronized(entriesLock) {
				return setNewBundleResolution(bundleRes);
			}
		}
	}
	
	protected BundleInfo setNewBundleResolution(BundleResolution bundleRes) {
		for(BundleResolution newDepBundleRes : bundleRes.getDirectDependencies()) {
			setNewBundleResolution(newDepBundleRes);
		}
		BundleInfo newInfo = getInfo(bundleRes.getBundlePath());
		newInfo.bundleResolution = bundleRes;
		return newInfo;
	}
	
	protected DubBundle getBundle(BundlePath bundlePath) {
		return getStoredManifest(bundlePath).bundle;
	}
	
	public ParsedModule updateWorkingCopyAndParse(Path filePath, String source) {
		return parseCache.getParsedModule(filePath, source);
	}
	
	public void discardWorkingCopy(Path filePath) {
		parseCache.discardWorkingCopy(filePath);
	}
	
	public CommonResolvedModule getUpdatedResolvedModule(Path filePath) throws ExecutionException {
		BundlePath bundlePath = BundlePath.findBundleForPath(filePath);
		
		try {
			if(bundlePath == null) {
				ParsedModule parsedModule = parseCache.getParsedModule(filePath);
				return new CommonResolvedModule(parsedModule, new NullModuleResolver());
			} else {
				BundleResolution bundleRes = getUpdatedResolution(bundlePath);
				return bundleRes.getResolvedModule(filePath);
			}
		} catch (ParseSourceException e) {
			throw new ExecutionException(e);
		}
		
	}
	
}