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
package org.eclipse.epp.usagedata.internal.gathering;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.epp.usagedata.gathering";

	// The shared instance
	private static Activator plugin;

	private ServiceRegistration registration;

	private ServiceTracker usageDataServiceTracker;

	private UsageDataCaptureSettings settings;

	private BundleContext context;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;
		
		settings = new UsageDataCaptureSettings();
		
		final UsageDataService service = new UsageDataService();
		
		if (settings.isEnabled()) {
			service.startMonitoring();
		}
		
		getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				if (UsageDataCaptureSettings.CAPTURE_ENABLED_KEY.equals(event.getProperty())) {
					if (isTrue(event.getNewValue())) {
						service.startMonitoring();
					} else {
						service.stopMonitoring();
					}
				}
			}

			private boolean isTrue(Object newValue) {
				if (newValue instanceof Boolean) return ((Boolean)newValue).booleanValue();
				if (newValue instanceof String) return Boolean.valueOf((String)newValue);
				return false;
			}
			
		});
		
		// TODO There is basically no value to having this as a service at this point.
		// In fact, there is potential for some weirdness with this as a service. For
		// example, if the service is shut down it will just keep running anyway.
		registration = context.registerService(UsageDataService.class.getName(), service, null);
		
		usageDataServiceTracker = new ServiceTracker(context, UsageDataService.class.getName(), null);
		usageDataServiceTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {		
		this.context = context;
		UsageDataService service = getUsageDataCaptureService();
		if (service != null) service.stopMonitoring();
		
		usageDataServiceTracker.close();
		registration.unregister();
		
		plugin = null;
		super.stop(context);
	}

	private UsageDataService getUsageDataCaptureService() {
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

	public void earlyStartup() {
		// Do nothing.
	}
	
	/**
	 * <p>
	 * This is a convenience method for logging an exception.
	 * </p>
	 * 
	 * @param message
	 *            a {@link String} message to include in the log.
	 * @param e
	 *            the exception to capture in the log.
	 */
	public void logException(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	/**
	 * <p>
	 * This method returns the settings object for the usage data gathering
	 * plug-in.
	 * </p>
	 * 
	 * @see UsageDataCaptureSettings
	 * @return the instance of {@link UsageDataCaptureSettings} owned by the
	 *         receiver.
	 */
	public UsageDataCaptureSettings getSettings() {
		return settings;
	}

	public BundleContext getContext() {
		return context;
	}
}
