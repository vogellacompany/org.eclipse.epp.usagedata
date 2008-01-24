package org.eclipse.epp.usagedata.internal.ui.preview.util;

import org.eclipse.epp.usagedata.internal.recording.filtering.UsageDataEventFilter;
import org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings;

public class MockUploadSettings implements UploadSettings {
	UsageDataEventFilter filter = new MockUsageDataEventFilter();
	
	public UsageDataEventFilter getFilter() {
		return filter;
	}

	public String getUploadUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getWorkspaceId() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasUserAcceptedTermsOfUse() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLoggingServerActivity() {
		// TODO Auto-generated method stub
		return false;
	}

}
