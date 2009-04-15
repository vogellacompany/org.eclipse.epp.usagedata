/*******************************************************************************
 * Copyright (c) 2009 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.recording;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.junit.Test;

public class UsageDataRecorderUtilsTests {

	@Test
	public void testWriteHeader() throws Exception {
		StringWriter writer = new StringWriter();
		UsageDataRecorderUtils.writeHeader(writer);
		assertEquals("what,kind,bundleId,bundleVersion,description,time\n", writer.toString());
	}

	@Test
	public void testWriteEvent() throws Exception {
		UsageDataEvent event = new UsageDataEvent("activate", "view", "myview", "mybundle", "1.0", 1000);
		StringWriter writer = new StringWriter();
		UsageDataRecorderUtils.writeEvent(writer, event);
		assertEquals("activate,view,mybundle,1.0,\"myview\",1000\n", writer.toString());
	}

	@Test
	public void testEncode() {
		assertEquals("\"first\"", UsageDataRecorderUtils.encode("first"));
		assertEquals("\"first, second\"", UsageDataRecorderUtils.encode("first, second"));
		assertEquals("\"first, \"\"second\"\"\"", UsageDataRecorderUtils.encode("first, \"second\""));
		assertEquals("\"first\\nsecond\"", UsageDataRecorderUtils.encode("first\nsecond"));
	}	
	
	@Test
	public void testSplitLine() {
		String[] strings = UsageDataRecorderUtils.splitLine("x,y,z");
		assertEquals("x", strings[0]);
		assertEquals("y", strings[1]);
		assertEquals("z", strings[2]);
	}
	
	@Test
	public void testSplitLineWithEscapedQuotes() {
		String[] strings = UsageDataRecorderUtils.splitLine("x,\"\"\"y\"\"\",z");
		assertEquals("x", strings[0]);
		assertEquals("\"y\"", strings[1]);
		assertEquals("z", strings[2]);
	}

	@Test
	public void testSplitLineWithQuotesAndCommas() {
		String[] strings = UsageDataRecorderUtils.splitLine("first,\"second, third\",fourth");
		assertEquals("first", strings[0]);
		assertEquals("second, third", strings[1]);
		assertEquals("fourth", strings[2]);
	}
	
	@Test
	public void testSplitLineWithEscapedQuotesAndCommas() {
		String[] strings = UsageDataRecorderUtils.splitLine("first,\"\"\"second\"\", third\",fourth");
		assertEquals("first", strings[0]);
		assertEquals("\"second\", third", strings[1]);
		assertEquals("fourth", strings[2]);
	}

}
