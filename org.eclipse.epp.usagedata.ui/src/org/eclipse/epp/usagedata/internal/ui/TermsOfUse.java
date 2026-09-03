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
package org.eclipse.epp.usagedata.internal.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledFormText;

/**
 * Shows the terms of use as native, theme-following text.
 */
public class TermsOfUse {

	private static final String TERMS_FILE = "terms.xml"; //$NON-NLS-1$
	private static final String HEADER_FONT = "header"; //$NON-NLS-1$

	/**
	 * Creates a scrolling view of the terms that wraps to the parent's width.
	 * Links open in the external browser.
	 */
	public static Control createControl(Composite parent) {
		ScrolledFormText scrolled = new ScrolledFormText(parent, SWT.V_SCROLL | SWT.BORDER, false);
		FormText text = new FormText(scrolled, SWT.WRAP | SWT.NO_FOCUS);
		text.marginWidth = 8;
		text.marginHeight = 8;
		text.setFont(HEADER_FONT, JFaceResources.getHeaderFont());
		text.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent event) {
				browseTo(String.valueOf(event.getHref()));
			}
		});
		scrolled.setFormText(text);
		scrolled.setText(readTerms());
		passOnInitialFocus(scrolled, text);
		return scrolled;
	}

	/**
	 * A FormText that receives focus selects its first link and scrolls to it,
	 * so the dialog's initial focus would open the terms at the bottom. The
	 * first focus is handed to the next control instead.
	 */
	private static void passOnInitialFocus(ScrolledFormText scrolled, FormText text) {
		text.addListener(SWT.FocusIn, new Listener() {
			public void handleEvent(Event event) {
				text.removeListener(SWT.FocusIn, this);
				text.traverse(SWT.TRAVERSE_TAB_NEXT);
				scrolled.setOrigin(0, 0);
			}
		});
	}

	private static String readTerms() {
		try (InputStream in = FileLocator.openStream(Activator.getDefault().getBundle(), new Path(TERMS_FILE), false);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.joining("\n")); //$NON-NLS-1$
		} catch (IOException e) {
			Activator.getDefault().log(IStatus.WARNING, e, "Cannot read the terms of use."); //$NON-NLS-1$
			return "<form></form>"; //$NON-NLS-1$
		}
	}

	private static void browseTo(String url) {
		try {
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
		} catch (Exception e) {
			Activator.getDefault().log(IStatus.WARNING, e, "Cannot open %1$s.", url); //$NON-NLS-1$
		}
	}
}
