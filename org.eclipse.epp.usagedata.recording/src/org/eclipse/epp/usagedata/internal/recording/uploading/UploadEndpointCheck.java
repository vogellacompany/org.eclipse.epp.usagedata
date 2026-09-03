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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

/**
 * Checks whether the upload location answers, so that turning the collector on
 * can say straight away when the data it gathers has nowhere to go.
 */
public class UploadEndpointCheck {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private UploadEndpointCheck() {
	}

	/**
	 * This method answers <code>true</code> only if the location responds with
	 * a success or a redirect. An error status counts as unreachable: the
	 * uploader keeps its data unless the server answers 200, so a location that
	 * responds only with 404 is no better than one that does not respond.
	 *
	 * @param url the upload location to check.
	 * @return whether the location could accept an upload.
	 */
	public static boolean isReachable(String url) {
		try {
			HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(TIMEOUT)
				.method("HEAD", BodyPublishers.noBody()) //$NON-NLS-1$
				.build();
			int status = client.send(request, BodyHandlers.discarding()).statusCode();
			return status >= 200 && status < 400;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		} catch (IOException | IllegalArgumentException e) {
			return false;
		}
	}
}
