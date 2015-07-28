/*******************************************************************************
 * Copyright (c) 2015, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package mmrnmhrm.core.build;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import melnorme.lang.ide.core.operations.OperationInfo;
import melnorme.lang.ide.core.operations.build.BuildManager;
import melnorme.lang.ide.core.operations.build.BuildOperationCreator;
import melnorme.lang.ide.core.operations.build.BuildTargetValidator;
import melnorme.lang.ide.core.operations.build.CommonBuildTargetOperation;
import melnorme.lang.ide.core.operations.build.IToolOperation;
import melnorme.lang.ide.core.project_model.AbstractBundleInfo;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.core.CommonException;
import mmrnmhrm.core.DeeCore;
import mmrnmhrm.core.dub_model.DeeBundleModelManager.DeeBundleModel;
import mmrnmhrm.core.dub_model.DubBundleInfo;

public class DeeBuildManager extends BuildManager {
	
	public static final String BuildType_Default = "<default>";
	
	public DeeBuildManager(DeeBundleModel bundleModel) {
		super(bundleModel);
	}
	
	@Override
	protected void bundleProjectAdded(IProject project, AbstractBundleInfo bundleInfo) {
		DubBundleInfo dubBundleInfo = (DubBundleInfo) bundleInfo;
		if(dubBundleInfo.getBundleDesc().isResolved()) {
			// We ignore resolved description, because only unresolved ones have configuration info
			return;
		}
		super.bundleProjectAdded(project, dubBundleInfo);
	}
	
	@Override
	protected Indexable<BuildType> getBuildTypes_do() {
		return ArrayList2.<BuildType>create(
			new DeeBuildType(BuildType_Default),
			new DeeBuildType(DubBuildType.UNITTEST.getBuildTypeString())
		);
	}
	
	@Override
	public IToolOperation newProjectBuildOperation(OperationInfo opInfo, IProject project, boolean fullBuild)
			throws CommonException {
		return new DeeBuildOperationCreator(project, opInfo, fullBuild).newProjectBuildOperation();
	}
	
	public static class DeeBuildOperationCreator extends BuildOperationCreator {
		
		public DeeBuildOperationCreator(IProject project, OperationInfo parentOpInfo, boolean fullBuild) {
			super(project, parentOpInfo, fullBuild);
		}
		
		@Override
		public IToolOperation newProjectBuildOperation() throws CommonException {
			IToolOperation projectBuildOp = super.newProjectBuildOperation();
			
			return (pm) -> {
				DeeCore.getDubProcessManager().submitTaskAndAwaitResult(() -> {
					projectBuildOp.execute(pm);
					return null;
				});
			};
		}
		
	}
	
	@Override
	public BuildTargetValidator createBuildTargetValidator(IProject project, String buildConfigName,
			String buildTypeName, String buildOptions) throws CommonException {
		return new BuildTargetValidator(project, buildConfigName, buildTypeName, buildOptions);
	}
	
	/* -----------------  ----------------- */
	
	protected class DeeBuildType extends BuildType {
		public DeeBuildType(String name) {
			super(name);
		}
		
		@Override
		public String getDefaultBuildOptions(BuildTargetValidator buildTargetValidator) throws CommonException {
			return "";
		}
		
		@Override
		public CommonBuildTargetOperation getBuildOperation(BuildTargetValidator buildTargetValidator,
				OperationInfo opInfo, Path buildToolPath, boolean fullBuild) throws CommonException, CoreException {
			return new DubBuildTargetOperation(buildTargetValidator, opInfo, buildToolPath, fullBuild);
		}
		
	}
	
}