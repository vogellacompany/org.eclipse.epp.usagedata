/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.recording.settings;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Deleting the usage data removes the live event file and the staged upload
 * files, and nothing else in the working directory.
 */
public class DeleteUsageDataTests {

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
	public void testDeletesTheEventFileAndTheStagedUploads() throws Exception {
		File events = file("usagedata.csv");
		File staged = file("upload0.csv");
		File moreStaged = file("upload1.csv");

		settings().deleteUsageData();

		assertFalse(events.exists());
		assertFalse(staged.exists());
		assertFalse(moreStaged.exists());
	}

	@Test
	public void testLeavesOtherFilesAlone() throws Exception {
		File id = file(".workspaceId");

		settings().deleteUsageData();

		assertTrue(id.exists());
	}

	@Test
	public void testCopesWithNothingRecorded() {
		settings().deleteUsageData();
	}

	private File file(String name) throws Exception {
		File file = new File(workingDirectory, name);
		Files.write(file.toPath(), "x".getBytes()); //$NON-NLS-1$
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
