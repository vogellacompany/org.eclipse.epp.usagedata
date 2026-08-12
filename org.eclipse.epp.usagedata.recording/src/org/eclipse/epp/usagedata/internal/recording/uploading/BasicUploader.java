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
package org.eclipse.epp.usagedata.internal.recording.uploading;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.recording.UsageDataRecordingActivator;
import org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings;

/**
 * Instances of the {@link BasicUploader} class are responsible for
 * uploading a set of files to the server.
 *
 * @author Wayne Beaton
 *
 */
public class BasicUploader extends AbstractUploader {

	/**
	 * The HTTP_USERID constant is the key for the HTTP header
	 * that is used to pass the user (i.e. workstation) identifier.
	 * This value identifies the user's workstation (which may
	 * include multiple Eclipse workspaces).
	 */
	private static final String HTTP_USERID = "USERID"; //$NON-NLS-1$

	/**
	 * The HTTP_WORKSPACE constant is the key for the HTTP header
	 * that is used to pass the workspace identifier. This value
	 * is used to identify a single workspace on the user's workstation.
	 * A user may have more than one workspace and each will have
	 * a different workspace id.
	 */
	private static final String HTTP_WORKSPACEID = "WORKSPACEID";	 //$NON-NLS-1$

	/**
	 * The HTTP_TIME constant is the key for the HTTP header
	 * that is used to pass the current time on the workstation to
	 * the server. This value is included in the request so that the
	 * server, if desired, can account for differences in the clock
	 * between the user's workstation and the server.
	 */
	private static final String HTTP_TIME = "TIME"; //$NON-NLS-1$

	private static final String USER_AGENT = "User-Agent"; //$NON-NLS-1$

	private boolean uploadInProgress = false;

	private ListenerList responseListeners = new ListenerList();

	public BasicUploader(UploadParameters uploadParameters) {
		super();
		setUploadParameters(uploadParameters);
	}

