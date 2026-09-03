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
package org.eclipse.epp.usagedata.internal.recording.settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.epp.usagedata.internal.recording.UsageDataRecordingActivator;
import org.eclipse.epp.usagedata.internal.recording.filtering.PreferencesBasedFilter;
import org.eclipse.epp.usagedata.internal.recording.filtering.UsageDataEventFilter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

/**
 * This class provides a convenient location to find the settings
 * for this bundle. Some settings are in the preferences; others
 * are found in system properties. Still more are simply provided
 * as constant values.
 * 
 * @author Wayne Beaton
 *
 */
public class UsageDataRecordingSettings implements UploadSettings {

	private static final String DEFAULT_ID = "unknown"; //$NON-NLS-1$

	private static final String UPLOAD_FILE_PREFIX = "upload"; //$NON-NLS-1$

	public static final String UPLOAD_PERIOD_KEY = UsageDataRecordingActivator.PLUGIN_ID + ".period"; //$NON-NLS-1$
	public static final String LAST_UPLOAD_KEY = UsageDataRecordingActivator.PLUGIN_ID + ".last-upload"; //$NON-NLS-1$
	public static final String LAST_ATTEMPT_KEY = UsageDataRecordingActivator.PLUGIN_ID + ".last-attempt"; //$NON-NLS-1$
	public static final String ASK_TO_UPLOAD_KEY = UsageDataRecordingActivator.PLUGIN_ID + ".ask"; //$NON-NLS-1$
	public static final String UPLOAD_MODE_KEY = UsageDataRecordingActivator.PLUGIN_ID + ".upload-mode"; //$NON-NLS-1$
	public static final String LOG_SERVER_ACTIVITY_KEY = UsageDataRecordingActivator.PLUGIN_ID + ".log-server"; //$NON-NLS-1$
	public static final String FILTER_ECLIPSE_BUNDLES_ONLY_KEY = UsageDataRecordingActivator.PLUGIN_ID + ".filter-eclipse-only"; //$NON-NLS-1$
	public static final String FILTER_PATTERNS_KEY = UsageDataRecordingActivator.PLUGIN_ID + ".filter-patterns"; //$NON-NLS-1$
	public static final String FILTER_BUNDLE_EVENTS_KEY = UsageDataRecordingActivator.PLUGIN_ID + ".filter-bundle-events"; //$NON-NLS-1$
	
	public static final String UPLOAD_URL_KEY = UsageDataRecordingActivator.PLUGIN_ID + ".upload-url"; //$NON-NLS-1$
	
	/**
	 * Upload modes: ask the user before uploading, upload silently in the
	 * background, or never upload automatically (the user must trigger the
	 * upload explicitly, e.g. via the "Upload Now" button).
	 */
	public static final int UPLOAD_MODE_ASK = 0;
	public static final int UPLOAD_MODE_AUTOMATIC = 1;
	public static final int UPLOAD_MODE_MANUAL = 2;
	
	public static final int PERIOD_REASONABLE_MINIMUM = 15 * 60 * 1000; // 15 minutes
	static final int UPLOAD_PERIOD_DEFAULT = 5 * 24 * 60 * 60 * 1000; // five days
	public static final String UPLOAD_URL_DEFAULT = "https://www.example.com/upload.php"; //$NON-NLS-1$
	static final boolean ASK_TO_UPLOAD_DEFAULT = true;
	public static final int UPLOAD_MODE_DEFAULT = UPLOAD_MODE_ASK;

	/**
	 * Minimum time between upload attempts. Failed attempts are retried with
	 * an exponentially growing delay between {@link #RETRY_DELAY_BASE} and
	 * {@link #RETRY_DELAY_MAX}.
	 */
	static final long RETRY_DELAY_BASE = PERIOD_REASONABLE_MINIMUM;
	static final long RETRY_DELAY_MAX = 24 * 60L * 60L * 1000L; // 24 hours
	private static final int RETRY_DELAY_SHIFT_LIMIT = 7;

	/**
	 * Staged upload files older than this are deleted without being sent.
	 */
	public static final int UPLOAD_FILE_RETENTION_DAYS = 90;

	/**
	 * Staged upload files are deleted, oldest first, once they take up more
	 * than this in total.
	 */
	public static final long UPLOAD_DIRECTORY_MAX_BYTES = 10L * 1024L * 1024L;

