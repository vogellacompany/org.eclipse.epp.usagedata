package org.eclipse.epp.usagedata.internal.recording.uploading;

import org.eclipse.core.runtime.ListenerList;

public abstract class AbstractUploader implements Uploader {

	private ListenerList uploadListeners = new ListenerList();

	public void addUploadListener(UploadListener listener) {
		uploadListeners.add(listener);
	}

	public void removeUploadListener(UploadListener listener) {
		uploadListeners.remove(listener);
	}
	
	protected void fireUploadComplete(UploadResult result) {
		for (Object listener : uploadListeners.getListeners()) {
			((UploadListener)listener).uploadComplete(result);
		}
	}
}
