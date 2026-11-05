/*******************************************************************************
 * Copyright (c) 2026 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.recording;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;
import org.eclipse.epp.usagedata.internal.recording.uploading.UploadManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * The state of this bundle, for the classes the extension registry creates,
 * which cannot have it injected. {@link RecordingStarter} runs the life cycle.
 */
public final class UsageDataRecording {

	public static final String PLUGIN_ID = "org.eclipse.epp.usagedata.recording"; //$NON-NLS-1$

	private static final ILog LOG = Platform.getLog(UsageDataRecording.class);
	private static final ScopedPreferenceStore PREFERENCES = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
	private static final UploadManager UPLOAD_MANAGER = new UploadManager();
	private static final UsageDataRecorder RECORDER = new UsageDataRecorder();

	private static UsageDataRecordingSettings settings;

	private UsageDataRecording() {
	}

	public static synchronized UsageDataRecordingSettings getSettings() {
		if (settings == null) settings = new UsageDataRecordingSettings();
		return settings;
	}

	static synchronized void disposeSettings() {
		if (settings == null) return;
		settings.dispose();
		settings = null;
	}

	public static UploadManager getUploadManager() {
		return UPLOAD_MANAGER;
	}

	public static UsageDataRecorder getRecorder() {
		return RECORDER;
	}

	public static IPreferenceStore getPreferenceStore() {
		return PREFERENCES;
	}

	public static void savePreferences() {
		try {
			PREFERENCES.save();
		} catch (IOException e) {
			log(IStatus.WARNING, e, "Cannot save the preferences."); //$NON-NLS-1$
		}
	}

	public static File getStateLocation() {
		return Platform.getStateLocation(getBundle()).toFile();
	}

	public static String getVersion() {
		return getBundle().getHeaders().get("Bundle-Version"); //$NON-NLS-1$
	}

	private static Bundle getBundle() {
		return FrameworkUtil.getBundle(UsageDataRecording.class);
	}

	public static void log(IStatus status) {
		LOG.log(status);
	}

	public static void log(int status, String message, Object... arguments) {
		log(status, (Exception) null, message, arguments);
	}

	public static void log(int status, Exception exception, String message, Object... arguments) {
		log(status, exception, String.format(message, arguments));
	}

	public static void log(int status, Exception e, String message) {
		LOG.log(new Status(status, PLUGIN_ID, message, e));
	}
}
