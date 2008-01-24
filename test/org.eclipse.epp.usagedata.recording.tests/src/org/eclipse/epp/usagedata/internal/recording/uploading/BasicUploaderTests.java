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
package org.eclipse.epp.usagedata.internal.recording.uploading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.epp.usagedata.internal.recording.Activator;
import org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings;
import org.eclipse.epp.usagedata.internal.recording.uploading.util.MockUploadSettings;
import org.eclipse.epp.usagedata.internal.recording.uploading.util.UploadGoodServlet;
import org.eclipse.epp.usagedata.internal.recording.uploading.util.UploaderTestUtils;
import org.eclipse.epp.usagedata.internal.recording.uploading.util.UsageDataRecordingSettingsMock;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.equinox.http.jetty.JettyConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

public class BasicUploaderTests {
	private static final String GOOD_SERVLET_NAME = "/upload_good";
	private static final String SERVER_NAME = "usagedata.upload.tests";
	
	private static int port;
	private static ServiceTracker tracker;

	@BeforeClass
	public static void startServer() throws Exception {
		Dictionary<String, Object> settings = new Hashtable<String, Object>();	
		settings.put(JettyConstants.OTHER_INFO, SERVER_NAME);
		settings.put("http.port", 0);
		JettyConfigurator.startServer(SERVER_NAME, settings);
		
		ServiceReference[] reference = Activator.getDefault().getBundle().getBundleContext().getServiceReferences("org.osgi.service.http.HttpService", "(other.info=usagedata.upload.tests)"); 
		Object assignedPort = reference[0].getProperty("http.port"); 
		port = Integer.parseInt((String)assignedPort);
		
		tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(), reference[0], null);
		tracker.open();
		HttpService server = (HttpService)tracker.getService();
		server.registerServlet(GOOD_SERVLET_NAME, new UploadGoodServlet(), null, null);		
	}
	
	@AfterClass
	public static void stopServer() throws Exception {
		tracker.close();
		JettyConfigurator.stopServer(SERVER_NAME);
	}
	
	@Test
	public void testBigUpload() throws Exception {
		UsageDataRecordingSettingsMock settings = UploaderTestUtils.getSettings();
		settings.setUploadUrl("http://localhost:" + port + GOOD_SERVLET_NAME);
		
		File file = UploaderTestUtils.createBogusUploadDataFile(settings, 90);
		
		UploadParameters uploadParameters = new UploadParameters();
		uploadParameters.setSettings(settings);
		uploadParameters.setFiles(new File[] {file});
		
		UploadResult result = new BasicUploader(uploadParameters).doUpload(null);

		assertEquals(200, result.getReturnCode());
		assertFalse(file.exists());
	}
	
	@Test
	public void testInvalidUrl() throws Exception {
		UsageDataRecordingSettingsMock settings = UploaderTestUtils.getSettings();
		settings.setUploadUrl("httpx://localhost:" + port + GOOD_SERVLET_NAME);
		
		File file = UploaderTestUtils.createBogusUploadDataFile(settings, 1);

		UploadParameters uploadParameters = new UploadParameters();
		uploadParameters.setSettings(settings);
		uploadParameters.setFiles(new File[] {file});
		
		try {
			new BasicUploader(uploadParameters).doUpload(null);
			
			fail("IllegalStateException expected.");
		} catch (IllegalStateException e) {
			// Expected
		} 
		assertTrue(file.exists());
	}

	@Test
	public void testUnknownHost() throws Exception {
		UsageDataRecordingSettingsMock settings = UploaderTestUtils.getSettings();
		settings.setUploadUrl("http://localhost:" + port + "/Non-existent-path");
		
		File file = UploaderTestUtils.createBogusUploadDataFile(settings, 1);

		UploadParameters uploadParameters = new UploadParameters();
		uploadParameters.setSettings(settings);
		uploadParameters.setFiles(new File[] {file});
		
		UploadResult result = new BasicUploader(uploadParameters).doUpload(null);
		
		assertEquals(404, result.getReturnCode());
		assertTrue(file.exists());
	}
		
	@Test
	public void testTermsOfUseNotAccepted() {
		UploadSettings settings = new MockUploadSettings() {
			@Override
			public boolean isEnabled() {
				return true;
			}
			
			@Override
			public boolean hasUserAcceptedTermsOfUse() {
				return false;
			}
		};
		UploadParameters uploadParameters = new UploadParameters();
		uploadParameters.setSettings(settings);
		
		assertFalse(new BasicUploader(uploadParameters).hasUserAuthorizedUpload());
	}

	@Test
	public void testNotEnabled() {
		UploadSettings settings = new MockUploadSettings() {
			@Override
			public boolean isEnabled() {
				return false;
			}
			
			@Override
			public boolean hasUserAcceptedTermsOfUse() {
				return true;
			}
		};
		UploadParameters uploadParameters = new UploadParameters();
		uploadParameters.setSettings(settings);
		
		assertFalse(new BasicUploader(uploadParameters).hasUserAuthorizedUpload());
	}
}