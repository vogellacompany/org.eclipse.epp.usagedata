package org.eclipse.epp.usagedata.internal.ui;

import org.eclipse.epp.usagedata.internal.recording.UsageDataGatheringTests;
import org.eclipse.epp.usagedata.internal.ui.preview.UploadPreviewTests;
import org.eclipse.epp.usagedata.internal.ui.preview.UsageDataEventWrapperTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { 
	UsageDataEventWrapperTests.class,
	UploadPreviewTests.class,
	UsageDataGatheringTests.class
})
public class AllTests {

}
