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
package org.eclipse.epp.usagedata.internal.recording.uploading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;

public class UsageDataFileReader {
	public interface Iterator {
		public void header(String header) throws Exception;
		public void event(String line, UsageDataEvent event) throws Exception;
	}

	private final BufferedReader reader;
	private String header;
 
	public UsageDataFileReader(File file) throws IOException {
		this(new FileInputStream(file));
	}

	public UsageDataFileReader(InputStream inputStream) throws IOException {
		this(new InputStreamReader(inputStream));
	}

	public UsageDataFileReader(Reader reader) throws IOException {
		this(new BufferedReader(reader));
	}
	
	public UsageDataFileReader(BufferedReader bufferedReader) throws IOException {
		reader = bufferedReader;
		header = reader.readLine(); // Clear out the header line.
	}

	public UsageDataEvent next() throws IOException {
		String line = reader.readLine();
		if (line == null) return null;
		return createUsageDataEvent(line);
	}

	private UsageDataEvent createUsageDataEvent(String line) {
		String[] tokens = line.split("\\,");
		UsageDataEvent usageDataEvent = new UsageDataEvent(tokens[0], tokens[1], tokens[4], tokens[2], tokens[3], Long.valueOf(tokens[5]));
		return usageDataEvent;
	}

	public void close() throws IOException {
		reader.close();
	}

	public void iterate(Iterator iterator) throws Exception {
		iterator.header(header);
		while (true) {
			String line = reader.readLine();
			if (line == null) return;
			UsageDataEvent event = createUsageDataEvent(line);
			iterator.event(line, event);
		}
	}

}