	/**
	 * Bundle lifecycle events are filtered out of uploads by default. Starting
	 * an IDE reports every bundle it activates, which is the great majority of
	 * what gets recorded and says far less about how anyone works than the
	 * commands, editors and views it buries.
	 */
	public static final boolean FILTER_BUNDLE_EVENTS_DEFAULT = true;

	private int consecutiveFailedAttempts = 0;

	private PreferencesBasedFilter filter = new PreferencesBasedFilter();

	/**
	 * First if the system property {@value #UPLOAD_PERIOD_KEY} has been set,
	 * use that value. Next, check to see if there is a value stored (same key)
	 * in the preferences store. Finally, use the default value,
	 * {@value #UPLOAD_PERIOD_DEFAULT}. If the obtained value is deemed to be
	 * unreasonable (less than {@value #PERIOD_REASONABLE_MINIMUM}), that a
	 * reasonable minimum value is returned instead.
	 * 
	 * @return
	 */
	public long getPeriodBetweenUploads() {
		long period = 0L;
		if (System.getProperties().containsKey(UPLOAD_PERIOD_KEY)) {
			String value = System.getProperty(UPLOAD_PERIOD_KEY);
			try {
				period = Long.valueOf(value);
			} catch (NumberFormatException e) {
				// If we can't get it from this source, we'll pick it up some
				// other way. Long the problem and move on.
				UsageDataRecordingActivator.getDefault().log(IStatus.WARNING,
						e, "The UsageDataUploader cannot parse the %1$s system property (\"%2$s\"", UPLOAD_PERIOD_KEY, value); //$NON-NLS-1$
			}
		} else if (getPreferencesStore().contains(UPLOAD_PERIOD_KEY)) {
			period = getPreferencesStore().getLong(UPLOAD_PERIOD_KEY);
		} else {
			period = UPLOAD_PERIOD_DEFAULT;
		}

		if (period < PERIOD_REASONABLE_MINIMUM)
			period = PERIOD_REASONABLE_MINIMUM;

		return period;
	}

	/**
	 * The last upload time is stored in the preferences. If no value is
	 * currently set, the current time is used (and is stored for the next time
	 * we're asked). Time is expressed in milliseconds. There is no mechanism
	 * for overriding this value.
	 * 
	 * @return
	 */
	public long getLastUploadTime() {
		if (getPreferencesStore().contains(LAST_UPLOAD_KEY)) {
			return getPreferencesStore().getLong(LAST_UPLOAD_KEY);
		}
		long period = System.currentTimeMillis();
		getPreferencesStore().setValue(LAST_UPLOAD_KEY, period);
		UsageDataRecordingActivator.getDefault().savePluginPreferences();

		return period;
	}

	/**
	 * This method answers <code>true</code> if enough time has passed since
	 * the last upload to warrant starting a new one, and if enough time has
	 * passed since the last upload attempt. The latter keeps failed attempts
	 * from being retried more often than {@link #getRetryDelay()} suggests.
	 * If an upload has not yet occurred, it answers <code>true</code> if the
	 * required amount of time has passed since the first time this method was
	 * called. It answers <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if it is time to upload; <code>false</code>
	 *         otherwise.
	 */
	public boolean isTimeToUpload() {
		if (PlatformUI.getWorkbench().isClosing())
			return false;
		long now = System.currentTimeMillis();
		if (now - getLastAttemptTime() < getRetryDelay())
			return false;
		return now - getLastUploadTime() > getPeriodBetweenUploads();
	}

	/**
	 * This method answers the time of the last upload attempt, regardless of
	 * whether that attempt succeeded. Time is expressed in milliseconds. It
	 * answers <code>0</code> if no attempt has been recorded.
	 * 
	 * @return the time of the last upload attempt.
	 */
	public long getLastAttemptTime() {
		if (getPreferencesStore().contains(LAST_ATTEMPT_KEY)) {
			return getPreferencesStore().getLong(LAST_ATTEMPT_KEY);
		}
		return 0L;
	}

	private void stampAttemptTime() {
		getPreferencesStore().setValue(LAST_ATTEMPT_KEY, System.currentTimeMillis());
	}

