/*******************************************************************************
 * Copyright (c) 2007 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.recording;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.epp.usagedata.gathering.services.UsageDataService;
import org.eclipse.epp.usagedata.recording.settings.UsageDataRecordingSettings;
import org.eclipse.epp.usagedata.recording.uploading.UploadManager;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.epp.usagedata.recording";

	// The shared instance
	private static Activator plugin;

	private UploadManager uploadManager;

	private UsageDataRecordingSettings settings;

	private UsageDataRecorder usageDataRecorder;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		uploadManager = new UploadManager();
		settings = new UsageDataRecordingSettings();
		
		usageDataRecorder = new UsageDataRecorder();
		usageDataRecorder.start();
		getUsageDataService().addUsageDataEventListener(usageDataRecorder);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		usageDataRecorder.stop();
		getUsageDataService().removeUsageDataEventListener(usageDataRecorder);
		
		plugin = null;
		super.stop(context);
	}

	private UsageDataService getUsageDataService() {
		return org.eclipse.epp.usagedata.gathering.Activator.getDefault().getUsageDataCaptureService();
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public UsageDataRecordingSettings getSettings() {
		return settings;
	}
	
	public void logException(String message, Exception e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	// TODO Figure out why this is causing compiler problems.
	//@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		
	}

	public UploadManager getUploadManager() {
		return uploadManager;
	}
}
