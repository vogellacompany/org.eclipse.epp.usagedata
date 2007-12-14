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
package org.eclipse.epp.usagedata.gathering.monitors;

import org.eclipse.epp.usagedata.gathering.Activator;
import org.eclipse.epp.usagedata.gathering.services.UsageDataService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * Instances of this class hook into the {@link BundleContext} so
 * that they are notified of bundle events. Those events are passed
 * to the {@link UsageDataService}.
 * 
 * @author Wayne Beaton
 *
 */
public class BundleUsageMonitor implements UsageMonitor {

	private BundleListener bundleUsageListener;

	public void startMonitoring(final UsageDataService usageDataService) {
		// First, create events for all the bundles that have already been registered.
		recordCurrentlyInstalledBundles(usageDataService);
		
		// Create an install a listener on the bundle context.
		bundleUsageListener = new BundleListener() {
			public void bundleChanged(BundleEvent event) {
				usageDataService.recordEvent(getWhatHappenedString(event), "bundle", event.getBundle().getSymbolicName(), event.getBundle().getSymbolicName(), getBundleVersion(event));
			}			
		};
		getBundleContext().addBundleListener(bundleUsageListener);
	}


	private void recordCurrentlyInstalledBundles(UsageDataService usageDataService) {
		for (Bundle bundle : getBundleContext().getBundles()) {
			if (bundle.getState() != Bundle.ACTIVE) continue;
			String bundleId = bundle.getSymbolicName();
			usageDataService.recordEvent("started", "bundle", bundleId, bundleId, getBundleVersion(bundle));
		}
	}

	/**
	 * This method returns a {@link String} that describes what caused this
	 * {@link BundleEvent} to be fired.
	 * 
	 * @param event
	 *            instance of {@link BundleEvent}.
	 * @return {@link String} describing what happened.
	 */
	protected String getWhatHappenedString(BundleEvent event) {
		switch (event.getType()) {
			case BundleEvent.INSTALLED: return "installed";
			case BundleEvent.LAZY_ACTIVATION: return "lazy_activation";
			case BundleEvent.RESOLVED: return "resolved";
			case BundleEvent.STARTED: return "started";
			case BundleEvent.STARTING: return "starting";
			case BundleEvent.STOPPED: return "stopped";
			case BundleEvent.STOPPING: return "stopping";
			case BundleEvent.UNINSTALLED: return "uninstalled";
			case BundleEvent.UNRESOLVED: return "unresolved";
			case BundleEvent.UPDATED: return "updated";
			default: return "unknown";
		}
	}

	protected String getBundleVersion(BundleEvent event) {
		return getBundleVersion(event.getBundle());
	}

	private String getBundleVersion(Bundle bundle) {
		return (String)bundle.getHeaders().get("Bundle-Version");
	}


	public void stopMonitoring() {
		getBundleContext().removeBundleListener(bundleUsageListener);
	}	

	private BundleContext getBundleContext() {
		return Activator.getDefault().getBundle().getBundleContext();
	}

}
