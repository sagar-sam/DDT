/*******************************************************************************
 * Copyright (c) 2014, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package mmrnmhrm.core.projectmodel;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import melnorme.utilbox.misc.ListenerListHelper;
import melnorme.utilbox.misc.SimpleLogger;
import mmrnmhrm.core.projectmodel.IDubModel.IDubModelListener;

import org.eclipse.core.resources.IProject;

import dtool.dub.DubBundle;
import dtool.dub.DubBundle.DubBundleException;
import dtool.dub.DubBundleDescription;
import dtool.engine.compiler_installs.CompilerInstall;

public class DubModel extends ListenerListHelper<IDubModelListener> implements IDubModel {
	
	protected final SimpleLogger log = DubModelManager.log;
	
	protected final HashMap<String, ProjectInfo> projectInfos = new HashMap<>();
	
	public DubModel() {
	}
	
	@Override
	public void addListener(IDubModelListener listener) {
		super.addListener(listener);
	}
	@Override
	public void removeListener(IDubModelListener listener) {
		super.removeListener(listener);
	}
	
	protected void fireUpdateEvent(DubModelUpdateEvent updateEvent) {
		for (IDubModelListener listener : getListeners()) {
			listener.notifyUpdateEvent(updateEvent);
		}
	}
	
	@Override
	public synchronized DubBundleDescription getBundleInfo(String projectName) {
		ProjectInfo projectInfo = projectInfos.get(projectName);
		return projectInfo == null ? null : projectInfo.getBundleDesc();
	}
	
	public synchronized ProjectInfo getProjectInfo(String projectName) {
		return projectInfos.get(projectName);
	}
	
	@Override
	public synchronized Set<String> getDubProjects() {
		return new HashSet<>(projectInfos.keySet());
	}
	
	protected synchronized void addProjectInfo(IProject project, DubBundleDescription dubBundleDescription, 
			CompilerInstall compilerInstall) {
		ProjectInfo newProjectInfo = new ProjectInfo(compilerInstall, dubBundleDescription);
		addProjectInfo(project, newProjectInfo);
	}
	
	protected synchronized void addProjectInfo(IProject project, ProjectInfo newProjectInfo) {
		String projectName = project.getName();
		projectInfos.put(projectName, newProjectInfo);
		log.println("DUB project model added: " + projectName);
		fireUpdateEvent(new DubModelUpdateEvent(project, newProjectInfo.getBundleDesc()));
	}
	
	protected synchronized void removeProjectInfo(IProject project) {
		ProjectInfo oldProjectInfo = projectInfos.remove(project.getName());
		assertNotNull(oldProjectInfo);
		DubBundleDescription oldDesc = oldProjectInfo.getBundleDesc();
		log.println("DUB project model removed: " + project.getName());
		fireUpdateEvent(new DubModelUpdateEvent(project, oldDesc));
	}
	
	protected synchronized void addErrorToProjectInfo(IProject project, DubBundleException dubError) {
		ProjectInfo projectInfo = projectInfos.get(project.getName());
		DubBundleDescription unresolvedDescription = projectInfo.getBundleDesc();
		DubBundle main = unresolvedDescription.getMainBundle();
		
		DubBundleDescription bundleDesc = new DubBundleDescription(main, dubError);
		addProjectInfo(project, bundleDesc, projectInfo.compilerInstall);
	}
	
}