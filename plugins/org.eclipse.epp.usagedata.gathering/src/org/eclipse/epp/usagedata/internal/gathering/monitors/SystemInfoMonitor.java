package org.eclipse.epp.usagedata.internal.gathering.monitors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator;
import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;

/**
 * This monitor captures information about the System. Specifically,
 * we capture:
 * <ul>
 * <li>Operating System</li>
 * <li>System Architecture</li>
 * <li>Window System</li>
 * <li>Locale</li>
 * <li>Number of processors available</li>
 * <li>And a number of system properties</li>
 * 
 */
public class SystemInfoMonitor implements UsageMonitor {
	
	private static final String SYSINFO = "sysinfo";
	
	private static final String INFO_PROCESSORS = "processors";
	private static final String INFO_LOCALE = "locale";
	private static final String INFO_WS = "ws";
	private static final String INFO_ARCH = "arch";
	private static final String INFO_OS = "os";
	
	/**
	 * This property contains a list of system properties that
	 * we obtain the values for.
	 * <p>
	 * Many of the system properties contain information like paths
	 * which may provide us with too much information about a particular
	 * user. We avoid inadvertently including any of this information 
	 * by being particular about the actual properties we capture.
	 * AFAIK, none of these properties will likely contain any information
	 * of a personal nature.
	 */
	private static final String[] SYSTEM_PROPERTIES = {
		"java.runtime.name",
		"java.runtime.version",
		"java.specification.name",
		"java.specification.vendor",
		"java.specification.version",
		"java.vendor",
		"java.version",
		"java.vm.info",
		"java.vm.name",
		"java.vm.specification.name",
		"java.vm.specification.vendor",
		"java.vm.specification.version",
		"java.vm.vendor",
		"java.vm.version"
	};
	
	public void startMonitoring(UsageDataService usageDataService) {
		/*
		 * If you look deep enough into the call chain, there is some
		 * possibility that these Platform.xxx methods can cause a
		 * runtime exception. We'll catch and log that potential exception.
		 */
		try {
			usageDataService.recordEvent(INFO_OS, SYSINFO, Platform.getOS(), null);
			usageDataService.recordEvent(INFO_ARCH, SYSINFO, Platform.getOSArch(), null);
			usageDataService.recordEvent(INFO_WS, SYSINFO, Platform.getWS(), null);
			usageDataService.recordEvent(INFO_LOCALE, SYSINFO, Platform.getNL(), null);
		} catch (Exception e) {
			UsageDataCaptureActivator.getDefault().logException("Exception occurred while obtaining platform properties.", e);
		}
		
		usageDataService.recordEvent(INFO_PROCESSORS, SYSINFO, String.valueOf(Runtime.getRuntime().availableProcessors()), null);
		
		for (String property : SYSTEM_PROPERTIES) {
			usageDataService.recordEvent(property, SYSINFO, System.getProperty(property), null);
		}
	}

	public void stopMonitoring() {
	}

}
