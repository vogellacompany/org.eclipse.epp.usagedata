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
package org.eclipse.epp.usagedata.internal.ui;

import org.eclipse.epp.usagedata.internal.ui.preferences.UsageDataCapturePreferencesPageTests;
import org.eclipse.epp.usagedata.internal.ui.preview.UploadPreviewTests;
import org.eclipse.epp.usagedata.internal.ui.preview.UsageDataEventWrapperTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { 
	UsageDataEventWrapperTests.class,
	UploadPreviewTests.class,
	UsageDataCapturePreferencesPageTests.class
})
public class AllTests {

}
