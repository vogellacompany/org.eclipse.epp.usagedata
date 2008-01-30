package org.eclipse.epp.usagedata.internal.gathering;

import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataServiceLifecycleTests;
import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataServiceTests;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettingsTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This test suite must be run in the workbench.
 * 
 * @author Wayne Beaton 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	UsageDataServiceLifecycleTests.class,
	UsageDataServiceTests.class,
	UsageDataCaptureSettingsTests.class
})
public class UsageDataGatheringTests {

}
