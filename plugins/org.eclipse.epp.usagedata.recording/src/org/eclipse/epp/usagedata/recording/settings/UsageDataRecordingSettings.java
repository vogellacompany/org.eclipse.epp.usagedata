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
package org.eclipse.epp.usagedata.recording.settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epp.usagedata.recording.Activator;
import org.eclipse.epp.usagedata.recording.uploading.BasicUploader;
import org.eclipse.epp.usagedata.recording.uploading.Uploader;
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
public class UsageDataRecordingSettings {

	private static final String DEFAULT_ID = "unknown";

	private static final String UPLOAD_FILE_PREFIX = "upload";

	public static final String UPLOAD_PERIOD_KEY = Activator.PLUGIN_ID + ".period";
	public static final String LAST_UPLOAD_KEY = Activator.PLUGIN_ID + ".last-upload";
	public static final String UPLOAD_URL_KEY = Activator.PLUGIN_ID + ".upload-url";
	public static final String ASK_TO_UPLOAD_KEY = Activator.PLUGIN_ID + ".ask";
	public static final String LOG_SERVER_ACTIVITY_KEY = Activator.PLUGIN_ID + ".log-server";

	public static final int PERIOD_REASONABLE_MINIMUM = 60000; 
	// TODO 15 * 60 * 1000; // 15 minutes
	static final int UPLOAD_PERIOD_DEFAULT = 1 * 24 * 60 * 60 * 1000; 
	// TODO 5 * 24 * 60 * 60 * 1000; // five days
	static final String UPLOAD_URL_DEFAULT = "http://cortez.eclipse.org/upload.php";
	static final boolean ASK_TO_UPLOAD_DEFAULT = true;

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
				Activator.getDefault().logException(
						"The UsageDataUploader cannot parse the " + UPLOAD_PERIOD_KEY
								+ " system property (\"" + value + "\").", e);
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
		Activator.getDefault().savePluginPreferences();

		return period;
	}

	/**
	 * This method answers <code>true</code> if enough time has passed since
	 * the last upload to warrant starting a new one. If an upload has not yet
	 * occurred, it answers <code>true</code> if the required amount of time
	 * has passed since the first time this method was called. It answers
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if it is time to upload; <code>false</code>
	 *         otherwise.
	 */
	public boolean isTimeToUpload() {
		if (PlatformUI.getWorkbench().isClosing())
			return false;
		return System.currentTimeMillis() - getLastUploadTime() > getPeriodBetweenUploads();
	}

	/** 
	 * This method returns the {@link File} where usage data events should be persisted.
	 *  
	 * @return the {@link File} where usage data events are persisted.
	 */
	public File getEventFile() {
		return new File(getWorkingDirectory(), "usagedata.csv");
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
			file = new File(parent, UPLOAD_FILE_PREFIX + index++ + ".csv");
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
		return getExistingOrGenerateId(new File(System.getProperty("user.home")), "." + Activator.PLUGIN_ID
				+ ".userId");
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
		return getExistingOrGenerateId(getWorkingDirectory(), "."
				+ Activator.PLUGIN_ID + ".workspaceId");
	}

	/**
	 * This method answers whether or not we want to ask the server to 
	 * provide a log of activity. This method only answers <code>true</code>
	 * if the "{@value #LOG_SERVER_ACTIVITY_KEY}" system property is set
	 * to "true". This is mostly useful for debugging.
	 * 
	 * @return
	 */
	public String isLoggingServerActivity() {
		return System.getProperty(LOG_SERVER_ACTIVITY_KEY);
	}

	/**
	 * This method answers an array containing the files that are available
	 * for uploading.
	 * 
	 * @return
	 */
	public File[] getUsageDataUploadFiles() {
		return getWorkingDirectory().listFiles(new FilenameFilter() {
			// TODO Sort out why the override is causing compiler errors on
			// build.
			// @Override
			public boolean accept(File dir, String name) {
				return name.startsWith(UPLOAD_FILE_PREFIX);
			}

		});
	}

	/**
	 * This method sets the {@value #LAST_UPLOAD_KEY} property to the
	 * current time.
	 */
	public void setLastUploadTime() {
		getPreferencesStore().setValue(LAST_UPLOAD_KEY, System.currentTimeMillis());
	}

	/**
	 * This method returns the target URL for uploads.
	 * 
	 * @return the target URL for uploads.
	 */
	public String getUploadUrl() {
		return getPropertyOrPreferenceString(UPLOAD_URL_KEY, UPLOAD_URL_DEFAULT);
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
				Activator.getDefault().logException(
						"Cannot read the existing id from " + file.toString()
								+ ", using the default", e);
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
				Activator.getDefault().logException(
						"Cannot write the generated id to " + file.toString()
								+ ", using the default", e);
				return DEFAULT_ID;
			} finally {
				close(writer);
			}
		}
	}

	private IPreferenceStore getPreferencesStore() {
		return Activator.getDefault().getPreferenceStore();
	}
	
	private File getWorkingDirectory() {
		return Activator.getDefault().getStateLocation().toFile();
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

	private String getPropertyOrPreferenceString(String key, String defaultValue) {
		if (System.getProperties().containsKey(key)) {
			return System.getProperty(key);
		} else if (getPreferencesStore().contains(key)) {
			return getPreferencesStore().getString(key);
		} else {
			return defaultValue;
		}
	}

	public boolean shouldAskBeforeUploading() {
		if (System.getProperties().containsKey(ASK_TO_UPLOAD_KEY)) {
			return "true".equals(System.getProperty(ASK_TO_UPLOAD_KEY));
		} else if (getPreferencesStore().contains(ASK_TO_UPLOAD_KEY)) {
			return getPreferencesStore().getBoolean(ASK_TO_UPLOAD_KEY);
		} else {
			return ASK_TO_UPLOAD_DEFAULT;
		}
	}

	/**
	 * This method returns the {@link Uploader} to use to upload data to the
	 * server. At present, this is implemented using the extension point
	 * registry. This separation is done so that a UI component can participate
	 * in the upload process without muddying the lines between model and view;
	 * a UI plug-in, might, for example, open an editor or dialogue box asking
	 * the user if it is okay to do the upload.
	 * <p>
	 * An extension point is not quite the right fit here as there is currently
	 * only provision for there being a single choice. It's a good choice, 
	 * because we want this to be lazy loaded. At some point, it may
	 * make sense to have multiple uploaders available and let the user
	 * (via preferences) select the one that they want to use (perhaps using
	 * a combo box or something. For now, that's probably just too complicated,
	 * and so the extension point is used with an understanding that there
	 * should only be one extension to it (in the event that there is more than
	 * one extension, the first one that we find is used).
	 * </p>
	 * 
	 * @return
	 */
	public Uploader getUploader() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(Activator.PLUGIN_ID + ".uploader");
		for (IConfigurationElement element : elements) {
			if ("uploader".equals(element.getName())) {
				try {
					Object uploader = element.createExecutableExtension("class");
					if (uploader instanceof Uploader) {
						return (Uploader) uploader;
					}
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(e.getStatus());
				}
			}
		}
		return new BasicUploader();
	}
}
