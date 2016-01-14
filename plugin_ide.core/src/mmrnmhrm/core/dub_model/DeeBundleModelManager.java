/*******************************************************************************
 * Copyright (c) 2013 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package mmrnmhrm.core.dub_model;

import static melnorme.lang.ide.core.operations.ILangOperationsListener_Default.ProcessStartKind.ENGINE_TOOLS;
import static melnorme.lang.ide.core.utils.TextMessageUtils.headerBIG;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import static melnorme.utilbox.core.CoreUtil.array;

import java.text.MessageFormat;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import dtool.dub.DubBundle;
import dtool.dub.DubBundle.DubBundleException;
import dtool.dub.DubBundleDescription;
import dtool.dub.DubHelper;
import dtool.dub.DubManifestParser;
import dtool.engine.compiler_installs.CompilerInstall;
import dtool.engine.compiler_installs.SearchCompilersOnPathOperation;
import melnorme.lang.ide.core.BundleInfo;
import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.AbstractToolManager;
import melnorme.lang.ide.core.operations.ILangOperationsListener_Default.IOperationConsoleHandler;
import melnorme.lang.ide.core.project_model.BundleModelManager;
import melnorme.lang.ide.core.project_model.LangBundleModel;
import melnorme.lang.ide.core.utils.EclipseUtils;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.ide.core.utils.operation.CoreOperationRunnable;
import melnorme.lang.ide.core.utils.operation.EclipseAsynchJobAdapter;
import melnorme.lang.ide.core.utils.operation.EclipseAsynchJobAdapter.IRunnableWithJob;
import melnorme.lang.ide.core.utils.process.IRunProcessTask;
import melnorme.lang.tooling.BundlePath;
import melnorme.utilbox.concurrency.ITaskAgent;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;
import mmrnmhrm.core.DeeCoreMessages;
import mmrnmhrm.core.dub_model.DeeBundleModelManager.DeeBundleModel;
import mmrnmhrm.core.dub_model.DeeBundleModelManager.WorkspaceModelManagerTask;
import mmrnmhrm.core.engine.DeeToolManager;

/**
 * Updates a {@link DeeBundleModel} when resource changes occur, using 'dub describe'.
 * Also creates problem markers on the Eclipse workspace. 
 */
public class DeeBundleModelManager extends BundleModelManager<DeeBundleModel> {
	
	public static class DeeBundleModel extends LangBundleModel {
		
	}
	
	/* -----------------  ----------------- */
	
	public static final String DUB_PROBLEM_ID = LangCore.PLUGIN_ID + ".DubProblem";
	
	public DeeBundleModelManager() {
		super(new DeeBundleModel());
	}
	
	public DeeToolManager getProcessManager() {
		return (DeeToolManager) LangCore.getToolManager();
	}
	
	@Override
	protected ManagerResourceListener init_createResourceListener() {
		return new ManagerResourceListener();
	}
	
	@Override
	protected void bundleProjectAdded(IProject project) {
		handleBundleManifestChanged(project);
	}
	
	@Override
	protected BundleInfo createNewInfo(IProject project) {
		DubBundleDescription unresolvedDescription = readUnresolvedBundleDescription(project);
		/* XXX: Could it be a problem to run a possibly long-running operation here? */
		return createProjectInfo(unresolvedDescription);
	}
	
	protected void handleBundleManifestChanged(final IProject project) {
		BundleInfo unresolvedProjectInfo = createNewInfo(project);
		getModel().setProjectInfo(project, unresolvedProjectInfo); 
		
		modelAgent.submit(new ProjectModelDubDescribeTask(this, project, unresolvedProjectInfo));
	}
	
