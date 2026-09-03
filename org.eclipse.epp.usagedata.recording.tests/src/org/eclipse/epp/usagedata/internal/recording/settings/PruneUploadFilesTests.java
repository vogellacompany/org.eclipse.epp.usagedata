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
package org.eclipse.epp.usagedata.internal.recording.settings;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * These tests confirm that staged upload files are bounded, both by age and by
 * the space they take up together.
 */
public class PruneUploadFilesTests {

	private File workingDirectory;

	@BeforeEach
	public void setup() throws Exception {
		workingDirectory = Files.createTempDirectory("usagedata").toFile(); //$NON-NLS-1$
	}

	@AfterEach
	public void tearDown() {
		File[] files = workingDirectory.listFiles();
		if (files != null) {
			for (File file : files) {
				file.delete();
			}
		}
		workingDirectory.delete();
	}

	@Test
	public void testDeletesFilesPastTheRetentionPeriod() throws Exception {
		File stale = uploadFile("upload0.csv", 100, UsageDataRecordingSettings.UPLOAD_FILE_RETENTION_DAYS + 1);
		File fresh = uploadFile("upload1.csv", 100, 1);

		settings().pruneOldUploadFiles();

		assertFalse(stale.exists());
		assertTrue(fresh.exists());
	}

	@Test
	public void testKeepsFilesThatFitInTheBudget() throws Exception {
		File first = uploadFile("upload0.csv", UsageDataRecordingSettings.UPLOAD_DIRECTORY_MAX_BYTES / 4, 3);
		File second = uploadFile("upload1.csv", UsageDataRecordingSettings.UPLOAD_DIRECTORY_MAX_BYTES / 4, 2);

		settings().pruneOldUploadFiles();

		assertTrue(first.exists());
		assertTrue(second.exists());
	}

	@Test
	public void testDeletesOldestFirstWhenOverTheBudget() throws Exception {
		long twoThirds = UsageDataRecordingSettings.UPLOAD_DIRECTORY_MAX_BYTES * 2 / 3;
		File oldest = uploadFile("upload0.csv", twoThirds, 3);
		File newest = uploadFile("upload1.csv", twoThirds, 1);

		settings().pruneOldUploadFiles();

		assertFalse(oldest.exists());
		assertTrue(newest.exists());
	}

	@Test
	public void testLeavesTheLiveEventFileAlone() throws Exception {
		File events = uploadFile("usagedata.csv", UsageDataRecordingSettings.UPLOAD_DIRECTORY_MAX_BYTES, 200);

		settings().pruneOldUploadFiles();

		assertTrue(events.exists());
	}

	/*
	 * The length is set rather than written, so that a file large enough to
	 * exceed the budget costs nothing to create.
	 */
	private File uploadFile(String name, long length, int ageInDays) throws Exception {
		File file = new File(workingDirectory, name);
		RandomAccessFile handle = new RandomAccessFile(file, "rw"); //$NON-NLS-1$
		try {
			handle.setLength(length);
		} finally {
			handle.close();
		}
		file.setLastModified(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ageInDays));
		return file;
	}

	private UsageDataRecordingSettings settings() {
		return new UsageDataRecordingSettings() {
			@Override
			protected File getWorkingDirectory() {
				return workingDirectory;
			}
		};
	}
}
