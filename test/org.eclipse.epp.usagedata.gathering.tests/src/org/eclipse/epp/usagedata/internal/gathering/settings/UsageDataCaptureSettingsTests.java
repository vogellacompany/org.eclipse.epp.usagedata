package org.eclipse.epp.usagedata.internal.gathering.settings;

import static org.junit.Assert.assertTrue;

import org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator;
import org.junit.Test;

/**
 * These tests confirm that the settings, based on values stored
 * in the preferences, are properly propagated. This test must
 * be run in the workbench.
 * 
 * @author Wayne Beaton
 *
 */
public class UsageDataCaptureSettingsTests {
	@Test
	public void testIsEnabledTrue() {
		UsageDataCaptureActivator.getDefault().getPreferenceStore().setValue(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY, true);
		
		assertTrue(UsageDataCaptureActivator.getDefault().getSettings().isEnabled());
	}
	
	@Test
	public void testSetEnabled() {
		UsageDataCaptureActivator.getDefault().getSettings().setEnabled(true);
		
		assertTrue(UsageDataCaptureActivator.getDefault().getPreferenceStore().getBoolean(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY));
	}
	
	@Test
	public void testHasUserAcceptedTermsOfUse() {
		UsageDataCaptureActivator.getDefault().getPreferenceStore().setValue(UsageDataCaptureSettings.USER_ACCEPTED_TERMS_OF_USE_KEY, true);
		
		assertTrue(UsageDataCaptureActivator.getDefault().getSettings().hasUserAcceptedTermsOfUse());
	}
	
	@Test
	public void testSetHasUserAcceptedTermsOfUse() {
		UsageDataCaptureActivator.getDefault().getSettings().setUserAcceptedTermsOfUse(true);
		
		assertTrue(UsageDataCaptureActivator.getDefault().getPreferenceStore().getBoolean(UsageDataCaptureSettings.USER_ACCEPTED_TERMS_OF_USE_KEY));
	}
}
