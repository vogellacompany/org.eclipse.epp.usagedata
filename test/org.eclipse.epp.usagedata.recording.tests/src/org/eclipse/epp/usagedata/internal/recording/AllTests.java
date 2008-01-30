package org.eclipse.epp.usagedata.internal.recording;

import org.eclipse.epp.usagedata.internal.recording.filtering.AbstractUsageDataEventFilterTests;
import org.eclipse.epp.usagedata.internal.recording.filtering.FilterUtilsTests;
import org.eclipse.epp.usagedata.internal.recording.filtering.PreferencesBasedFilterTests;
import org.eclipse.epp.usagedata.internal.recording.uploading.BasicUploaderTests;
import org.eclipse.epp.usagedata.internal.recording.uploading.UsageDataFileReaderTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses( { 
	AbstractUsageDataEventFilterTests.class,
	FilterUtilsTests.class,
	PreferencesBasedFilterTests.class,
	BasicUploaderTests.class, 
	UsageDataFileReaderTests.class
})
public class AllTests {

}
