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
package org.eclipse.epp.usagedata.ui.preferences;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.epp.usagedata.recording.Activator;
import org.eclipse.epp.usagedata.recording.settings.UsageDataRecordingSettings;
import org.eclipse.epp.usagedata.ui.editors.myusage.MyUsageDataEditor;
import org.eclipse.epp.usagedata.ui.editors.myusage.MyUsageDataEditorInput;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class UsageDataUploadingPreferencesPage extends PreferencePage
	implements IWorkbenchPreferencePage {

	private static final int MILLISECONDS_IN_ONE_DAY = 24 * 60 * 60 * 1000;

	private static final long MINIMUM_PERIOD_IN_DAYS = UsageDataRecordingSettings.PERIOD_REASONABLE_MINIMUM / MILLISECONDS_IN_ONE_DAY;
	private static final long MAXIMUM_PERIOD_IN_DAYS = 90;
	
	private Text uploadUrlText;
	private Text uploadPeriodText;
	private Label label;
	private Text lastUploadText;

	private Button askBeforeUploadingCheckbox;


	public UsageDataUploadingPreferencesPage() {
		setDescription("Usage data collection");
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
		createUploadingArea(composite);
		createButtonsArea(composite);
		
		Label filler = new Label(parent, SWT.NONE);
		filler.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		
		initialize();
		
		return composite;
	}


	private void initialize() {
		askBeforeUploadingCheckbox.setSelection(getRecordingPreferences().getBoolean(UsageDataRecordingSettings.ASK_TO_UPLOAD_KEY));		
		uploadUrlText.setText(getRecordingPreferences().getString(UsageDataRecordingSettings.UPLOAD_URL_KEY));
		uploadPeriodText.setText(String.valueOf(getRecordingPreferences().getLong(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY) / MILLISECONDS_IN_ONE_DAY));
		
		lastUploadText.setText(getLastUploadDateAsString());
	}
	


	@Override
	public boolean performOk() {		
		getRecordingPreferences().setValue(UsageDataRecordingSettings.ASK_TO_UPLOAD_KEY, askBeforeUploadingCheckbox.getSelection());		
		getRecordingPreferences().setValue(UsageDataRecordingSettings.UPLOAD_URL_KEY, uploadUrlText.getText());
		getRecordingPreferences().setValue(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY, Long.valueOf(uploadPeriodText.getText()) * MILLISECONDS_IN_ONE_DAY);
		
		return super.performOk();
	}
	
	@Override
	public boolean isValid() {
		if (!isValidUploadUrl(uploadUrlText.getText())) return false;
		if (!isValidUploadPeriod(uploadPeriodText.getText())) return false;
		return true;
	}

	@Override
	protected void performDefaults() {
		askBeforeUploadingCheckbox.setSelection(getRecordingPreferences().getDefaultBoolean(UsageDataRecordingSettings.ASK_TO_UPLOAD_KEY));
		uploadUrlText.setText(getRecordingPreferences().getDefaultString(UsageDataRecordingSettings.UPLOAD_URL_KEY));
		uploadPeriodText.setText(String.valueOf(getRecordingPreferences().getDefaultLong(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY) / MILLISECONDS_IN_ONE_DAY));

		lastUploadText.setText(getLastUploadDateAsString());

		super.performDefaults();
	}

	private void createGeneralInformationArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		
		composite.setLayout(new GridLayout());
				
		askBeforeUploadingCheckbox = new Button(composite, SWT.CHECK | SWT.LEFT);
		askBeforeUploadingCheckbox.setText("Ask before uploading"); 
	}


	private void createUploadingArea(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Uploading");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		group.setLayout(new GridLayout(3, false));

		// Create the layout that will be used by all the fields.
		GridData fieldLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fieldLayoutData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		
		createUploadUrlField(group);		
		createUploadPeriodField(group);
		createLastUploadField(group);
	}

	private void createUploadUrlField(Group composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setText("Upload URL:");
		
		uploadUrlText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		gridData.horizontalSpan = 2;
		uploadUrlText.setLayoutData(gridData);
		
		final ControlDecoration errorDecoration = new ControlDecoration(uploadUrlText, SWT.LEFT | SWT.TOP);
		errorDecoration.setImage(getErrorImage());
		errorDecoration.setDescriptionText("Enter a valid URL.");
		errorDecoration.hide();
		
		uploadUrlText.addModifyListener(new ModifyListener() {
			// TODO Figure out why this is causing compiler problems.
			//@Override
			public void modifyText(ModifyEvent e) {
				String contents = uploadUrlText.getText();
				if (isValidUploadUrl(contents))
					errorDecoration.hide();
				else
					errorDecoration.show();
				updateApplyButton();
				getContainer().updateButtons();
			}
		});
		
		if (System.getProperty(UsageDataRecordingSettings.UPLOAD_URL_KEY) != null) {
			addOverrideWarning(uploadUrlText);						
		}
	}

	private boolean isValidUploadUrl(String text) {
		try {
			URL url = new URL(text);
//			if (!("http".equals(url.getProtocol())))
//				return false;
			if (url.getHost().length() == 0)
				return false;
		} catch (MalformedURLException e1) {
			return false;
		}
		return true;
	}
		
	private void createUploadPeriodField(Group composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setText("Upload Period:");
		
		uploadPeriodText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		uploadPeriodText.setTextLimit(2);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		gridData.horizontalSpan = 1;
		GC gc = new GC(uploadPeriodText.getDisplay());
		gc.setFont(uploadPeriodText.getFont());
		gridData.widthHint = gc.stringExtent(String.valueOf(MAXIMUM_PERIOD_IN_DAYS)).x;
		gc.dispose();
		uploadPeriodText.setLayoutData(gridData);
		
		new Label(composite, SWT.NONE).setText("days");
		
		final ControlDecoration rangeErrorDecoration = new ControlDecoration(uploadPeriodText, SWT.LEFT | SWT.TOP);
		rangeErrorDecoration.setDescriptionText(MessageFormat.format("Enter a period between {0} and {1} days.", MINIMUM_PERIOD_IN_DAYS, MAXIMUM_PERIOD_IN_DAYS));
		rangeErrorDecoration.setImage(getErrorImage());
		rangeErrorDecoration.hide();
		
		uploadPeriodText.addModifyListener(new ModifyListener() {
			// TODO Figure out why this is causing compiler problems.
			//@Override
			public void modifyText(ModifyEvent e) {
				String contents = uploadPeriodText.getText();
				if (isValidUploadPeriod(contents))
					rangeErrorDecoration.hide();
				else {
					rangeErrorDecoration.show();
				}
				updateApplyButton();
				getContainer().updateButtons();
			}
		});
		if (System.getProperty(UsageDataRecordingSettings.UPLOAD_PERIOD_KEY) != null) {
			addOverrideWarning(uploadPeriodText);
		}
	}
	
	private boolean isValidUploadPeriod(String text) {
		try {
			long value = Long.parseLong(text);
			if (value < MINIMUM_PERIOD_IN_DAYS)
				return false;
			if (value > MAXIMUM_PERIOD_IN_DAYS)
				return false;
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
		
	private Image getErrorImage() {
		return FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
	}


	private void createLastUploadField(Group composite) {
		label = new Label(composite, SWT.NONE);
		label.setText("Last Upload:");
		
		lastUploadText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		lastUploadText.setEnabled(false);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		gridData.horizontalSpan = 2;
		lastUploadText.setLayoutData(gridData);
	}
	
	private void createButtonsArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		composite.setLayout(new RowLayout());

		createUploadNowButton(composite);
		createShowMyUsageButton(composite);
	}

	private void createShowMyUsageButton(Composite composite) {
		Button uploadNow = new Button(composite, SWT.PUSH);
		uploadNow.setText("Show My Usage Data");
		uploadNow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MyUsageDataEditorInput input = new MyUsageDataEditorInput(getSettings().getMyUsageDataUrl());
				try {
					getPage().openEditor(input, MyUsageDataEditor.class.getName());
				} catch (PartInitException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}

	protected IWorkbenchPage getPage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}


	protected UsageDataRecordingSettings getSettings() {
		return Activator.getDefault().getSettings();
	}


	private void createUploadNowButton(Composite composite) {
		Button uploadNow = new Button(composite, SWT.PUSH);
		uploadNow.setText("Upload Now");
		uploadNow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Activator.getDefault().getUploadManager().startUpload();
			}
		});
	}
	
	private void addOverrideWarning(Control control) {
		FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);
		ControlDecoration warning = new ControlDecoration(control, SWT.BOTTOM | SWT.LEFT);
		warning.setImage(decoration.getImage());
		warning.setDescriptionText("This value is being overridden by a System property.");
	}

	private String getLastUploadDateAsString() {
		long time = getRecordingSettings().getLastUploadTime();
		Date date = new Date(time);
		return date.toString();
	}


	private IPreferenceStore getRecordingPreferences() {
		return Activator.getDefault().getPreferenceStore();
	}

	private UsageDataRecordingSettings getRecordingSettings() {
		return Activator.getDefault().getSettings();
	}
}