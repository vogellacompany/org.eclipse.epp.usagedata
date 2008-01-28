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
package org.eclipse.epp.usagedata.internal.recording.uploading.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.recording.UsageDataRecorderUtils;

public class UploaderTestUtils {
	

	private static final int NUMBER_OF_ENTRIES_PER_DAY = 500;

	public UploaderTestUtils() throws IOException {
	}
	
	public static File createBogusUploadDataFile(int days) throws Exception {
		File file = File.createTempFile("bogusUploadData", "csv");
		FileWriter writer = new FileWriter(file);
		UsageDataRecorderUtils.writeHeader(writer);
		for(int index=0;index<days*NUMBER_OF_ENTRIES_PER_DAY;index++) {
			UsageDataRecorderUtils.writeEvent(writer, new UsageDataEvent("bogus", "bogus", "bogus", "bogus","bogus",System.currentTimeMillis()));
		}

		writer.close();
		
		return file;
	}
}
