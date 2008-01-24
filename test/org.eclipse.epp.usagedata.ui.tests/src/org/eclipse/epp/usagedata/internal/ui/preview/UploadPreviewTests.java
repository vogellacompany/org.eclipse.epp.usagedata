/*******************************************************************************
 * Copyright (c) 2008 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.ui.preview;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.epp.usagedata.internal.recording.uploading.UploadParameters;
import org.eclipse.epp.usagedata.internal.ui.Activator;
import org.eclipse.epp.usagedata.internal.ui.preview.util.MockUploadSettings;
import org.eclipse.epp.usagedata.internal.ui.preview.util.MockUsageDataEventFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

public class UploadPreviewTests {
	UploadParameters parameters;
	
	@Before
	public void setup() {
		parameters = new UploadParameters();
		parameters.setSettings(new MockUploadSettings());
	}
	
	@Test
	public void testUpdateButtons() throws Exception {
		while (!EclipseStarter.isRunning()) Thread.sleep(100);
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				doTestUpdateButtons(display);
			}
		});
	}
	
	void doTestUpdateButtons(Display display) {
		Shell shell = new Shell(display);
		UploadPreview preview = new UploadPreview(parameters);
		preview.createControl(shell);
		
		shell.open();

		assertFalse(preview.removeFilterButton.getEnabled());
		
		((MockUsageDataEventFilter)parameters.getFilter()).addPattern("org.eclipse.core.*");
		
		assertTrue(preview.removeFilterButton.getEnabled());		
	}
	
	@Test
	public void testRowChangesColorWhenFilterChanges() throws Exception {
		parameters.setFiles(new File[] {findFile("upload0.csv")});
		final UploadPreview preview = new UploadPreview(parameters);
		
		while (!EclipseStarter.isRunning()) Thread.sleep(100);
		final Display display = PlatformUI.getWorkbench().getDisplay();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		preview.createControl(shell);
		shell.open();
	
		preview.processFiles(new NullProgressMonitor());
		preview.viewer.setInput((Object[]) preview.events.toArray(new Object[preview.events.size()]));
		
		assertNull(preview.viewer.getTable().getItem(0).getImage(0));
		assertEquals(display.getSystemColor(SWT.COLOR_BLACK), preview.viewer.getTable().getItem(0).getForeground(1));
		
		((MockUsageDataEventFilter)parameters.getFilter()).addPattern("org.eclipse.osgi");

		assertNotNull(preview.viewer.getTable().getItem(0).getImage(0));
		assertEquals(display.getSystemColor(SWT.COLOR_GRAY), preview.viewer.getTable().getItem(0).getForeground(1));
	}

	private File findFile(String string) throws URISyntaxException, IOException {
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path("upload0.csv"), null);
		return new File(FileLocator.toFileURL(url).toURI());
	}
}
