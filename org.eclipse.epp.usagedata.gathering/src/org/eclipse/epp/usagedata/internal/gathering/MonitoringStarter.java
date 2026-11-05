/*******************************************************************************
 * Copyright (c) 2026 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.gathering;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * Runs monitoring: starts it once the workbench has started, in place of an
 * early startup extension, which RCP applications do not always run, and
 * follows the capture preference from then on. The reference to the workbench
 * holds this back until the workspace is set, since it reads preferences.
 */
@Component(immediate = true, property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, //$NON-NLS-1$
		reference = @Reference(name = "workbench", service = IWorkbench.class))
public class MonitoringStarter implements EventHandler {

	private final AtomicBoolean started = new AtomicBoolean();

	private final IPropertyChangeListener captureListener = this::captureChanged;

	private UsageDataService service;

	private volatile boolean active;

	@Reference
	void setService(UsageDataService service) {
		this.service = service;
	}

	/**
	 * Covers a component that activates after the startup event was sent, such
	 * as when the bundle is installed into a running workbench.
	 */
	@Activate
	void activate() {
		active = true;
		UsageDataCaptureSettings.getPreferenceStore().addPropertyChangeListener(captureListener);
		Job job = Job.create("Usage Data Service Starter", (IProgressMonitor monitor) -> { //$NON-NLS-1$
			if (PlatformUI.isWorkbenchRunning() && !PlatformUI.getWorkbench().isStarting()) start();
			return Status.OK_STATUS;
		});
		job.setSystem(true);
		job.schedule();
	}

	@Deactivate
	void deactivate() {
		active = false;
		UsageDataCaptureSettings.getPreferenceStore().removePropertyChangeListener(captureListener);
		service.stopMonitoring();
		try {
			UsageDataCaptureSettings.savePreferences();
		} catch (IOException e) {
			Platform.getLog(MonitoringStarter.class).warn("Cannot save the preferences.", e); //$NON-NLS-1$
		}
	}

	@Override
	public void handleEvent(Event event) {
		start();
	}

	private void start() {
		if (!started.compareAndSet(false, true)) return;
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
			if (active && new UsageDataCaptureSettings().isEnabled()) service.startMonitoring();
		});
	}

	private void captureChanged(PropertyChangeEvent event) {
		if (!UsageDataCaptureSettings.CAPTURE_ENABLED_KEY.equals(event.getProperty())) return;
		Object value = event.getNewValue();
		boolean enabled = value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value));
		if (enabled) {
			service.startMonitoring();
		} else {
			service.stopMonitoring();
		}
	}
}