	protected DubBundleDescription readUnresolvedBundleDescription(final IProject project) {
		java.nio.file.Path location = project.getLocation().toFile().toPath();
		BundlePath bundlePath = BundlePath.create(location);
		DubBundle unresolvedBundle = DubManifestParser.parseDubBundleFromLocation2(bundlePath);
		if(unresolvedBundle == null) {
			// Can happen if using SDL format, which we don't know how to parse. Provide dummy bundle
			unresolvedBundle = new DubBundle(bundlePath, "(UNKNOW)", null, null, null, 
				null, null, null, null, null, null);
		}
		
		return new DubBundleDescription(unresolvedBundle);
	}
	
	protected final BundleInfo updateProjectInfo(IProject project, BundleInfo oldInfo, 
			DubBundleDescription dubBundleDescription) {
		return getModel().updateProjectInfo(project, oldInfo, createProjectInfo(dubBundleDescription));
	}
	
	/* ----------------------------------- */
	
	protected class SearchCompilersOnPathOperation_Eclipse extends SearchCompilersOnPathOperation {
		@Override
		protected void handleWarning(String message) {
			LangCore.logWarning(message);
		}
	}
	
	protected BundleInfo createProjectInfo(DubBundleDescription dubBundleDescription) {
		CompilerInstall compilerInstall = new SearchCompilersOnPathOperation_Eclipse().
				searchForCompilersInDefaultPathEnvVars().getPreferredInstall();
		
		return new BundleInfo(compilerInstall, dubBundleDescription);
	}
	
	public void syncPendingUpdates() {
		modelAgent.waitForPendingTasks();
	}
	
	/** WARNING: this API is intended to be used for tests only */
	public ITaskAgent internal_getModelAgent() {
		return modelAgent;
	}
	
	public static IMarker[] getDubErrorMarkers(IProject project) throws CoreException {
		return project.findMarkers(DUB_PROBLEM_ID, true, IResource.DEPTH_ONE);
	}
	
	protected abstract class WorkspaceModelManagerTask implements Runnable {
		
		protected final DeeBundleModelManager workspaceModelManager;
		
		public WorkspaceModelManagerTask() {
			this.workspaceModelManager = DeeBundleModelManager.this;
		}
		
		protected void logInternalError(CoreException ce) {
			LangCore.logInternalError(ce);
		}
		
	}
	
}


class ProjectModelDubDescribeTask extends ProjectUpdateBuildpathTask implements IRunnableWithJob {
	
	protected final IProject project;
	protected final BundleInfo unresolvedProjectInfo;
	protected final DubBundleDescription unresolvedDescription;
	
	protected ProjectModelDubDescribeTask(DeeBundleModelManager dubModelManager, IProject project, 
			BundleInfo unresolvedProjectInfo) {
		super(dubModelManager);
		this.project = project;
		this.unresolvedProjectInfo = unresolvedProjectInfo;
		unresolvedDescription = unresolvedProjectInfo.getBundleDesc();
	}
	
	protected DeeToolManager getProcessManager() {
		return workspaceModelManager.getProcessManager();
	}
	
	@Override
	public void run() {
		try {
			ResourceUtils.getWorkspace().run(new IWorkspaceRunnable() {
				
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					if(project.exists() == false) {
						return;
					}
					deleteDubMarkers(project);
					
					if(unresolvedDescription.hasErrors() == true) {
						DubBundleException error = unresolvedDescription.getError();
						setDubErrorMarker(project, error);
						return; // only run dub describe if unresolved description had no errors
					}
					
				}
			}, project, 0, null);
		} catch (CoreException ce) {
			logInternalError(ce);
		}
		
