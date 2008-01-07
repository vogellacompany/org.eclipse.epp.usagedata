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
package org.eclipse.epp.usagedata.ui.uploaders;

import org.eclipse.epp.usagedata.recording.Activator;
import org.eclipse.epp.usagedata.recording.settings.UsageDataRecordingSettings;
import org.eclipse.epp.usagedata.recording.uploading.AbstractUploader;
import org.eclipse.epp.usagedata.recording.uploading.BasicUploader;
import org.eclipse.epp.usagedata.recording.uploading.UploadListener;
import org.eclipse.epp.usagedata.recording.uploading.UploadParameters;
import org.eclipse.epp.usagedata.recording.uploading.UploadResult;
import org.eclipse.epp.usagedata.ui.wizards.AskUserUploaderWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class AskUserUploader extends AbstractUploader {
	public static final int UPLOAD_NOW = 0;
	public static final int UPLOAD_ALWAYS = 1;
	public static final int DONT_UPLOAD = 2;
	public static final int NEVER_UPLOAD = 3;
	
	private BasicUploader basicUploader;
	private UploadParameters parameters;
	private WizardDialog dialog;

	private int action = UPLOAD_NOW;

	public void startUpload(UploadParameters parameters) {
		this.parameters = parameters;
		if (getSettings().shouldAskBeforeUploading()) {
			openUploadWizard();
		} else {
			startBasicUpload();
		}
	}

	private void openUploadWizard() {
		final AskUserUploaderWizard wizard = new AskUserUploaderWizard(AskUserUploader.this);
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				dialog = new WizardDialog(getShell(), wizard);
				dialog.setBlockOnOpen(false);
				dialog.open();
			}

			private Shell getShell() {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			}
		});
	}

	private UsageDataRecordingSettings getSettings() {
		return Activator.getDefault().getSettings();
	}

	public synchronized boolean isUploadInProgress() {
		if (isWizardOpen()) return true;
		if (basicUploader != null) {
			return basicUploader.isUploadInProgress();
		}
		return false;
	}

	private boolean isWizardOpen() {
		if (dialog == null) return false;
		return dialog.getShell().isVisible();
	}

	public synchronized void cancel() {
		dialog = null;
		fireUploadComplete(new UploadResult(UploadResult.CANCELLED));
	}

	public synchronized void execute() {
		dialog = null;
		startBasicUpload();
	}
	
	private void startBasicUpload() {
		basicUploader = new BasicUploader();
		basicUploader.addUploadListener(new UploadListener() {
			public void uploadComplete(UploadResult result) {
				fireUploadComplete(result);
				basicUploader = null;
			}
		});
		basicUploader.startUpload(parameters);
	}

	public void setAction(int action) {
		this.action = action;
	}

	public int getAction() {
		return action;
	}
}
