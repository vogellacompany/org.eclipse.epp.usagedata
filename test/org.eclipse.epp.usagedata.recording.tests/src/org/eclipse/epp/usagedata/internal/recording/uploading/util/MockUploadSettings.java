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
package org.eclipse.epp.usagedata.internal.recording.uploading.util;

import org.eclipse.epp.usagedata.internal.recording.filtering.UsageDataEventFilter;
import org.eclipse.epp.usagedata.internal.recording.settings.UploadSettings;

public class MockUploadSettings implements UploadSettings {

	public UsageDataEventFilter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUploadUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getWorkspaceId() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasUserAcceptedTermsOfUse() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLoggingServerActivity() {
		// TODO Auto-generated method stub
		return false;
	}

}