		// only run dub describe if unresolved description had no errors
		if(unresolvedDescription.hasErrors() == false) {
			try {
				EclipseAsynchJobAdapter.runUnderAsynchJob(getNameForJob(), this);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
	protected String getNameForJob() {
		return "Running 'dub describe' on project: " + project.getName();
	}
	
	protected void deleteDubMarkers(IProject project) throws CoreException {
		IMarker[] markers = DeeBundleModelManager.getDubErrorMarkers(project);
		for (IMarker marker : markers) {
			marker.delete();
		}
	}
	
	protected void setDubErrorMarker(IProject project, DubBundleException error) throws CoreException {
		setDubErrorMarker(project, error.getExtendedMessage());
	}
	
	protected void setDubErrorMarker(IProject project, String message) throws CoreException {
		IMarker dubMarker = project.createMarker(DeeBundleModelManager.DUB_PROBLEM_ID);
		dubMarker.setAttribute(IMarker.MESSAGE, message);
		dubMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	}
	
	@Override
	public void runUnderEclipseJob(IProgressMonitor monitor) {
		assertNotNull(monitor);
		try {
			new CoreOperationRunnable() {
				@Override
				public void doRun(IProgressMonitor pm) throws CommonException, CoreException, OperationCancellation {
					resolveProjectOperation(pm);
				}
			}.coreAdaptedRun(monitor);
		} catch(OperationCancellation ce) {
			return;
		} catch(CoreException ce) {
			try {
				EclipseUtils.getWorkspace().run(new IWorkspaceRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						if(project.exists() == false) {
							return;
						}
						setProjectDubError(project, ce);
					}
				}, null, 0, monitor);
				
			} catch (CoreException e) {
				logInternalError(ce);
			}
		}
	}
	
	protected AbstractToolManager getToolManager() {
		return LangCore.getToolManager();
	}
	
	protected Void resolveProjectOperation(IProgressMonitor pm) throws CoreException, CommonException {
		IPath projectLocation = project.getLocation();
		if(projectLocation == null) {
			return null; // Project no longer exists, or not stored in the local filesystem.
		}
		
		BundlePath bundlePath = BundlePath.create(projectLocation.toFile().toPath());
			
		String dubPath = getToolManager().getSDKToolPath(project).toString();
		
		IOperationConsoleHandler opHandler = getToolManager().startNewOperation(ENGINE_TOOLS, true, false);
		opHandler.writeInfoMessage(
			headerBIG(MessageFormat.format(DeeCoreMessages.RunningDubDescribe, project.getName())));
		
		// TODO: add --skip-registry to dub command
		ProcessBuilder pb = AbstractToolManager.createProcessBuilder(project, array(dubPath, "describe"));
		IRunProcessTask dubDescribeTask = getProcessManager().newRunProcessTask(opHandler, pb, pm);
		
		ExternalProcessResult processHelper;
		try {
			processHelper = getProcessManager().submitTaskAndAwaitResult(dubDescribeTask);
		} catch (OperationCancellation e) {
			throw LangCore.createCoreException("Error, `describe` cancelled.", null);
		}
		
		final DubBundleDescription bundleDesc = DubHelper.parseDubDescribe(bundlePath, processHelper);
		if(bundleDesc.hasErrors()) {
			throw LangCore.createCoreException("Error resolving bundle: ", bundleDesc.getError());
		}
		
		ResourceUtils.getWorkspace().run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				if(project.exists() == false) {
					return;
				}
				assertTrue(!bundleDesc.hasErrors());
				
				workspaceModelManager.updateProjectInfo(project, unresolvedProjectInfo, bundleDesc);
				project.refreshLocal(1, monitor);
			}
		}, null, 0, pm);
		
		return null;
	}
	
	protected void setProjectDubError(IProject project, CoreException ce) throws CoreException {
		
		DubBundleException dubError = new DubBundleException(ce.getMessage(), ce.getCause());
		
		DubBundle main = unresolvedDescription.getMainBundle();
		DubBundleDescription bundleDesc = new DubBundleDescription(main, dubError);
		BundleInfo newProjectInfo = new BundleInfo(unresolvedProjectInfo.getCompilerInstall(), bundleDesc);
		workspaceModelManager.getModel().setProjectInfo(project, newProjectInfo);
		
		setDubErrorMarker(project, dubError);
	}
	
}

abstract class ProjectUpdateBuildpathTask extends WorkspaceModelManagerTask {
	
	protected ProjectUpdateBuildpathTask(DeeBundleModelManager dubModelManager) {
		dubModelManager.super();
	}
	
}