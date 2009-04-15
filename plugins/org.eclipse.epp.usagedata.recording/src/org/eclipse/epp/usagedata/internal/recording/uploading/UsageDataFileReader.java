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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.recording.UsageDataRecorderUtils;

public class UsageDataFileReader {
	public interface Iterator {
		public void header(String header) throws Exception;
		public void event(String line, UsageDataEvent event) throws Exception;
	}

	private final BufferedReader reader;
 
	/**
	 * This constructor creates an instance that will read the data contained in
	 * the <code>file</code> parameter. Note that if you use this constructor,
	 * you must explicitly {@link #close()} the resulting instance.
	 * 
	 * @param file
	 *            a {@link File}; the file must exist.
	 * @throws FileNotFoundException
	 *             if the file does not exist, or is a directory, or is some
	 *             other way a foolish choice.
	 * @throws IOException
	 */
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
	}

	private UsageDataEvent createUsageDataEvent(String line) {
		String[] tokens = UsageDataRecorderUtils.splitLine(line);
		UsageDataEvent usageDataEvent = new UsageDataEvent(tokens[0], tokens[1], tokens[4], tokens[2], tokens[3], Long.valueOf(tokens[5]));
		return usageDataEvent;
	}

	public void close() throws IOException {
		reader.close();
	}

	public void iterate(Iterator iterator) throws Exception {
		iterate(new NullProgressMonitor(), iterator);
	}

	public void iterate(IProgressMonitor monitor, Iterator iterator) throws Exception {
		monitor.beginTask("Iterate over usage data file", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		try {
			// The first line is the header.
			iterator.header(reader.readLine());
			while (true) {
				if (monitor.isCanceled()) break;
				String line = reader.readLine();
				if (line == null) break;
				UsageDataEvent event = createUsageDataEvent(line);
				iterator.event(line, event);
			}
		} finally {
			monitor.done();
		}
	}

}
