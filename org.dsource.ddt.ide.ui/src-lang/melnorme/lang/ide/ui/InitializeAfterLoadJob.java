/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package melnorme.lang.ide.ui;

import melnorme.lang.ide.ui.LangPlugin;
import melnorme.lang.ide.ui.LangUIMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

public class InitializeAfterLoadJob extends UIJob {

	private final class RealJob extends Job {
		public RealJob(String name) {
			super(name);
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("", 10); //$NON-NLS-1$
			
			try {
				LangPlugin.initializeAfterLoad(new SubProgressMonitor(monitor, 4));
			} catch (CoreException e) {
				LangPlugin.log(e);
				LangPlugin.initialized = true;
				return e.getStatus();
			}
			
			LangPlugin.initialized = true;
			return new Status(IStatus.OK, LangPlugin.PLUGIN_ID, IStatus.OK, "", null);
		}
		@Override
		public boolean belongsTo(Object family) {
			return LangPlugin.PLUGIN_ID.equals(family);
		}
	}
	
	public InitializeAfterLoadJob() {
		super(LangUIMessages.InitializeAfterLoadJob_starter_job_name);
		setSystem(true);
	}
	
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		LangPlugin.initialized = false;
		Job job = new RealJob(LangUIMessages.LangPlugin_initializing_ui);
		job.setPriority(Job.SHORT);
		job.schedule();
		return new Status(IStatus.OK, LangPlugin.PLUGIN_ID, IStatus.OK, "", null);
	}
	
}