	/**
	 * This method answers how long to wait before trying another upload after
	 * a failed attempt. The delay doubles with each consecutive failure,
	 * starting at {@link #RETRY_DELAY_BASE} and capped at
	 * {@link #RETRY_DELAY_MAX}.
	 * 
	 * @return the delay in milliseconds.
	 */
	public long getRetryDelay() {
		int shift = Math.min(consecutiveFailedAttempts, RETRY_DELAY_SHIFT_LIMIT);
		long delay = RETRY_DELAY_BASE << shift;
		if (delay > RETRY_DELAY_MAX) delay = RETRY_DELAY_MAX;
		return delay;
	}

	/**
	 * Records that an upload succeeded: the last upload time is set, the
	 * retry backoff is reset, and the current time is stamped as the latest
	 * attempt.
	 */
	public void recordSuccessfulUpload() {
		consecutiveFailedAttempts = 0;
		stampAttemptTime();
		setLastUploadTime();
	}

	/**
	 * Records that an upload attempt failed (e.g. because the user was
	 * offline). The staged files are kept and retried once the retry delay
	 * has passed.
	 */
	public void recordFailedUploadAttempt() {
		if (consecutiveFailedAttempts < RETRY_DELAY_SHIFT_LIMIT + 1)
			consecutiveFailedAttempts++;
		stampAttemptTime();
	}

	/**
	 * Records that the user was asked to upload and declined. Like the
	 * behaviour prior to upload tracking, this postpones the next automatic
	 * upload by a full period so the user is not asked again right away.
	 */
	public void recordDeclinedUpload() {
		consecutiveFailedAttempts = 0;
		stampAttemptTime();
		setLastUploadTime();
	}

	/** 
	 * This method returns the {@link File} where usage data events should be persisted.
	 *  
	 * @return the {@link File} where usage data events are persisted.
	 */
	public File getEventFile() {
		return new File(getWorkingDirectory(), "usagedata.csv"); //$NON-NLS-1$
	}

	/**
	 * When it's time to start uploading the usage data, the file that's used
	 * to persist the data is moved (renamed) and a new file is created. The
	 * moved file is then uploaded to the server. This method finds an appropriate
	 * destination for the moved file. The destination {@link File} will be in the
	 * bundle's state location, but will not actually exist in the file system.
	 * 
	 * @return a destination {@link File} for the move operation. 
	 */
	public File computeDestinationFile() {
		int index = 0;
		File parent = getWorkingDirectory();
		File file = null;
		// TODO Unlikely (impossible?), but what if this spins forever.
		while (true) {
			file = new File(parent, UPLOAD_FILE_PREFIX + index++ + ".csv"); //$NON-NLS-1$
			if (!file.exists())
				return file;
		}
	}

	/**
	 * This method returns an identifier for the workstation. This value
	 * is common to all workspaces on a single machine. The value
	 * is persisted (if possible) in a hidden file in the users's working 
	 * directory. If an existing file cannot be read, or a new file cannot
	 * be written, this method returns "unknown".
	 * 
	 * @return an identifier for the workstation.
	 */
	public String getUserId() {
		return getExistingOrGenerateId(new File(System.getProperty("user.home")), "." + UsageDataRecordingActivator.PLUGIN_ID //$NON-NLS-1$ //$NON-NLS-2$
				+ ".userId"); //$NON-NLS-1$
	}

	/**
	 * This method returns an identifier for the workspace. This value is unique
	 * to the workspace. It is persisted (if possible) in a hidden file in the bundle's
	 * state location.If an existing file cannot be read, or a new file cannot
	 * be written, this method returns "unknown".
	 * 
	 * @return an identifier for the workspace.
	 */
	public String getWorkspaceId() {
		return getExistingOrGenerateId(getWorkingDirectory(), "." //$NON-NLS-1$
				+ UsageDataRecordingActivator.PLUGIN_ID + ".workspaceId"); //$NON-NLS-1$
	}


	/**
	 * This method answers whether or not we want to ask the server to 
	 * provide a log of activity. This method only answers <code>true</code>
	 * if the "{@value #LOG_SERVER_ACTIVITY_KEY}" system property is set
	 * to "true". This is mostly useful for debugging.
	 * 
	 * @return true if we're logging, false otherwise.
	 * 
	 * @see UploadSettings#isLoggingServerActivity()
	 */
	public boolean isLoggingServerActivity() {
		return "true".equals(System.getProperty(LOG_SERVER_ACTIVITY_KEY)); //$NON-NLS-1$
	}

