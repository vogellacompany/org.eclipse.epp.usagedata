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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.junit.Before;
import org.junit.Test;

public class AbstractUsageDataEventFilterTests {
	
	AbstractUsageDataEventFilter filter;
	
	@Before
	public void setup() {
		filter = new AbstractUsageDataEventFilter() {
			public boolean includes(UsageDataEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}

	@Test
	public void testAddThenRemoveFilterChangeListener() {
		assertTrue(filter.changeListeners.isEmpty());
		
		FilterChangeListener filterChangeListener = new FilterChangeListener() {
			public void filterChanged() {
				// TODO Auto-generated method stub
				
			}
		};
		filter.addFilterChangeListener(filterChangeListener);
		
		assertEquals(1, filter.changeListeners.size());
		
		filter.removeFilterChangeListener(filterChangeListener);

		assertTrue(filter.changeListeners.isEmpty());
	}

	@Test
	public void testFireFilterChangedEvent() {
		final List<Boolean> fired = new ArrayList<Boolean>();
		FilterChangeListener filterChangeListener = new FilterChangeListener() {
			public void filterChanged() {
				fired.add(true);
			}
		};
		filter.addFilterChangeListener(filterChangeListener);
		filter.fireFilterChangedEvent();
		
		assertEquals(1, fired.size());		
	}

	@Test
	public void testMatches() {
		assertTrue(filter.matches("*.core.*", "org.eclipse.core.ui"));
		assertTrue(filter.matches("*.core", "org.eclipse.core"));
		assertTrue(filter.matches("org.*", "org.eclipse.core"));

		assertFalse(filter.matches("*.core.*", "org.eclipse.core"));
		assertFalse(filter.matches("org.*", "com.eclipse.core"));
	}

	@Test
	public void testAsRegex() {
		assertEquals(".*\\.core", filter.asRegex("*.core"));
	}

}
