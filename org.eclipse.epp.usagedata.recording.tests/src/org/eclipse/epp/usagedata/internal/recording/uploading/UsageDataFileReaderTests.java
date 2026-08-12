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
package org.eclipse.epp.usagedata.internal.recording.uploading;

import java.io.StringReader;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.junit.Test;
import static org.junit.Assert.*;

public class UsageDataFileReaderTests {
	@Test
	public void testIterate() throws Exception {
		String content = "what,kind,bundleId,bundleVersion,description,time\n"
				+ "what,kind,bundleId,bundleVersion,description,123456\n"
				+ "what,kind,bundleId,bundleVersion,description,123456\n"
				+ "what,kind,bundleId,bundleVersion,description,123456\n";
		
		final StringBuilder builder = new StringBuilder();

		UsageDataFileReader reader = new UsageDataFileReader(new StringReader(content));
		reader.iterate(new UsageDataFileReader.Iterator() {

			public void event(String line, UsageDataEvent event) {
				builder.append(line);
				builder.append("\n");
			}

			public void header(String header) {
				builder.append(header);
				builder.append("\n");
			}
		});
		assertEquals(content, builder.toString());
	}

	@Test
	public void testIterateSkipsRowsWithMissingFields() throws Exception {
		String header = "what,kind,bundleId,bundleVersion,description,time\n";
		String valid =  "what,kind,bundleId,bundleVersion,description,123456\n";
		String invalid = "what,kind,bundleId,bundleVersion,description\n";
		
		final StringBuilder builder = new StringBuilder();

		UsageDataFileReader reader = new UsageDataFileReader(new StringReader(header+valid+invalid));
		reader.iterate(new UsageDataFileReader.Iterator() {

			public void event(String line, UsageDataEvent event) {
				builder.append(line);
				builder.append("\n");
			}

			public void header(String header) {
				builder.append(header);
				builder.append("\n");
			}
		});
		assertEquals(header+valid, builder.toString());
	}
	
	@Test
	public void testIterateSkipsRowsWithIncorrectlyFormattedNumberFields() throws Exception {
		String header = "what,kind,bundleId,bundleVersion,description,time\n";
		String valid =  "what,kind,bundleId,bundleVersion,description,123456\n";
		String invalid = "what,kind,bundleId,bundleVersion,description,123bob\n";
		
		final StringBuilder builder = new StringBuilder();

		UsageDataFileReader reader = new UsageDataFileReader(new StringReader(header+valid+invalid));
		reader.iterate(new UsageDataFileReader.Iterator() {

			public void event(String line, UsageDataEvent event) {
				builder.append(line);
				builder.append("\n");
			}

			public void header(String header) {
				builder.append(header);
				builder.append("\n");
			}
		});
		assertEquals(header+valid, builder.toString());
	}
	
	
	/**
	 * This test scans through a file containing real usage data events.
	 * We're happy if this test just runs without causing any exceptions.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReadSingleFile() throws Exception {
		UsageDataFileReader reader = new UsageDataFileReader(UsageDataFileReaderTests.class.getResourceAsStream("usagedata.csv"));
		reader.iterate(new UsageDataFileReader.Iterator() {
			public void event(String line, UsageDataEvent event) throws Exception {				
			}

			public void header(String header) throws Exception {				
			}
			
		});
	}
}
