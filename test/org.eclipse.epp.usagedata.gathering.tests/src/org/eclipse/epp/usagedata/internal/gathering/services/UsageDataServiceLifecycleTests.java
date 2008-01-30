/*******************************************************************************
 * Copyright (c) 2008 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.gathering.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.util.tracker.ServiceTracker;

@SuppressWarnings("restriction")
public class UsageDataServiceLifecycleTests {
	private static ServiceTracker tracker;

	@BeforeClass
	public static void setup() throws Exception {
		while (!EclipseStarter.isRunning()) Thread.sleep(100);
		tracker = new ServiceTracker(UsageDataCaptureActivator.getDefault().getContext(), UsageDataService.class.getName(), null);
		tracker.open();
	}
	
	@AfterClass
	public static void shutdown() {
		tracker.close();
	}
	
	/**
	 * Before a test starts running, make sure that the service
	 * is running.
	 * 
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception {		
		getService().startMonitoring();
		while (getService().eventConsumerJob.getState() != Job.RUNNING) Thread.sleep(50);	
	}
	
	UsageDataService getService() {
		return (UsageDataService)tracker.getService();
	}
	
	@Test
	public void testServiceHasStarted() {
		assertTrue(getService().isMonitoring());
		assertNotNull(getService().eventConsumerJob);
		assertTrue(getService().eventConsumerJob.isSystem());
		assertEquals(Job.LONG, getService().eventConsumerJob.getPriority());
	}
	
	@Test (timeout=2000)
	public void testServiceStops() throws Exception {
		assertTrue(getService().isMonitoring());
		getService().stopMonitoring();
		assertFalse(getService().isMonitoring());
		while (eventConsumerJobIsRunning()) Thread.sleep(50);		
	}

	private boolean eventConsumerJobIsRunning() {
		if (getService() == null) return false;
		if (getService().eventConsumerJob == null) return false;
		
		return getService().eventConsumerJob.getState() == Job.RUNNING;
	}
	
	public void preferenceChangeStopsMonitoring() throws Exception {
		assertTrue(getService().isMonitoring());
		UsageDataCaptureActivator.getDefault().getPreferenceStore().setValue(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY, false);
		
		assertFalse(getService().isMonitoring());
		while (eventConsumerJobIsRunning()) Thread.sleep(50);	
	}
}
