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
package org.eclipse.epp.usagedata.ui;

import java.io.File;

import org.eclipse.epp.usagedata.recording.uploading.Uploader;
import org.eclipse.epp.usagedata.recording.uploading.UploadManager;
import org.eclipse.epp.usagedata.recording.uploading.BasicUploader;

public class AskUserUploader implements Uploader {
	private BasicUploader basicUploader;

	public void startUpload(UploadManager uploadManager, File[] files) {
		// TODO This is where I need to create and open an editor. 
		basicUploader = new BasicUploader();
		basicUploader.startUpload(uploadManager, files);
	}

	public boolean isUploadInProgress() {
		return basicUploader.isUploadInProgress();
	}
}
