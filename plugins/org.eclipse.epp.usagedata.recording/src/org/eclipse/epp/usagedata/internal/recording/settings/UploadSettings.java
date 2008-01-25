package org.eclipse.epp.usagedata.internal.recording.settings;

import org.eclipse.epp.usagedata.internal.recording.filtering.UsageDataEventFilter;

public interface UploadSettings {

	/**
	 * This method answers whether or not we want to ask the server to 
	 * provide a log of activity. This method only answers <code>true</code>
	 * if the "{@value #LOG_SERVER_ACTIVITY_KEY}" system property is set
	 * to "true". This is mostly useful for debugging.
	 * 
	 * @return
	 */
	public abstract boolean isLoggingServerActivity();

	/**
	 * This method returns the target URL for uploads.
	 * 
	 * @return the target URL for uploads.
	 */
	public abstract String getUploadUrl();

	public abstract UsageDataEventFilter getFilter();

	public abstract boolean hasUserAcceptedTermsOfUse();

	public abstract boolean isEnabled();

	public abstract String getUserId();

	public abstract String getWorkspaceId();

	public abstract String getUserAgent();

}