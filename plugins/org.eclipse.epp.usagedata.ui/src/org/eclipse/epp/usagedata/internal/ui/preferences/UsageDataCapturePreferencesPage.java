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

import org.eclipse.epp.usagedata.internal.gathering.Activator;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class UsageDataCapturePreferencesPage extends PreferencePage
	implements IWorkbenchPreferencePage {
	
	private Button captureEnabledCheckbox;

	public UsageDataCapturePreferencesPage() {
		setDescription("The Usage Data Collector collects information about how individuals are using the Eclipse platform. The intent is to use this data to help committers and organizations better understand how developers are using Eclipse.");
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		composite.setLayout(new GridLayout());
		
		createGeneralInformationArea(composite);
		
		Label filler = new Label(parent, SWT.NONE);
		filler.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		
		initialize();
		
		return composite;
	}

	private void initialize() {
		captureEnabledCheckbox.setSelection(getCapturePreferences().getBoolean(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY));
	}

	@Override
	public boolean performOk() {
		getCapturePreferences().setValue(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY, captureEnabledCheckbox.getSelection());
	
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		captureEnabledCheckbox.setSelection(getCapturePreferences().getDefaultBoolean(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY));

		super.performDefaults();
	}

	private void createGeneralInformationArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		
		composite.setLayout(new GridLayout());
		
		captureEnabledCheckbox = new Button(composite, SWT.CHECK | SWT.LEFT);
		captureEnabledCheckbox.setText("Enable capture"); 
	}


	private IPreferenceStore getCapturePreferences() {
		return org.eclipse.epp.usagedata.internal.gathering.Activator.getDefault().getPreferenceStore();
	}
}
