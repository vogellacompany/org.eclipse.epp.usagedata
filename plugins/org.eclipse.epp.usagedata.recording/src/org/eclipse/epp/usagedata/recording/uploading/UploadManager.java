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
package org.eclipse.epp.usagedata.recording.uploading;

import java.io.File;

import org.eclipse.epp.usagedata.recording.Activator;
import org.eclipse.epp.usagedata.recording.settings.UsageDataRecordingSettings;

public class UploadManager {

	private Uploader uploader;

	/**
	 * This method starts the upload. The first thing it does is find the files
	 * containing data that needs to be uploaded. If no data is found, then the
	 * method simply returns and the universe is left to unfold as it will. If
	 * data is found, we continue.
	 * <p>
	 * The settings are checked to see what the user wants us to do with the
	 * data. If the user has authorized that the data be uploaded, an upload job
	 * is spawned. If the settings indicate that the user must be asked what to
	 * do, then an editor is opened which invites the user to decide what to do
	 * with the information.
	 * </p>
	 */
	public synchronized void startUpload() {
		if (isUploadInProgress()) return;
		File[] usageDataUploadFiles = findUsageDataUploadFiles();
		if (usageDataUploadFiles.length == 0) return;
		
		// TODO Figure out how to do user interaction.
		uploader = getSettings().getUploader();
		
		uploader.startUpload(this, usageDataUploadFiles);
	}

	
	private boolean isUploadInProgress() {
		if (uploader == null) return false;
		return uploader.isUploadInProgress();
	}

	private File[] findUsageDataUploadFiles() {
		return getSettings().getUsageDataUploadFiles();
	}
	
	private UsageDataRecordingSettings getSettings() {
		return Activator.getDefault().getSettings();
	}

	public synchronized void uploadComplete() {
		uploader = null;
	}

}
