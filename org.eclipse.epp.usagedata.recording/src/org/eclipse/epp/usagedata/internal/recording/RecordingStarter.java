package org.eclipse.epp.usagedata.internal.recording;

import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Connects the recorder to the {@link UsageDataService}. The reference to the
 * workbench holds this back until the workspace is set, since the recorder
 * needs the state location and the preferences.
 */
@Component(immediate = true, reference = @Reference(name = "workbench", service = IWorkbench.class))
public class RecordingStarter {

	private UsageDataService service;

	@Reference
	void setService(UsageDataService service) {
		this.service = service;
	}

	@Activate
	void activate() {
		UsageDataRecording.getRecorder().start();
		service.addUsageDataEventListener(UsageDataRecording.getRecorder());
	}

	@Deactivate
	void deactivate() {
		UsageDataRecording.getRecorder().stop();
		service.removeUsageDataEventListener(UsageDataRecording.getRecorder());
		UsageDataRecording.disposeSettings();
		UsageDataRecording.savePreferences();
	}
}
