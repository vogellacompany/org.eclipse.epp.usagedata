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
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings;
import org.eclipse.epp.usagedata.internal.recording.uploading.util.MockUploadSettings;
import org.eclipse.epp.usagedata.internal.recording.uploading.util.UploaderTestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

public class BasicUploaderTests {
	private static final String GOOD_PATH = "/upload_good";

	private static int port;
	private static HttpServer server;

	@BeforeClass
	public static void startServer() throws Exception {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext(GOOD_PATH, exchange -> {
			exchange.getRequestBody().readAllBytes();
			byte[] body = "log:received!\n".getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "text/plain");
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(body);
			}
		});
		server.start();
		port = server.getAddress().getPort();
	}

	@AfterClass
	public static void stopServer() throws Exception {
		server.stop(0);
	}

	@Test
	public void testBigUpload() throws Exception {
		MockUploadSettings settings = new MockUploadSettings();
		settings.setUploadUrl("http://localhost:" + port + GOOD_PATH);
		
		File file = UploaderTestUtils.createBogusUploadDataFile(90);
		
		UploadParameters uploadParameters = new UploadParameters();
		uploadParameters.setSettings(settings);
		uploadParameters.setFiles(new File[] {file});
		
		UploadResult result = new BasicUploader(uploadParameters).doUpload(new NullProgressMonitor());

		assertEquals(200, result.getReturnCode());
		assertFalse(file.exists());
	}
	
	@Test
	public void testInvalidUrl() throws Exception {
		MockUploadSettings settings = new MockUploadSettings();
		settings.setUploadUrl("httpx://localhost:" + port + GOOD_PATH);
		
		File file = UploaderTestUtils.createBogusUploadDataFile(1);

		UploadParameters uploadParameters = new UploadParameters();
		uploadParameters.setSettings(settings);
		uploadParameters.setFiles(new File[] {file});
		
		try {
			new BasicUploader(uploadParameters).doUpload(new NullProgressMonitor());
			
			fail("IllegalStateException expected.");
		} catch (IllegalStateException e) {
			// Expected
		} 
		assertTrue(file.exists());
	}

	@Test
	public void testUnknownHost() throws Exception {
		MockUploadSettings settings = new MockUploadSettings();
		settings.setUploadUrl("http://localhost:" + port + "/Non-existent-path");
		
		File file = UploaderTestUtils.createBogusUploadDataFile(1);

		UploadParameters uploadParameters = new UploadParameters();
		uploadParameters.setSettings(settings);
		uploadParameters.setFiles(new File[] {file});
		
		UploadResult result = new BasicUploader(uploadParameters).doUpload(new NullProgressMonitor());
		
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