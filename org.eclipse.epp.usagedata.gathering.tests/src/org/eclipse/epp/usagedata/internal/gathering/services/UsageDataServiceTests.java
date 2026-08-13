package org.eclipse.epp.usagedata.internal.gathering.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEventListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

/**
 * This test class tests the various method concerned with providing
 * usage data events to the service. This test case does not need to
 * run inside the workbench (i.e. it can be "Run As... &gt; JUnit Test".
 * @author Wayne
 *
 */
public class UsageDataServiceTests {
	private UsageDataService service;

	@BeforeEach
	public void setUp() throws Exception {
		service = new UsageDataService() {
			@Override
			protected void startMonitors() {
			}
			
			/*
			 * Override this method since we don't really care
			 * if the workbench is running and (at least in some
			 * cases) it will never start running.
			 */
			@Override
			protected void waitForWorkbenchToFinishStarting() {
			}
		};
		service.startMonitoring();
	}

	@AfterEach
	public void tearDown() throws Exception {
		service.stopMonitoring();
	}
	
	@Test
	@Timeout(value = 2, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
	public void testRecordEvent() throws Exception {
		final List<UsageDataEvent> events = new ArrayList<UsageDataEvent>();
		UsageDataEventListener listener = new UsageDataEventListener() {
			public void accept(UsageDataEvent event) {
				events.add(event);
			}			
		};
		service.addUsageDataEventListener(listener);
		long time = System.currentTimeMillis();
		service.recordEvent("bogus", "bogus", "bogus", "bogus");
		
		while (events.isEmpty()) Thread.sleep(100);
		
		// There should be only one event.
		UsageDataEvent event = events.get(0);
		
		assertEquals("bogus", event.bundleId);
		assertNull(event.bundleVersion);
		assertTrue(Math.abs(time - event.when) < 2000);
	}

	@Test
	@Timeout(value = 2, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
	public void testRecordEventWithBundleVersionResolution() throws Exception {
		final List<UsageDataEvent> events = new ArrayList<UsageDataEvent>();
		UsageDataEventListener listener = new UsageDataEventListener() {
			public void accept(UsageDataEvent event) {
				events.add(event);
			}			
		};
		service.addUsageDataEventListener(listener);
		service.recordEvent("started", "bundle", "bogus", "bogus_bundle", "bogus_version");
		service.recordEvent("bogus", "bogus", "bogus", "bogus_bundle");
		
		while (events.isEmpty()) Thread.sleep(100);
		
		UsageDataEvent bundleEvent = events.get(0);
		
		assertEquals("bogus_bundle", bundleEvent.bundleId);
		assertEquals("bogus_version", bundleEvent.bundleVersion);
		
		// The first event is the bundle start, the second one is the one we're interested in.
		UsageDataEvent event = events.get(1);
		
		assertEquals("bogus_bundle", event.bundleId);
		assertEquals("bogus_version", event.bundleVersion);
	}
}
