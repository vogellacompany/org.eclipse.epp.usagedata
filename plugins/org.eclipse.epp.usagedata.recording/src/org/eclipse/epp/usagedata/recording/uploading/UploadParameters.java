package org.eclipse.epp.usagedata.recording.uploading;

import java.io.File;

import org.eclipse.epp.usagedata.recording.settings.UsageDataRecordingSettings;

public class UploadParameters {

	private File[] files;
	private UsageDataRecordingSettings settings;
	private UploadManager uploadManager;

	public UploadParameters(UploadManager uploadManager) {
		this.uploadManager = uploadManager;
	}

	public void setSettings(UsageDataRecordingSettings settings) {
		this.settings = settings;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public UploadManager getUploadManager() {
		return uploadManager;
	}

	public UsageDataRecordingSettings getSettings() {
		return settings;
	}

	public File[] getFiles() {
		return files;
	}
}
