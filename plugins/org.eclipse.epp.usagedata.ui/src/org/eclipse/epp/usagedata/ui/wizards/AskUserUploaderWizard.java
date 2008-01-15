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

import org.eclipse.epp.usagedata.ui.uploaders.AskUserUploader;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class AskUserUploaderWizard extends Wizard implements INewWizard {
	private final AskUserUploader uploader;
	private TermsOfUseWizardPage termsPage;

	public AskUserUploaderWizard(AskUserUploader uploader) {
		super();
		this.uploader = uploader;
		setNeedsProgressMonitor(false);
		setHelpAvailable(false);
		
	}
	
	public void addPages() {
		addPage(new SelectActionWizardPage(uploader));
		termsPage = new TermsOfUseWizardPage(uploader);
		addPage(termsPage);
		addPage(new UploadPreviewPage(uploader));
		//addPage(new FilterPage(uploader));
	}
	
	@Override
	public boolean canFinish() {
		if (!uploader.hasUploadAction()) return true;
		return uploader.hasUserAcceptedTermsOfUse();
	}
	
	@Override
	public boolean performCancel() {
		uploader.cancel();
		return true;
	}
	
	public boolean performFinish() {
		uploader.execute();
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	public void showTermsPage() {
		getContainer().showPage(termsPage);
	}
}