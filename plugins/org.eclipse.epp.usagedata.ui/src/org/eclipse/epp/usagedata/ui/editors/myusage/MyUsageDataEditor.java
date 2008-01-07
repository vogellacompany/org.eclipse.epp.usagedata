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
package org.eclipse.epp.usagedata.ui.editors.myusage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * The {@link MyUsageDataEditor} presents a browser, populated
 * with information about the usage data captured and uploaded
 * for the current workspace. This information is downloaded from
 * the server (i.e. there is no local data displayed).
 * 
 * @author Wayne Beaton
 *
 */
public class MyUsageDataEditor extends EditorPart {

	private Browser browser;

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Nothing to save.		
	}

	@Override
	public void doSaveAs() {
		// Nothing to save.
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		// Nothing to save, can't be dirty.
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// Nothing to save.
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		composite.setLayout(new GridLayout());
		
		createButtons(composite);
		createBrowser(composite);
	}

	private void createButtons(Composite composite) {
		Composite buttons = new Composite(composite, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		
		buttons.setLayout(new RowLayout());
		Button refreshButton = new Button(buttons, SWT.PUSH);
		refreshButton.setText("Refresh");
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				reload();
			}
		});
	}

	private void createBrowser(Composite composite) {
		browser = new Browser(composite, SWT.BORDER);
		browser.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		browser.setUrl(((MyUsageDataEditorInput)getEditorInput()).getUsageDataUrl());
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	public void reload() {
		browser.refresh();
	}
}
