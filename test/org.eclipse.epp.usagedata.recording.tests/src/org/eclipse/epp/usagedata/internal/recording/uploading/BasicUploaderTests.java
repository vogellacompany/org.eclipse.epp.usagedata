package org.eclipse.epp.usagedata.internal.recording.uploading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.epp.usagedata.internal.recording.Activator;
import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;
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
		
		UploadParameters uploadParameters = new UploadParameters(null);
		uploadParameters.setSettings(settings);
		uploadParameters.setFiles(new File[] {file});
		
		UploadResult result = new BasicUploader().doUpload(uploadParameters, null);

		assertEquals(200, result.getReturnCode());
		assertFalse(file.exists());
	}
	
	@Test
	public void testInvalidUrl() throws Exception {
		UsageDataRecordingSettingsMock settings = UploaderTestUtils.getSettings();
		settings.setUploadUrl("httpx://localhost:" + port + GOOD_SERVLET_NAME);
		
		File file = UploaderTestUtils.createBogusUploadDataFile(settings, 1);

		UploadParameters uploadParameters = new UploadParameters(null);
		uploadParameters.setSettings(settings);
		uploadParameters.setFiles(new File[] {file});
		
		try {
			new BasicUploader().doUpload(uploadParameters, null);
			
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

		UploadParameters uploadParameters = new UploadParameters(null);
		uploadParameters.setSettings(settings);
		uploadParameters.setFiles(new File[] {file});
		
		UploadResult result = new BasicUploader().doUpload(uploadParameters, null);
		
		assertEquals(404, result.getReturnCode());
		assertTrue(file.exists());
	}
		
	@Test
	public void testTermsOfUseNotAccepted() {
		UsageDataRecordingSettings settings = new UsageDataRecordingSettings() {
			@Override
			public boolean isEnabled() {
				return true;
			}
			
			@Override
			public boolean hasUserAcceptedTermsOfUse() {
				return false;
			}
		};
		UploadParameters uploadParameters = new UploadParameters(null);
		uploadParameters.setSettings(settings);
		
		assertFalse(new BasicUploader().hasUserAuthorizedUpload(uploadParameters));
	}

	@Test
	public void testNotEnabled() {
		UsageDataRecordingSettings settings = new UsageDataRecordingSettings() {
			@Override
			public boolean isEnabled() {
				return false;
			}
			
			@Override
			public boolean hasUserAcceptedTermsOfUse() {
				return true;
			}
		};
		UploadParameters uploadParameters = new UploadParameters(null);
		uploadParameters.setSettings(settings);
		
		assertFalse(new BasicUploader().hasUserAuthorizedUpload(uploadParameters));
	}
}