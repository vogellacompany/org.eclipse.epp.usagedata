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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.epp.usagedata.internal.recording.uploading.UploadParameters;
import org.eclipse.epp.usagedata.internal.ui.Activator;
import org.eclipse.epp.usagedata.internal.ui.preview.util.MockUploadSettings;
import org.eclipse.epp.usagedata.internal.ui.preview.util.MockUsageDataEventFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The {@link UploadPreviewTests} class tests various aspects of the
 * {@link UploadPreview} class. This test must be run as a &quot;JUnit Plug-in Test&quot;.
 * 
 * @author Wayne Beaton
 */
@SuppressWarnings("restriction")
public class UploadPreviewTests {
	UploadParameters parameters;
	UploadPreview preview;
	private Display display;
	private Shell shell;

	@Before
	public void setup() throws Exception {
		parameters = new UploadParameters();
		parameters.setSettings(new MockUploadSettings());

		parameters.setFiles(new File[] {findFile("upload0.csv")});
		preview = new UploadPreview(parameters);
		
		while (!EclipseStarter.isRunning()) Thread.sleep(100);
		display = PlatformUI.getWorkbench().getDisplay();
		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		preview.createControl(shell);
		shell.open();
	
		preview.processFiles(new NullProgressMonitor());
		preview.viewer.setInput((Object[]) preview.events.toArray(new Object[preview.events.size()]));
	}
	
	@After
	public void shutdown() {
		shell.close();
		shell.dispose();
	}
	
	@Test
	public void tableFullyPopulated() {
		assertEquals(4, preview.events.size());
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

		assertFalse(preview.removeFilterButton.getEnabled());
		
		((MockUsageDataEventFilter)parameters.getFilter()).addPattern("org.eclipse.core.*");
		
		assertTrue(preview.removeFilterButton.getEnabled());		
	}
	
	@Test
	public void testRowChangesColorWhenFilterChanges() throws Exception {
		
		assertNull(preview.viewer.getTable().getItem(0).getImage(0));
		assertEquals(display.getSystemColor(SWT.COLOR_BLACK), preview.viewer.getTable().getItem(0).getForeground(1));
		
		((MockUsageDataEventFilter)parameters.getFilter()).addPattern("org.eclipse.osgi");

		assertNotNull(preview.viewer.getTable().getItem(0).getImage(0));
		assertEquals(display.getSystemColor(SWT.COLOR_GRAY), preview.viewer.getTable().getItem(0).getForeground(1));
		
		((MockUsageDataEventFilter)parameters.getFilter()).removeFilterPatterns(new String[] {"org.eclipse.osgi"});
		
		assertNull(preview.viewer.getTable().getItem(0).getImage(0));
		assertEquals(display.getSystemColor(SWT.COLOR_BLACK), preview.viewer.getTable().getItem(0).getForeground(1));
		
	}

	private File findFile(String string) throws URISyntaxException, IOException {
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path("upload0.csv"), null);
		return new File(FileLocator.toFileURL(url).toURI());
	}
}
