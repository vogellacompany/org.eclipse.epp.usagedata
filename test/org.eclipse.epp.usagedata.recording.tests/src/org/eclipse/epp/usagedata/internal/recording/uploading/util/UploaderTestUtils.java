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
import java.io.IOException;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.recording.UsageDataRecorder;
import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;

public class UploaderTestUtils {
	

	private static final int NUMBER_OF_ENTRIES_PER_DAY = 500;

	public UploaderTestUtils() throws IOException {
	}
	
	public static File createBogusUploadDataFile(UsageDataRecordingSettings settings, int days) throws Exception {
		DumbedDownUsageDataRecorder recorder = new DumbedDownUsageDataRecorder(settings);
		recorder.start();
		for(int index=0;index<days*NUMBER_OF_ENTRIES_PER_DAY;index++) {
			recorder.accept(new UsageDataEvent("bogus", "bogus", "bogus", "bogus","bogus",System.currentTimeMillis()));
		}
		recorder.dumpEvents();

		return settings.getEventFile();
	}

	public static UsageDataRecordingSettingsMock getSettings() throws Exception {

		File file = File.createTempFile("bogusUploadData", "csv");
		file.delete();
		
		return new UsageDataRecordingSettingsMock(file);
	}
}

class DumbedDownUsageDataRecorder extends UsageDataRecorder {
		
	private final UsageDataRecordingSettings settings;

	public DumbedDownUsageDataRecorder(UsageDataRecordingSettings settings) {
		this.settings = settings;
	}
	
	@Override
	protected UsageDataRecordingSettings getSettings() {
		return settings;
	}

	@Override
	protected void uploadDataIfNecessary() {
	}

	@Override
	public void dumpEvents() {
		super.dumpEvents();
	}
}
