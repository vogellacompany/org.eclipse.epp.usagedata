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

import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;

public class UsageDataRecordingSettingsMock extends	UsageDataRecordingSettings {
	private final File file;
	private String uploadUrl;

	UsageDataRecordingSettingsMock(File file) {
		this.file = file;
	}

	@Override
	public File getEventFile() {
		return file;
	}

	@Override
	public String getUserId() {
		return "bogus";
	}

	@Override
	public String getWorkspaceId() {
		return "bogus";
	}

	@Override
	public boolean isLoggingServerActivity() {
		return false;
	}

	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;		
	}
	
	@Override
	public String getUploadUrl() {
		return uploadUrl;
	}
	
	@Override
	public boolean hasUserAcceptedTermsOfUse() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
}