	/**
	 * This method answers an array containing the files that are available
	 * for uploading.
	 * 
	 * @return
	 */
	public File[] getUsageDataUploadFiles() {
		return getWorkingDirectory().listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(UPLOAD_FILE_PREFIX);
			}

		});
	}

	/**
	 * This method deletes staged upload files older than
	 * {@link #UPLOAD_FILE_RETENTION_DAYS}, then deletes the oldest of what is
	 * left until the rest fit in {@link #UPLOAD_DIRECTORY_MAX_BYTES}, so that
	 * data collected during long offline periods does not accumulate
	 * indefinitely.
	 */
	public void pruneOldUploadFiles() {
		File[] files = getUsageDataUploadFiles();
		if (files == null) return;

		long cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(UPLOAD_FILE_RETENTION_DAYS);
		List<File> remaining = new ArrayList<File>();
		long total = 0;
		for (File file : files) {
			if (file.lastModified() < cutoff) {
				file.delete();
				continue;
			}
			remaining.add(file);
			total += file.length();
		}

		if (total <= UPLOAD_DIRECTORY_MAX_BYTES) return;

		// The oldest data is the least interesting, so it goes first.
		remaining.sort(Comparator.comparingLong(File::lastModified));
		for (File file : remaining) {
			if (total <= UPLOAD_DIRECTORY_MAX_BYTES) return;
			long length = file.length();
			if (file.delete()) total -= length;
		}
	}

	/**
	 * This method sets the {@value #LAST_UPLOAD_KEY} property to the
	 * current time.
	 */
	public void setLastUploadTime() {
		getPreferencesStore().setValue(LAST_UPLOAD_KEY, System.currentTimeMillis());
		UsageDataRecordingActivator.getDefault().savePluginPreferences();
	}
	
	/**
	 * <p>
	 * This method either finds an existing id or generates a new one. The id is
	 * stored in file system at the given path and file. If the file exists, the
	 * id is extracted from it. If the file does not exist, or if an id cannot
	 * be determined from its contents, a new id is generated and then stored in
	 * the file. If the file cannot be read or written (i.e. an IOException
	 * occurs), the operation is aborted and "unknown" is returned.
	 * </p>
	 * 
	 * @param directory
	 *           the directory that will contain the stored id.
	 * @param fileName
	 *            name of the file containing the id.
	 * @return a globally unique id.
	 */
	private String getExistingOrGenerateId(File directory, String fileName) {
		if (!directory.exists()) return DEFAULT_ID;
		if (!directory.isDirectory()) {
		} // TODO Think of something else
		File file = new File(directory, fileName);
		if (file.exists()) {
			FileReader reader = null;
			try {
				reader = new FileReader(file);
				char[] buffer = new char[256];
				int count = reader.read(buffer);
				// TODO what if the file can't be read, or if there is no
				// content?
				return new String(buffer, 0, count);
			} catch (IOException e) {
				handleCannotReadFileException(file, e);
				return DEFAULT_ID;
			} finally {
				close(reader);
			}
		} else {
			String id = UUID.randomUUID().toString();
			FileWriter writer = null;
			try {
				// TODO What if there is a collection with another process?
				writer = new FileWriter(file);
				writer.write(id);
				return id;
			} catch (IOException e) {
				handleCannotReadFileException(file, e);
				return DEFAULT_ID;
			} finally {
				close(writer);
			}
		}
	}

	private void handleCannotReadFileException(File file, IOException e) {
		UsageDataRecordingActivator.getDefault().log(IStatus.WARNING,	e, "Cannot read the existing id from %1$s; using the default.", file.toString()); //$NON-NLS-1$
	}

	private IPreferenceStore getPreferencesStore() {
		return UsageDataRecordingActivator.getDefault().getPreferenceStore();
	}
	
	protected File getWorkingDirectory() {
		return UsageDataRecordingActivator.getDefault().getStateLocation().toFile();
	}
	
	/**
	 * Convenience method for closing a {@link Writer} that could possibly be
	 * <code>null</code>.
	 * 
	 * @param writer
	 *            the {@link Writer} to close.
	 */
	private void close(Writer writer) {
		if (writer == null)
			return;
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Handle exception
		}
	}

	/**
	 * Convenience method for closing a {@link Reader} that could possibly be
	 * <code>null</code>.
	 * 
	 * @param reader
	 *            the {@link Reader} to close.
	 */
	private void close(Reader reader) {
		if (reader == null)
			return;
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Handle exception
		}
	}

	public boolean shouldAskBeforeUploading() {
		return getUploadMode() == UPLOAD_MODE_ASK;
	}

	/**
	 * This method answers how the user wants uploads to be triggered:
	 * {@link #UPLOAD_MODE_ASK} (prompt before every upload),
	 * {@link #UPLOAD_MODE_AUTOMATIC} (upload silently in the background), or
	 * {@link #UPLOAD_MODE_MANUAL} (never upload automatically). The
	 * "{@value #ASK_TO_UPLOAD_KEY}" system property, if set, still overrides
	 * everything; installs that only carry the older "{@value #ASK_TO_UPLOAD_KEY}"
	 * preference are migrated to the matching mode.
	 * 
	 * @return one of {@link #UPLOAD_MODE_ASK}, {@link #UPLOAD_MODE_AUTOMATIC},
	 *         or {@link #UPLOAD_MODE_MANUAL}.
	 */
	public int getUploadMode() {
		if (System.getProperties().containsKey(ASK_TO_UPLOAD_KEY)) {
			return "true".equals(System.getProperty(ASK_TO_UPLOAD_KEY)) ? UPLOAD_MODE_ASK : UPLOAD_MODE_AUTOMATIC; //$NON-NLS-1$
		}
		IPreferenceStore store = getPreferencesStore();
		if (hasExplicitValue(store, UPLOAD_MODE_KEY)) {
			int mode = store.getInt(UPLOAD_MODE_KEY);
			if (mode >= UPLOAD_MODE_ASK && mode <= UPLOAD_MODE_MANUAL) return mode;
		}
		if (hasExplicitValue(store, ASK_TO_UPLOAD_KEY)) {
			return store.getBoolean(ASK_TO_UPLOAD_KEY) ? UPLOAD_MODE_ASK : UPLOAD_MODE_AUTOMATIC;
		}
		return UPLOAD_MODE_DEFAULT;
	}

	private boolean hasExplicitValue(IPreferenceStore store, String key) {
		return store.contains(key) && !store.isDefault(key);
	}

	public void setUploadMode(int mode) {
		getPreferencesStore().setValue(UPLOAD_MODE_KEY, mode);
		UsageDataRecordingActivator.getDefault().savePluginPreferences();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings#getFilter()
	 */
	public UsageDataEventFilter getFilter() {
		return filter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings#hasUserAcceptedTermsOfUse()
	 */
	public boolean hasUserAcceptedTermsOfUse() {
		return getCaptureSettings().hasUserAcceptedTermsOfUse();
	}

	public void setUserAcceptedTermsOfUse(boolean value) {
		getCaptureSettings().setUserAcceptedTermsOfUse(value);
		UsageDataRecordingActivator.getDefault().savePluginPreferences();
	}
	
	private UsageDataCaptureSettings getCaptureSettings() {
		return org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator.getDefault().getSettings();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings#isEnabled()
	 */
	public boolean isEnabled() {
		return getCaptureSettings().isEnabled();
	}

	public void setAskBeforeUploading(boolean value) {
		setUploadMode(value ? UPLOAD_MODE_ASK : UPLOAD_MODE_AUTOMATIC);
	}

	public void setEnabled(boolean value) {
		getCaptureSettings().setEnabled(value);
		UsageDataRecordingActivator.getDefault().savePluginPreferences();
	}

	public void dispose() {
		filter.dispose();
	}

	public String getUserAgent() {
		return "Eclipse UDC/" + UsageDataRecordingActivator.getDefault().getBundle().getHeaders().get("Bundle-Version"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getUploadUrl() {
		if (System.getProperties().containsKey(UPLOAD_URL_KEY)) {
			return System.getProperty(UPLOAD_URL_KEY);
		}
		if (getPreferencesStore().contains(UPLOAD_URL_KEY)) {
			return getPreferencesStore().getString(UPLOAD_URL_KEY);
		}
		return UPLOAD_URL_DEFAULT;
	}

	public void setUploadUrl(String url) {
		getPreferencesStore().setValue(UPLOAD_URL_KEY, url);
		UsageDataRecordingActivator.getDefault().savePluginPreferences();
	}

}
