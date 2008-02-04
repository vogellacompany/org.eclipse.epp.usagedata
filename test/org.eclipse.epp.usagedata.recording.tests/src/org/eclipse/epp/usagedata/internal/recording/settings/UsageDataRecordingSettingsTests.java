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
package org.eclipse.epp.usagedata.internal.recording.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.epp.usagedata.internal.recording.UsageDataRecordingActivator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.Before;
import org.junit.Test;

/**
 * These tests confirm that the settings, based on values stored
 * in the preferences, are properly propagated. This test must
 * be run in the workbench.
 * 
 * @author Wayne Beaton
 *
 */
public class UsageDataRecordingSettingsTests {

	static final long TEN_SECONDS = 10 * 1000;
	
	static final long ONE_DAY = 24 * 60 * 1000;
	static final long FIVE_DAYS = 5 * ONE_DAY;
	static final long TWO_DAYS = 2 * ONE_DAY;
	
	@Before
	public void setup() {
		System.clearProperty(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY);
		getPreferenceStore().setToDefault(UsageDataRecordingSettings.ASK_TO_UPLOAD_KEY);
		getPreferenceStore().setToDefault(UsageDataRecordingSettings.FILTER_ECLIPSE_BUNDLES_ONLY_KEY);
		getPreferenceStore().setToDefault(UsageDataRecordingSettings.FILTER_PATTERNS_KEY);
		getPreferenceStore().setToDefault(UsageDataRecordingSettings.LAST_UPLOAD_KEY);
		getPreferenceStore().setToDefault(UsageDataRecordingSettings.LOG_SERVER_ACTIVITY_KEY);
		getPreferenceStore().setToDefault(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY);
	}
	
	@Test
	public void testGetUploadPeriodFromSystemProperty() throws Exception {
		System.setProperty(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY, String.valueOf(FIVE_DAYS));
		
		assertEquals(FIVE_DAYS, getRecordingSettings().getPeriodBetweenUploads());
	}
	
	@Test
	public void testGetUploadPeriodFromPreference() throws Exception {
		getPreferenceStore().setValue(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY, FIVE_DAYS);
		
		assertEquals(FIVE_DAYS, getRecordingSettings().getPeriodBetweenUploads());
	}

	@Test
	public void testGetUploadPeriodValueTooSmall() throws Exception {
		getPreferenceStore().setValue(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY, TEN_SECONDS);
		
		assertEquals(UsageDataRecordingSettings.PERIOD_REASONABLE_MINIMUM, getRecordingSettings().getPeriodBetweenUploads());
	}

	@Test
	public void testGetLastUploadTimeFirstInvocation() throws Exception {
		long lastUploadTime = getRecordingSettings().getLastUploadTime();
		long time = System.currentTimeMillis();
		
		assertTrue(time - lastUploadTime < 50);
	}
	
	@Test
	public void testGetLastUploadTimeWithPreviousUpload() throws Exception {
		getPreferenceStore().setValue(UsageDataRecordingSettings.LAST_UPLOAD_KEY, 1000);
		assertEquals(1000, getRecordingSettings().getLastUploadTime());
	}
	
	@Test
	public void testIsTimeToUploadAnswersFalse() throws Exception {
		long currentTime = System.currentTimeMillis();
		getPreferenceStore().setValue(UsageDataRecordingSettings.LAST_UPLOAD_KEY, currentTime-TWO_DAYS);
		getPreferenceStore().setValue(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY, FIVE_DAYS);
		
		assertFalse(getRecordingSettings().isTimeToUpload());
	}
	
	@Test
	public void testIsTimeToUploadAnswersTrue() throws Exception {
		long currentTime = System.currentTimeMillis();
		getPreferenceStore().setValue(UsageDataRecordingSettings.LAST_UPLOAD_KEY, currentTime-FIVE_DAYS);
		getPreferenceStore().setValue(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY, TWO_DAYS);
		
		assertTrue(getRecordingSettings().isTimeToUpload());
	}
	
	private UsageDataRecordingSettings getRecordingSettings() {
		return getRecordingActivator().getSettings();
	}

	private IPreferenceStore getPreferenceStore() {
		return getRecordingActivator().getPreferenceStore();
	}

	private UsageDataRecordingActivator getRecordingActivator() {
		return UsageDataRecordingActivator.getDefault();
	}
}
