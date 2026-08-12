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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class implements a &quot;mock&quot; implementation of
 * {@link UsageDataEventFilter} to be used for testing. The
 * implementation extends {@link PreferencesBasedFilter}, overriding
 * the methods that read to and write from the preferences and replacing
 * them with in-memory variants.
 * 
 * @author Wayne Beaton
 */
public class MockUsageDataEventFilter extends PreferencesBasedFilter {

	private List<String> patterns = new ArrayList<String>();
	private boolean isEclipseOnly = false;

	public boolean includes(UsageDataEvent event) {
		for (String pattern : patterns) {
			if (matches(pattern, event.bundleId)) return false;
		}
		return true;
	}

	@Override
	protected void hookListeners() {
	}
	
	@Override
	public void addPattern(String value) {
		patterns.add(value);
		fireFilterChangedEvent();
	}
	
	@Override
	public boolean isEclipseOnly() {
		return isEclipseOnly;
	}
	
	@Override
	public void setEclipseOnly(boolean value) {
		isEclipseOnly = value;
		fireFilterChangedEvent();
	}
	
	@Override
	public void removeFilterPatterns(Object[] toRemove) {
		for (Object pattern : toRemove) {
			patterns.remove(pattern);
		}
		fireFilterChangedEvent();
	}
	
	@Override
	public String[] getFilterPatterns() {
		return (String[]) patterns.toArray(new String[patterns.size()]);
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();
	}	
	
	@Override
	IPreferenceStore getPreferenceStore() {
		// Override to make sure that nothing bad happens by mistake...
		return null;
	}
}
