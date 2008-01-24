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

import org.eclipse.epp.usagedata.internal.recording.filtering.FilterUtils;
import org.junit.Test;

public class FilterUtilsTests {

	@Test
	public void testGetFilterSuggestionBasedOnBundleIds1() {
		String suggestion = FilterUtils.getFilterSuggestionBasedOnBundleIds(new String[] {
			"org.eclipse.core.stuff", "org.eclipse.core.junk", "org.eclipse.junk"
		});
		
		assertEquals("org.eclipse.*", suggestion);
	}	

	@Test
	public void testGetFilterSuggestionBasedOnBundleIds() {
		String suggestion = FilterUtils.getFilterSuggestionBasedOnBundleIds(new String[] {
			"org.eclipse.core.stuff", "org.eclipse.core"
		});
		
		assertEquals("org.eclipse.core*", suggestion);
	}

	@Test
	public void testIsValidBundleIdPattern() {
		// What's good...
		assertTrue(FilterUtils.isValidBundleIdPattern("org.eclipse.*"));
		assertTrue(FilterUtils.isValidBundleIdPattern("org.*.core"));
		assertTrue(FilterUtils.isValidBundleIdPattern("*.core.*"));
		assertTrue(FilterUtils.isValidBundleIdPattern("*.*.*"));
		
		// Some counter examples...
		assertFalse(FilterUtils.isValidBundleIdPattern("$"));
		assertFalse(FilterUtils.isValidBundleIdPattern(".core"));
		assertFalse(FilterUtils.isValidBundleIdPattern("core."));
		assertFalse(FilterUtils.isValidBundleIdPattern(".core."));
		assertFalse(FilterUtils.isValidBundleIdPattern(""));
		assertFalse(FilterUtils.isValidBundleIdPattern("."));
	}

}
