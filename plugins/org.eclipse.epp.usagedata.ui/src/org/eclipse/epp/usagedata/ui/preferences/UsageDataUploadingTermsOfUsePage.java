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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.epp.usagedata.ui.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class UsageDataUploadingTermsOfUsePage extends PreferencePage
	implements IWorkbenchPreferencePage {

	public UsageDataUploadingTermsOfUsePage() {
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
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		composite.setLayout(new FillLayout());
		
		Browser browser = new Browser(composite, SWT.NONE);
		browser.setText(getTermsOfUse());
		
		return composite;
	}

	private String getTermsOfUse() {
		StringBuilder builder = new StringBuilder();
		InputStream input = null;
		try {
			input = (InputStream) Activator.getDefault().getBundle().getResource("terms.html").getContent();
			byte[] buffer = new byte[512];
			int count;
			while ((count = input.read(buffer)) > 0) {
				builder.append(new String(buffer, 0, count));
			}
		} catch (IOException e) {
			// TODO Handle exception.
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return builder.toString();
	}


}