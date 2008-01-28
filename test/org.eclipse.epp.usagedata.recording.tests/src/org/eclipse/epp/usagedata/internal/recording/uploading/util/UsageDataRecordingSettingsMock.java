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

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.recording.filtering.AbstractUsageDataEventFilter;
import org.eclipse.epp.usagedata.internal.recording.filtering.UsageDataEventFilter;
import org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings;

public class UsageDataRecordingSettingsMock implements UploadSettings {
	private String uploadUrl;
	private UsageDataEventFilter filter = new AbstractUsageDataEventFilter() {
		public boolean includes(UsageDataEvent event) {
			return true;
		}		
	};

	public String getUserId() {
		return "bogus";
	}

	public String getWorkspaceId() {
		return "bogus";
	}

	public boolean isLoggingServerActivity() {
		return false;
	}

	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;		
	}
	
	public String getUploadUrl() {
		return uploadUrl;
	}
	
	public boolean hasUserAcceptedTermsOfUse() {
		return true;
	}
	
	public boolean isEnabled() {
		return true;
	}

	public UsageDataEventFilter getFilter() {
		return filter;
	}

	public String getUserAgent() {
		return "MockUpload/1.0";
	}
}