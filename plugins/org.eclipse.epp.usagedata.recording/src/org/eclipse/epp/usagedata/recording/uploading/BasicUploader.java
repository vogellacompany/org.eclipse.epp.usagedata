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
package org.eclipse.epp.usagedata.recording.uploading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.epp.usagedata.recording.Activator;
import org.eclipse.epp.usagedata.recording.settings.UsageDataRecordingSettings;

/**
 * Instances of the {@link BasicUploader} class are responsible for
 * uploading a set of files to the server.
 * 
 * @author Wayne Beaton
 *
 */
public class BasicUploader implements Uploader {

	private boolean uploadInProgress = false;

	/**
	 * Uploads are done with a {@link Job} running in the background
	 * at a relatively low priority. The intent is to make the user
	 * as blissfully unaware that anything is happening as possible.
	 */
	public void startUpload(final UploadManager uploadManager, final File[] files) {
		if (uploadInProgress) return;
		uploadInProgress = true;
		getSettings().setLastUploadTime();
		Job job = new Job("Uploading usage data...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				doUpload(files, monitor);
				uploadInProgress = false;
				uploadManager.uploadComplete();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.LONG);
		job.schedule();
	}
	
	/**
	 * I can envision a time when we may want to upload something other than files.
	 * We may, for example, want to upload an in-memory representation of the files.
	 * For now, in the spirit of having something that works is better than
	 * overengineering something you may not need, we're just dealing with files.
	 * 
	 * TODO All of this can be moved to an UploadJob class.
	 * @param files
	 * @param monitor
	 */
	protected void doUpload(File[] files, IProgressMonitor monitor) {
		/*
		 * The files that we have been provided with were determined while the recorder
		 * was suspended. We should be safe to work with these files without worrying
		 * that other threads are messing with them. We do need to consider that other
		 * processes running outside of our JVM may be messing with these files and
		 * anticipate errors accordingly.
		 */

//		File zipFile = File.createTempFile("upload", "zip");
//		try {
//			if (!zipFile.exists()) zipFile.createNewFile(); // TODO Do we need this?
//			ZipOutputStream output = new ZipOutputStream(new FileOutputStream(zipFile));
//			for (File file : files) {
//				addFileToZip(output, file);
//			}
//			output.close();
//		} catch (IOException e) {
//			handleException(e);
//		}
		
		/*
		 * There appears to be some mechanism on some versions of HttpClient that
		 * allows the insertion of compression technology. For now, we don't worry
		 * about compressing our output; we can worry about that later.
		 */
		
		PostMethod post = new PostMethod(getSettings().getUploadUrl());
		post.setRequestHeader("USERID", getSettings().getUserId());
		post.setRequestHeader("WORKSPACEID", getSettings().getWorkspaceId());
		post.setRequestHeader("TIME", String.valueOf(System.currentTimeMillis()));
		// TODO Set the user agent header
		if ("true".equals(getSettings().isLoggingServerActivity())) {
			post.setRequestHeader("LOGGING", "true");
		}
		post.setRequestEntity(new MultipartRequestEntity(getFileParts(files), post.getParams()));
		
		// Configure the HttpClient to timeout after 4 seconds.
		HttpClientParams parameters = new HttpClientParams();
		// TODO Make the socket timeout a preference.
		parameters.setSoTimeout(4000); // "So" means "socket"; who knew?
		
		int result = 0;
		try {
			long start = System.currentTimeMillis();
			result = new HttpClient(parameters).executeMethod(post);
			long duration = System.currentTimeMillis() - start;
			
			Activator.getDefault().getLog()
				.log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
					MessageFormat.format("Usage data uploaded to {0} in {1} milliseconds", getSettings().getUploadUrl(), duration)));
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO What do we do with the response?
		try {
			// TODO Remove this useful-for-debugging hack. 
			String response = post.getResponseBodyAsString();
			if (response != null) System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Check the result. HTTP return code of 200 means success.
		if (result == 200) {
			for (File file : files) {
				// TODO what if file delete fails?
				if (file.exists()) file.delete();
			}
		}
	}

	private Part[] getFileParts(File[] files) {
		List<Part> fileParts = new ArrayList<Part>();
		for (File file : files) {
			try {
				// TODO Hook in a custom FilePart that filters contents.
				fileParts.add(new FilePart("uploads[]", file));
			} catch (FileNotFoundException e) {
				// If an exception occurs while creating the FilePart, 
				// ignore the error and move on. If this has happened,
				// then another process may have deleted or moved the file.
			}
		}
		return (Part[]) fileParts.toArray(new Part[fileParts.size()]);
	}	

	private UsageDataRecordingSettings getSettings() {
		return Activator.getDefault().getSettings();
	}

	public boolean isUploadInProgress() {
		return uploadInProgress;
	}
}
