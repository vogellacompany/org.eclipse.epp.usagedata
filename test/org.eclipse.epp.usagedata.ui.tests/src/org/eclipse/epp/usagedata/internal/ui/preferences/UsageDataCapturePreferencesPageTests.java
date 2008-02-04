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
package org.eclipse.epp.usagedata.internal.ui.preferences;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class UsageDataCapturePreferencesPageTests {
	private UsageDataCapturePreferencesPage page;


	@SuppressWarnings({ "restriction"})
	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		while (!EclipseStarter.isRunning()) Thread.sleep(100);
	}
	
	@Before
	public void before() {
		getPreferenceStore().setValue(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY, true);
	
		Shell shell = new Shell(Display.getCurrent());

		page = new UsageDataCapturePreferencesPage();
		page.createContents(shell);
		page.init(null);
		
		// Don't actually open the shell.
	}

	
	@After
	public void after() {
		page.dispose();
	}
	
	
	@Test
	public void testReflectsEnabledFromPreferences() throws Exception {
		getPreferenceStore().setValue(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY, true);
		assertTrue(page.captureEnabledCheckbox.getSelection());
	}

	@Test
	public void testReflectsDisabledFromPreferences() throws Exception {
		getPreferenceStore().setValue(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY, false);
		assertFalse(page.captureEnabledCheckbox.getSelection());
	}

	@Test
	public void testPerformOkSetsEnabled() throws Exception {
		page.captureEnabledCheckbox.setSelection(true);
		page.performOk();
		assertTrue(getPreferenceStore().getBoolean(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY));
	}

	@Test
	public void testPerformOkSetsDisabled() throws Exception {
		page.captureEnabledCheckbox.setSelection(false);
		page.performOk();
		assertFalse(getPreferenceStore().getBoolean(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY));
	}
	
	private IPreferenceStore getPreferenceStore() {
		return UsageDataCaptureActivator.getDefault().getPreferenceStore();
	}
}
