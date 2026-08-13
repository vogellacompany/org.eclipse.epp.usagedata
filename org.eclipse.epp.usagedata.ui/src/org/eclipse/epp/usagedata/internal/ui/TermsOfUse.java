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
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Provides the terms of use document for display in a {@link org.eclipse.swt.browser.Browser}.
 */
public class TermsOfUse {

	private static final String TERMS_FILE = "terms.html"; //$NON-NLS-1$

	/**
	 * Returns the terms of use as HTML, with the colors of the current theme
	 * applied. Returns an empty document if the terms cannot be read.
	 */
	public static String getHtml(Display display) {
		String html = readTerms();
		String style = "<style>body { background-color: " + toHex(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND)) //$NON-NLS-1$
				+ "; color: " + toHex(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND)) + "; }</style>"; //$NON-NLS-1$ //$NON-NLS-2$
		return html.replace("</head>", style + "</head>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String readTerms() {
		try (InputStream in = FileLocator.openStream(Activator.getDefault().getBundle(), new Path(TERMS_FILE), false);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.joining("\n")); //$NON-NLS-1$
		} catch (IOException e) {
			Activator.getDefault().log(IStatus.WARNING, e, "Cannot read the terms of use."); //$NON-NLS-1$
			return "<html><head></head><body></body></html>"; //$NON-NLS-1$
		}
	}

	private static String toHex(Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); //$NON-NLS-1$
	}
}
