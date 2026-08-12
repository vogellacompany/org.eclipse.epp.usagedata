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

import org.eclipse.epp.usagedata.internal.recording.filtering.NullFilter;
import org.eclipse.epp.usagedata.internal.recording.filtering.UsageDataEventFilter;
import org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings;

public class MockUploadSettings implements UploadSettings {

	private String uploadUrl;
	private UsageDataEventFilter filter = new NullFilter();

	public UsageDataEventFilter getFilter() {
		return filter;
	}

	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;		
	}
	
	public String getUploadUrl() {
		return uploadUrl;
	}

	public String getUserId() {
		return "bogusUserId";
	}

	public String getWorkspaceId() {
		return "bogusWorkspaceId";
	}

	public boolean hasUserAcceptedTermsOfUse() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isLoggingServerActivity() {
		return false;
	}

	public String getUserAgent() {
		return "Mock Upload/1.0";
	}

}
