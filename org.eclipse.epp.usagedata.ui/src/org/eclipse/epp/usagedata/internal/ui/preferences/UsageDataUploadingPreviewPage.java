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
package org.eclipse.epp.usagedata.internal.ui.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.epp.usagedata.internal.recording.UsageDataRecordingActivator;
import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;
import org.eclipse.epp.usagedata.internal.recording.uploading.UploadParameters;
import org.eclipse.epp.usagedata.internal.ui.preview.UploadPreview;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class UsageDataUploadingPreviewPage extends PreferencePage
	implements IWorkbenchPreferencePage {

	public UsageDataUploadingPreviewPage() {
		noDefaultAndApplyButton();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		UploadParameters parameters = new UploadParameters();
		UsageDataRecordingSettings settings = getSettings();
		parameters.setSettings(settings);
		parameters.setFiles(getRecordedFiles(settings));
		new UploadPreview(parameters).createControl(composite);
		return composite;
	}

	/**
	 * The staged upload files plus the live event file, flushed first, so the
	 * page shows what has been recorded rather than only what is staged.
	 */
	private File[] getRecordedFiles(UsageDataRecordingSettings settings) {
		UsageDataRecordingActivator.getDefault().getRecorder().flush();
		List<File> files = new ArrayList<File>(Arrays.asList(settings.getUsageDataUploadFiles()));
		if (settings.getEventFile().exists()) files.add(settings.getEventFile());
		return files.toArray(new File[files.size()]);
	}

	protected UsageDataRecordingSettings getSettings() {
		return UsageDataRecordingActivator.getDefault().getSettings();
	}
}