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
package org.eclipse.epp.usagedata.gathering.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.epp.usagedata.gathering.Activator;
import org.eclipse.epp.usagedata.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.gathering.events.UsageDataEventListener;
import org.eclipse.epp.usagedata.gathering.monitors.UsageMonitor;

@SuppressWarnings("restriction")
public class UsageDataService {
	private boolean monitoring = false;

	/**
	 * The list of monitors hooked into various parts of the system listening to
	 * what the user is up to. The objects in this list are of type
	 * {@link UsageMonitor}. Strictly speaking this is not a list of
	 * "listeners", but {@link ListenerList} provides some convenient management
	 * functionality.
	 */
	private ListenerList monitors = new ListenerList();

	/**
	 * The list of objects of type {@link UsageDataEventListener} listening to events
	 * generated by this service.
	 */
	private ListenerList eventListeners = new ListenerList();
	
	/**
	 * The thread that figures out what to do with events provided by the 
	 * various monitors. This functionality is separated into a separate thread
	 * in anticipation of performance issues (see {@link #startEventConsumerJob()}
	 * for discussion.
	 */
	private Job eventConsumerJob;	
	
	/**
	 * A temporary home for events as they are generated. As they are created, 
	 * events are dropped into the queue by the source thread. Events are consumed
	 * from the queue by the {@link #eventConsumerJob}.
	 */
	protected LinkedBlockingQueue<UsageDataEvent> events = new LinkedBlockingQueue<UsageDataEvent>();

	/**
	 * This field maps the symbolic name of bundles to the last loaded version.
	 * This information is handy for filling in missing bundle version information
	 * for singleton bundles.
	 */
	private Map<String, String> bundleVersionMap = new HashMap<String, String>();

	/**
	 * This method starts the monitoring process. If the service has already been
	 * "started" when this method is called, nothing happens (i.e. multiple calls
	 * to this method are tolerated).
	 */
	public void startMonitoring() {
		if (isMonitoring())
			return;

		startMonitors();
		startEventConsumerJob();

		monitoring = true;

	}
	
	/**
	 * This method stops the monitoring process. If the service is already stopped
	 * when this method is called, nothing happens (i.e. multiple calls
	 * to this method are tolerated).
	 */
	public synchronized void stopMonitoring() {
		if (!isMonitoring())
			return;

		stopMonitors();

		monitoring = false;

		eventConsumerJob.cancel();
		
		eventConsumerJob = null;
	}

	public boolean isMonitoring() {
		return monitoring;
	}
	
	/**
	 * Start the {@link #eventConsumerJob}. Various monitors add events to the
	 * {@link #events} queue. In order to avoid degrading system performance any
	 * more than necessary, events are added to the queue by the monitors. The
	 * {@link #eventConsumerJob} then consumes the events from the queue and
	 * dispatches them to the various {@link UsageDataEventListener}s. Since
	 * the event listeners will do expensive things like open and write to
	 * files, it is anticipated that this architecture will allow the necessary
	 * activities to happen without significantly impacting the user's
	 * experience.
	 */
	private void startEventConsumerJob() {
		// TODO Decide if the job is more trouble than it's worth.
		if (eventConsumerJob != null) return;
		
		eventConsumerJob = new Job("Usage Data Event consumer") {
			private boolean cancelled = false;
			// TODO Sort out why the override is causing compiler errors on build.
			//@Override
			public IStatus run(IProgressMonitor monitor) {
				waitForWorkbenchToFinishStarting();
				while (!cancelled) {
					UsageDataEvent event = getQueuedEvent();
					dispatchEvent(event);
				}
				return Status.OK_STATUS;
			}
		
			protected void canceling() {
				cancelled = true;
			}

			/**
			 * This method pauses the current thread until the workbench has
			 * finished starting. This should provide enough time for bundles
			 * that are installing usage data event listeners to complete before
			 * events are dispatched.
			 */
			private void waitForWorkbenchToFinishStarting() {
				/*
				 * We want the job to pause until after all the bundles that are
				 * loaded at startup have finished loading. This will give
				 * bundles that listen to usage data events time to load and
				 * install listeners before events are fired off (which should
				 * mean that events won't get lost).
				 * 
				 * I had originally tried using Display.syncExec(Runnable) (with
				 * an "do nothing" Runnable, but this caused some weird classloading
				 * issues similar to those referenced in Bug 88109.
				 * 
				 * The EclipseStarter.isRunning() method seems to fit the bill,
				 * despite being restricted access.
				 * 
				 * TODO Re-evaluate use of restricted access code.
				 */
				while (!EclipseStarter.isRunning()) {
					try {
						// It probably doesn't matter too much if we wait too long here.
						// TODO Is 1 second too long?
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						//  Ignore and loop again!
					}
				}
			}
		};
		eventConsumerJob.setSystem(true);
		eventConsumerJob.setPriority(Job.LONG);
		eventConsumerJob.schedule(1000); // Wait a few minutes before scheduling the job.
	}

	/**
	 * This method returns the next available event. If no event is available,
	 * the current thread is suspended until an event is added. This method will
	 * return <code>null</null> if it is called with an empty event queue after
	 * monitoring is turned off or if the thread is interrupted.
	 * 
	 * @return an instance of {@link UsageDataEvent} or <code>null</code>.
	 */
	private UsageDataEvent getQueuedEvent() {
		try {
			return events.take();
		} catch (InterruptedException e) {
			return null;
		}
	}

