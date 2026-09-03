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
package org.eclipse.epp.usagedata.internal.ui.wizards;

import org.eclipse.epp.usagedata.internal.ui.TermsOfUse;
import org.eclipse.epp.usagedata.internal.ui.uploaders.AskUserUploader;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class TermsOfUseWizardPage extends WizardPage {

	private final AskUserUploader uploader;

	public TermsOfUseWizardPage(AskUserUploader uploader) {
		super(Messages.TermsOfUseWizardPage_1);
		this.uploader = uploader;
		setDescription(Messages.TermsOfUseWizardPage_3);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		TermsOfUse.createControl(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Button acceptTermsButton = new Button(container, SWT.CHECK);
		acceptTermsButton.setText(Messages.TermsOfUseWizardPage_2); 
		GridData gridData = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
		acceptTermsButton.setLayoutData(gridData);
		acceptTermsButton.setSelection(uploader.hasUserAcceptedTermsOfUse());
		acceptTermsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				uploader.setUserAcceptedTermsOfUse(acceptTermsButton.getSelection());
				getContainer().updateButtons();
			}
		});
		
		setControl(container);
		
	}

}