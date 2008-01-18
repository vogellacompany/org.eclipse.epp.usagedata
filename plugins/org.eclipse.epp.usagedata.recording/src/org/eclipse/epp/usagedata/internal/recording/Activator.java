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
package org.eclipse.epp.usagedata.internal.recording;

import org.eclipse.core.runtime.Status;
import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;
import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;
import org.eclipse.epp.usagedata.internal.recording.uploading.UploadManager;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

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

	private ServiceTracker usageDataServiceTracker;
	
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
		
		usageDataServiceTracker = new ServiceTracker(context, UsageDataService.class.getName(), null);
		usageDataServiceTracker.open();
		
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
		return (UsageDataService)usageDataServiceTracker.getService();
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

	public void log(int status, String message, Object ... arguments) {
		log(status, (Exception)null, message, arguments);
	}
	
	public void log(int status, Exception exception, String message, Object ... arguments) {
		log(status, exception, String.format(message, arguments));
	}
	
	public void log(int status, Exception e, String message) {
		getLog().log(new Status(status, PLUGIN_ID, message, e));
	}
	

	public void log(Status status) {
		getLog().log(status);
	}

	public void earlyStartup() {
		// Don't actually need to do anything, but still need the method.		
	}

	public UploadManager getUploadManager() {
		return uploadManager;
	}


}
