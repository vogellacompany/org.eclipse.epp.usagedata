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
package org.eclipse.epp.usagedata.internal.gathering.settings;

import static org.junit.Assert.assertTrue;

import org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator;
import org.eclipse.jface.preference.IPreferenceStore;
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
		getPreferenceStore().setValue(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY, true);
		
		assertTrue(getCaptureSettings().isEnabled());
	}

	@Test
	public void testSetEnabled() {
		getCaptureSettings().setEnabled(true);
		
		assertTrue(getPreferenceStore().getBoolean(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY));
	}
	
	@Test
	public void testHasUserAcceptedTermsOfUse() {
		getPreferenceStore().setValue(UsageDataCaptureSettings.USER_ACCEPTED_TERMS_OF_USE_KEY, true);
		
		assertTrue(getCaptureSettings().hasUserAcceptedTermsOfUse());
	}
	
	@Test
	public void testSetHasUserAcceptedTermsOfUse() {
		getCaptureSettings().setUserAcceptedTermsOfUse(true);
		
		assertTrue(getPreferenceStore().getBoolean(UsageDataCaptureSettings.USER_ACCEPTED_TERMS_OF_USE_KEY));
	}
	

	private UsageDataCaptureSettings getCaptureSettings() {
		return getCaptureActivator().getSettings();
	}
	
	private IPreferenceStore getPreferenceStore() {
		return getCaptureActivator().getPreferenceStore();
	}

	private UsageDataCaptureActivator getCaptureActivator() {
		return UsageDataCaptureActivator.getDefault();
	}
}