	/**
	 * This method queues an event containing the given information for
	 * processing.
	 * 
	 * @param what
	 *            what happened? was it an activation, started, clicked, ... ?
	 * @param kind
	 *            what kind of thing caused it? view, editor, bundle, ... ?
	 * @param description
	 *            information about the event. e.g. name of the command, view,
	 *            editor, ...
	 * @param bundleId
	 *            symbolic name of the bundle that owns the thing that caused
	 *            the event.
	 */
	public void recordEvent(String what, String kind, String description,
			String bundleId) {
		recordEvent(what, kind, description, bundleId, getBundleVersion(bundleId));
	}
	
	/**
	 * <p>
	 * This method queues an event containing the given information for
	 * processing.
	 * </p>
	 * 
	 * @param what
	 *            what happened? was it an activation, started, clicked, ... ?
	 * @param kind
	 *            what kind of thing caused it? view, editor, bundle, ... ?
	 * @param description
	 *            information about the event. e.g. name of the command, view,
	 *            editor, ...
	 * @param bundleId
	 *            symbolic name of the bundle that owns the thing that caused
	 *            the event.
	 * @param bundleVersion
	 *            the version of the bundle that owns the thing that caused the
	 *            event.
	 */
	public void recordEvent(String what, String kind, String description,
			String bundleId, String bundleVersion) {
		UsageDataEvent event = new UsageDataEvent(what, kind, description, bundleId,
				bundleVersion, System.currentTimeMillis());
		registerBundleVersion(event);
		recordEvent(event);

	}

	private void recordEvent(UsageDataEvent event) {
		/*
		 * Multiple thread access to #events is managed the LinkedBlockingQueue
		 * implementation.
		 */
		events.add(event);
	}

	/**
	 * If the event represents a bundle activation, record a mapping between the
	 * bundleId and bundleVersion. This information is used to fill in missing
	 * information when an event comes in with just a bundleId and no version
	 * information. This assumes that the bundle is a singleton. That is, there
	 * is no provision here for dealing with multiple versions of bundles. If
	 * the event is a result of something that may come from a non-singleton
	 * bundle, then it is the responsibility of the event source to determine
	 * the appropriate version.
	 * 
	 * @param event
	 *            instance of {@link UsageDataEvent}.
	 */
	private void registerBundleVersion(UsageDataEvent event) {
		if (!("bundle".equals(event.kind))) return;
		if (!("started".equals(event.what))) return;
		
		synchronized (bundleVersionMap) {
			bundleVersionMap.put(event.bundleId, event.bundleVersion);
		}
	}

	/**
	 * <p>
	 * This method returns the version of the bundle with id bundleId. This
	 * assumes that the bundle is a singleton. That is, there is no provision
	 * here for dealing with multiple versions of bundles. If the event is a
	 * result of something that may come from a non-singleton bundle, then it is
	 * the responsibility of the event source to determine the appropriate
	 * version.
	 * </p>
	 * 
	 * @param bundleId
	 *            the symbolic name of a bundle.
	 * 
	 * @return the id of the last bundle started with the given id.
	 */
	private String getBundleVersion(String bundleId) {
		if (bundleId == null) return null;
		synchronized (bundleVersionMap) {
			return bundleVersionMap.get(bundleId);
		}
	}
	
	/**
	 * This method dispatches <code>event</code> to the registered event
	 * listeners.
	 * 
	 * @param event
	 *            the {@link UsageDataEvent} to dispatch.
	 */
	private void dispatchEvent(UsageDataEvent event) {
		Object[] listeners = eventListeners.getListeners();
		for (int index = 0; index < listeners.length; index++) {
			UsageDataEventListener listener = (UsageDataEventListener) listeners[index];
			dispatchEvent(event, listener);
		}
	}

	/**
	 * This method does the actual dispatching of the event to a single listener. If
	 * an exception occurs in the execution of the listener, an exception is logged.
	 *
	 * @param event
	 * @param listener
	 */
	private void dispatchEvent(UsageDataEvent event, UsageDataEventListener listener) {
		try {
			listener.accept(event);
		} catch (Throwable e) {
			// TODO Add some logic to remove repeat offenders.
			Activator.getDefault().logException("The listener (" + listener.getClass() + ") threw an exception", e);
		}
	}

	private void startMonitors() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(
						"org.eclipse.epp.usagedata.gathering.monitors");
		for (IConfigurationElement element : elements) {
			if ("monitor".equals(element.getName())) {

				try {
					Object monitor = element.createExecutableExtension("class");
					if (monitor instanceof UsageMonitor) {
						startMonitor((UsageMonitor) monitor);
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void startMonitor(UsageMonitor monitor) {
		monitor.startMonitoring(this);
		monitors.add(monitor);
	}

	private void stopMonitors() {
		for (Object monitor : monitors.getListeners()) {
			stopMonitor((UsageMonitor) monitor);
		}
	}

	private void stopMonitor(UsageMonitor monitor) {
		monitor.stopMonitoring();
		monitors.remove(monitor);
	}

	public void addUsageDataEventListener(UsageDataEventListener listener) {
		eventListeners.add(listener);
	}

	public void removeUsageDataEventListener(UsageDataEventListener listener) {
		eventListeners.remove(listener);		
	}

}