	/**
	 * Uploads are done with a {@link Job} running in the background
	 * at a relatively low priority. The intent is to make the user
	 * as blissfully unaware that anything is happening as possible.
	 * <p>
	 * Once the job has been started, the values on the instance
	 * cannot be modified. The instance is <em>not</em> reusable.
	 * </p>
	 */
	public synchronized void startUpload() {
		checkValues();
		if (uploadInProgress) return;
		uploadInProgress = true;
		Job job = new Job("Uploading usage data...") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				UploadResult result = upload(monitor);
				uploadInProgress = false;
				fireUploadComplete(result);
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.LONG);
		job.schedule();
	}

	/**
	 * Do the upload. This is basically a wrapper method that invokes the real
	 * behaviour and then deals with the fallout.
	 *
	 * @param monitor
	 *            an instance of something that implements
	 *            {@link IProgressMonitor}. Must not be <code>null</code>.
	 * @return
	 */
	UploadResult upload(IProgressMonitor monitor) {
		UploadResult result = null;

		try {
			long start = System.currentTimeMillis();
			result = doUpload(monitor);
			long duration = System.currentTimeMillis() - start;

			if (result.isSuccess()) {
				UsageDataRecordingActivator.getDefault().log(IStatus.INFO, "Usage data uploaded to %1$s in %2$s milliseconds.", getUploadUrl(), duration); //$NON-NLS-1$
			} else {
				UsageDataRecordingActivator.getDefault().log(IStatus.INFO, "Usage data upload to %1$s failed with error code %2$s.", getUploadUrl(), result.getReturnCode()); //$NON-NLS-1$
			}

		} catch (IllegalStateException e) {
			UsageDataRecordingActivator.getDefault().log(IStatus.WARNING, e, "The URL provided for usage data upload, %1$s, is invalid.", getUploadUrl()); //$NON-NLS-1$
		} catch (UnknownHostException e) {
			UsageDataRecordingActivator.getDefault().log(IStatus.WARNING, e, "The usage data upload server at %1$s could not be found.", getUploadUrl()); //$NON-NLS-1$
		} catch (ConnectException e) {
			UsageDataRecordingActivator.getDefault().log(IStatus.WARNING, e, "Could not connect to the usage data upload server at %1$s.", getUploadUrl()); //$NON-NLS-1$
		} catch (InterruptedIOException e) {
			UsageDataRecordingActivator.getDefault().log(IStatus.WARNING, e, "A socket timeout occurred while trying to upload usage data.");			 //$NON-NLS-1$
		} catch (Exception e) {
			UsageDataRecordingActivator.getDefault().log(IStatus.WARNING, e, "An exception occurred while trying to upload usage data."); //$NON-NLS-1$
		}

		return result;
	}
	private String getUploadUrl() {
		return getSettings().getUploadUrl();
	}

	/**
	 * This method does the heavy lifting when it comes to downloads.
	 *
	 * I can envision a time when we may want to upload something other than files.
	 * We may, for example, want to upload an in-memory representation of the files.
	 * For now, in the spirit of having something that works is better than
	 * overengineering something you may not need, we're just dealing with files.
	 *
	 * @param monitor
	 *            an instance of something that implements
	 *            {@link IProgressMonitor}. Must not be <code>null</code>.
	 * @throws Exception
	 */
	UploadResult doUpload(IProgressMonitor monitor) throws Exception {
		monitor.beginTask("Upload", getUploadParameters().getFiles().length + 3); //$NON-NLS-1$
		/*
		 * The files that we have been provided with were determined while the recorder
		 * was suspended. We should be safe to work with these files without worrying
		 * that other threads are messing with them. We do need to consider that other
		 * processes running outside of our JVM may be messing with these files and
		 * anticipate errors accordingly.
		 */

		// TODO Does it make sense to create a custom exception for this?
		if (!hasUserAuthorizedUpload()) throw new Exception("User has not authorized upload."); //$NON-NLS-1$

		URI uploadUri = URI.create(getSettings().getUploadUrl());
		if (!"http".equals(uploadUri.getScheme()) && !"https".equals(uploadUri.getScheme())) { //$NON-NLS-1$ //$NON-NLS-2$
			throw new IllegalStateException("Invalid upload URL scheme: " + uploadUri.getScheme()); //$NON-NLS-1$
		}

		String boundary = UUID.randomUUID().toString();

		byte[] multipartBody = buildMultipartBody(boundary, monitor);

		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.uri(uploadUri)
			.header("Content-Type", "multipart/form-data; boundary=" + boundary) //$NON-NLS-1$ //$NON-NLS-2$
			.header(HTTP_USERID, getSettings().getUserId())
			.header(HTTP_WORKSPACEID, getSettings().getWorkspaceId())
			.header(HTTP_TIME, String.valueOf(System.currentTimeMillis()))
			.header(USER_AGENT, getSettings().getUserAgent())
			.timeout(Duration.ofMillis(getSocketTimeout()))
			.POST(BodyPublishers.ofByteArray(multipartBody));

		boolean loggingServerActivity = getSettings().isLoggingServerActivity();
		if (loggingServerActivity) {
			requestBuilder.header("LOGGING", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		monitor.worked(1);

		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(30))
			.build();

		HttpResponse<InputStream> response = client.send(
			requestBuilder.build(),
			HttpResponse.BodyHandlers.ofInputStream());

		int result = response.statusCode();

		handleServerResponse(response);

		monitor.worked(1);

		// Check the result. HTTP return code of 200 means success.
		if (result == 200) {
			for (File file : getUploadParameters().getFiles()) {
				// TODO what if file delete fails?
				if (file.exists()) file.delete();
			}
		}

		monitor.worked(1);

		monitor.done();

		return new UploadResult(result);
	}

	/**
	 * This method returns a &quot;reasonable&quot; value for
	 * socket timeout based on the number of files we're trying
	 * to upload. Assumes that &quot;about a minute&quot; per
	 * file should be plenty of time.
	 *
	 * @return int value specifying a reasonable timeout.
	 */
	int getSocketTimeout() {
		return getUploadParameters().getFiles().length * 60000;
	}

	byte[] buildMultipartBody(String boundary, IProgressMonitor monitor) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (File file : getUploadParameters().getFiles()) {
			try {
				byte[] filteredContent = getFilteredFileContent(file);
				out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$ //$NON-NLS-2$
				out.write(("Content-Disposition: form-data; name=\"uploads[]\"; filename=\"" + file.getName() + "\"\r\n").getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$ //$NON-NLS-2$
				out.write("Content-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$
				out.write(filteredContent);
				out.write("\r\n".getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$
				monitor.worked(1);
			} catch (FileNotFoundException e) {
				// If an exception occurs while reading the file,
				// ignore the error and move on. If this has happened,
				// then another process may have deleted or moved the file.
			}
		}
		out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$ //$NON-NLS-2$
		return out.toByteArray();
	}

	byte[] getFilteredFileContent(File file) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
				InputStream input = new FileInputStream(file)) {
			new UsageDataFileReader(input).iterate(new UsageDataFileReader.Iterator() {
				public void header(String header) throws Exception {
					writer.append(header);
					writer.append('\n');
				}
				public void event(String line, UsageDataEvent event) throws Exception {
					if (getUploadParameters().getFilter().includes(event)) {
						writer.append(line);
						writer.append('\n');
					}
				}
			});
		}
		return out.toByteArray();
	}

	void handleServerResponse(HttpResponse<InputStream> response) {
		// No point in doing any work if nobody's listening.
		if (!shouldProcessServerResponse()) return;

		try (InputStream body = response.body();
			 BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
			handleServerResponse(reader);
		} catch (IOException e) {
			UsageDataRecordingActivator.getDefault().log(IStatus.WARNING, e, "Exception raised while parsing the server response"); //$NON-NLS-1$
		}
	}

	private boolean shouldProcessServerResponse() {
		if (getSettings().isLoggingServerActivity()) return true;
		if (!responseListeners.isEmpty()) return true;
		return false;
	}

	void handleServerResponse(BufferedReader response) throws IOException {
		while (true) {
			String line = response.readLine();
			if (line == null) return;
			if (getSettings().isLoggingServerActivity()) {
				UsageDataRecordingActivator.getDefault().log(IStatus.INFO, line);
			}
			int colon = line.indexOf(':'); // first occurrence
			if (colon != -1) {
				String key = line.substring(0, colon);
				String value = line.substring(colon + 1);
				handleServerResponse(key, value);
			} else {
				handleServerResponse("", line); //$NON-NLS-1$
			}
		}
	}

	void handleServerResponse(String key, String value) {
		BasicUploaderServerResponse response = new BasicUploaderServerResponse(key, value);
		for(Object listener : responseListeners.getListeners()) {
			((BasicUploaderResponseListener)listener).handleServerResponse(response);
		}
	}

	/**
	 * This method sets up a bit of a roadblock to ensure that an upload does
	 * not occur if the user has not explicitly consented. The user must have
	 * both enabled the service and agreed to the terms of use.
	 *
	 * @return <code>true</code> if the upload can occur, or
	 *         <code>false</code> otherwise.
	 */
	boolean hasUserAuthorizedUpload() {
		if (!getSettings().isEnabled()) return false;
		if (!getSettings().hasUserAcceptedTermsOfUse()) return false;
		return true;
	}
	private UploadSettings getSettings() {
		return getUploadParameters().getSettings();
	}

	public synchronized boolean isUploadInProgress() {
		return uploadInProgress;
	}

	public void addResponseListener(BasicUploaderResponseListener listener) {
		responseListeners.add(listener);
	}

	public void removeResponseListener(BasicUploaderResponseListener listener) {
		responseListeners.remove(listener);
	}
}
