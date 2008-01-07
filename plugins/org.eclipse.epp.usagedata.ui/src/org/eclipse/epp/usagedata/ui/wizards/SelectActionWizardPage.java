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
package org.eclipse.epp.usagedata.ui.wizards;

import org.eclipse.epp.usagedata.gathering.Activator;
import org.eclipse.epp.usagedata.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.epp.usagedata.ui.uploaders.AskUserUploader;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

public class SelectActionWizardPage extends WizardPage {

	private static final int WIDTH_HINT = 500;
	
	private final AskUserUploader uploader;
	private Button neverUploadRadio;
	private Button dontUploadRadio;
	private Button uploadAlwaysRadio;
	private Button uploadNowRadio;

	public SelectActionWizardPage(AskUserUploader uploader) {
		super("wizardPage");
		this.uploader = uploader;
		setTitle("Usage Data Upload");
		setDescription("It's time to upload your usage data.");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		createText(composite, "Eclipse has been monitoring how you have been using the workbench and would now like to upload the gathered information to a server at the Eclipse Foundation.");
		createSpacer(composite);		
						
		createUploadNowRadio(composite);		
		createSpacer(composite);
		
		createUploadAlwaysRadio(composite);
		createSpacer(composite);
		
		createDontUploadRadio(composite);
		createSpacer(composite);
		
		createNeverUploadRadio(composite);
		createSpacer(composite);
		
		FormText text = createFormText(composite, getTermsText());
		text.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent event) {
				((AskUserUploaderWizard)getWizard()).showTermsPage();
			}
		});
	
		setControl(composite);
	}

	private String getTermsText() {
		if (getGatheringSettings().hasUserAcceptedTermsOfUse()) 
			return "<form><p>To learn more about how this data will be used by the Eclipse Foundation, please review the <a href=\"terms\">Terms of Use</a> on the next page of this wizard.</p></form>";
		else
			return "<form><p>Please review the <a href=\"terms\">Terms of Use</a>.</p></form>";
	}

	private UsageDataCaptureSettings getGatheringSettings() {
		return Activator.getDefault().getSettings();
	}

	private void createSpacer(Composite parent) {
		Label spacer = new Label(parent, SWT.NONE);
		GridData layoutData = new GridData();
		layoutData.heightHint = 5;
		spacer.setLayoutData(layoutData);
	}

	private void createUploadNowRadio(Composite parent) {
		uploadNowRadio = createRadio(parent, "Upload Now",AskUserUploader.UPLOAD_NOW);
		createDescriptionText(parent, "Upload the usage data now. Ask before uploading again.");
	}

	private void createUploadAlwaysRadio(Composite parent) {
		uploadAlwaysRadio = createRadio(parent, "Upload Always", AskUserUploader.UPLOAD_ALWAYS);
		createDescriptionText(parent, "Upload the usage data now. Don't ask next time; just do the upload in the background. Note that you can change this setting in the preferences.");
	}

	private void createDontUploadRadio(Composite parent) {
		dontUploadRadio = createRadio(parent, "Don't upload", AskUserUploader.DONT_UPLOAD);		
		createDescriptionText(parent, "Do not upload usage data at this time. You will be asked to do the upload later.");
	}

	private void createNeverUploadRadio(Composite parent) {
		neverUploadRadio = createRadio(parent, "Never Upload",AskUserUploader.NEVER_UPLOAD);	
		createDescriptionText(parent, "Never upload the usage data.");		
	}

	private Button createRadio(Composite parent, String label, final int action) {
		Button radio = new Button(parent, SWT.RADIO);
		radio.setText(label);
		radio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getContainer().updateButtons();
				uploader.setAction(action);
			}
		});
		radio.setSelection(uploader.getAction() == action);
		
		return radio;
	}
	
	private void createDescriptionText(Composite parent, String string) {
		createText(parent, string, 25);
	}
	
	private void createText(Composite parent, String string) {
		createText(parent, string, 0);
	}
	
	private void createText(Composite parent, String string, int indent) {
		Label text = new Label(parent, SWT.WRAP);
		text.setText(string);
		
		GridData layoutData = new GridData();
		layoutData.horizontalIndent = indent;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.widthHint = WIDTH_HINT;
		text.setLayoutData(layoutData);
	}

	private FormText createFormText(Composite parent, String string) {
		FormText text = new FormText(parent, SWT.WRAP);
		text.setText(string, true, true);
		
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.widthHint = WIDTH_HINT;
		text.setLayoutData(layoutData);
		
		return text;
	}

	@Override
	public boolean isPageComplete() {
		if (uploadAlwaysRadio.getSelection()) return true;
		if (uploadNowRadio.getSelection()) return true;
		if (neverUploadRadio.getSelection()) return true;
		if (dontUploadRadio.getSelection()) return true;
		
		return false;
	}
}