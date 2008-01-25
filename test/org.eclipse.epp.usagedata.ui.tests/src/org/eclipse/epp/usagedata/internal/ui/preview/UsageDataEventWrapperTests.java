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
package org.eclipse.epp.usagedata.internal.ui.preview;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.recording.filtering.MockUsageDataEventFilter;
import org.eclipse.epp.usagedata.internal.recording.uploading.UploadParameters;
import org.eclipse.epp.usagedata.internal.ui.preview.util.MockUploadSettings;
import org.junit.Before;
import org.junit.Test;

/**
 * These tests can be run outside of the workbench.
 * 
 * @author Wayne Beaton
 */
public class UsageDataEventWrapperTests {

	private UploadParameters parameters;
	private UsageDataEventWrapper wrapper;
	private MockUsageDataEventFilter filter;
	private MockUploadSettings settings;

	@Before
	public void setup() {
		parameters = new UploadParameters();
		settings = new MockUploadSettings();
		filter = (MockUsageDataEventFilter)settings.getFilter();
		parameters.setSettings(settings);
		
		wrapper = new UsageDataEventWrapper(parameters, new UsageDataEvent("what", "kind", "description", "bundleId", "bundleVersion", 1000));
	}
	
	@Test
	public void testIsIncludedByFilter1() {
		// Should be null (unset) before we start
		assertNull(wrapper.isIncludedByFilter);
		// Lazy initialization should set it.
		assertTrue(wrapper.isIncludedByFilter());
	}

	@Test
	public void testIsIncludedByFilter2() {
		filter.addPattern("*");
		// Should be null (unset) before we start
		assertNull(wrapper.isIncludedByFilter);
		// Lazy initialization should set it.
		assertFalse(wrapper.isIncludedByFilter());
	}
	
	@Test
	public void testResetCaches() {
		// Should be null (unset) before we start
		assertNull(wrapper.isIncludedByFilter);
		// Lazy initialization should set it.
		assertTrue(wrapper.isIncludedByFilter());		
		wrapper.resetCaches();
		// Should be null (unset) again
		assertNull(wrapper.isIncludedByFilter);
	}

}
