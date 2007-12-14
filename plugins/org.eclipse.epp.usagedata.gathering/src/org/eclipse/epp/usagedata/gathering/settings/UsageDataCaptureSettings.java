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
package org.eclipse.epp.usagedata.gathering.settings;

import org.eclipse.epp.usagedata.gathering.Activator;
import org.eclipse.jface.preference.IPreferenceStore;

public class UsageDataCaptureSettings {

	public static final String CAPTURE_ENABLED_KEY = Activator.PLUGIN_ID + ".enabled";

	public boolean isEnabled() {
		if (System.getProperties().containsKey(CAPTURE_ENABLED_KEY)) {
			return "true".equals(System.getProperty(CAPTURE_ENABLED_KEY));
		} else if (getPreferencesStore().contains(CAPTURE_ENABLED_KEY)) {
			return getPreferencesStore().getBoolean(CAPTURE_ENABLED_KEY);
		} else {
			return true;
		}
	}

	public void setEnabled(boolean value) {
		// The preferences store actually does this for us. However, for
		// completeness, we're checking the value to potentially avoid 
		// messing with the service.
		if (getPreferencesStore().getBoolean(CAPTURE_ENABLED_KEY) == value) return;
		
		getPreferencesStore().setValue(CAPTURE_ENABLED_KEY, value);
		// TODO Is this good enough, or do we need an observer?
		if (value) {
			Activator.getDefault().getUsageDataCaptureService().startMonitoring();
		} else {
			Activator.getDefault().getUsageDataCaptureService().stopMonitoring();
		}
	}
	
	private IPreferenceStore getPreferencesStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
