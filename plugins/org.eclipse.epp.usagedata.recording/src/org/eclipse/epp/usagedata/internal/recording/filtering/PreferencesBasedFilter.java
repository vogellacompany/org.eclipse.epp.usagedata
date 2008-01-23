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
package org.eclipse.epp.usagedata.internal.recording.filtering;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.recording.Activator;
import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class PreferencesBasedFilter implements UsageDataEventFilter {

	private ListenerList changeListeners = new ListenerList();
	private IPropertyChangeListener propertyChangeListener;
	
	public PreferencesBasedFilter() {
		propertyChangeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (isFilterProperty(event.getProperty())) {
					fireFilterChangedEvent();
				}
			}			
		};
		getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
	}

	void fireFilterChangedEvent() {
		for (Object listener : changeListeners.getListeners()) {
			((FilterChangeListener)listener).filterChanged();
		}
	}

	boolean isFilterProperty(String property) {
		if (UsageDataRecordingSettings.FILTER_ECLIPSE_BUNDLES_ONLY_KEY.equals(property)) return true;
		if (UsageDataRecordingSettings.FILTER_PATTERNS_KEY.equals(property)) return true;
		return false;
	}

	public boolean includes(UsageDataEvent event) {
		if (includeOnlyEclipseDotOrgBundles()) {
			return event.bundleId.startsWith("org.eclipse.");
		}
		for (String filter : getFilterPatterns()) {
			if (matches(filter, event.bundleId)) return false;
		}
		return true;
	}

	private String[] getFilterPatterns() {
		String patternString = getPreferenceStore().getString(UsageDataRecordingSettings.FILTER_PATTERNS_KEY);
		return patternString.split("\n");
	}

	private boolean includeOnlyEclipseDotOrgBundles() {
		return getPreferenceStore().getBoolean(UsageDataRecordingSettings.FILTER_ECLIPSE_BUNDLES_ONLY_KEY);
	}

	private IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public void addFilterChangeListener(FilterChangeListener filterChangeListener) {
		changeListeners.add(filterChangeListener);
	}

	public void addPattern(String value) {
		String patternString = getPreferenceStore().getString(UsageDataRecordingSettings.FILTER_PATTERNS_KEY);
		if (patternString.trim().length() == 0) {
			patternString = value;
		} else {
			patternString += "\n" + value;
		}
		getPreferenceStore().setValue(UsageDataRecordingSettings.FILTER_PATTERNS_KEY, patternString);
		Activator.getDefault().savePluginPreferences();
	}
	
	boolean matches(String pattern, String bundleId) {
		return bundleId.matches(asRegex(pattern));
	}

	// TODO If we keep this, it needs to be more robust.
	String asRegex(String filter) {
		StringBuilder builder = new StringBuilder();
		for(int index=0;index<filter.length();index++) {
			char next = filter.charAt(index);
			if (next == '*') builder.append(".*");
			else if (next == '.') builder.append("\\.");
			else builder.append(next);
		}
		return builder.toString();
	}

	public void removeFilterChangeListener(FilterChangeListener filterChangeListener) {
		changeListeners.remove(filterChangeListener);
	}

	public boolean includesPattern(String pattern) {
		for (String filter : getFilterPatterns()) {
			if (pattern.equals(filter)) return true;
		}
		return false;
	}


}
