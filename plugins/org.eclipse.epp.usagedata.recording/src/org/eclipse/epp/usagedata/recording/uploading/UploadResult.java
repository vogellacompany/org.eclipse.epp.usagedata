package org.eclipse.epp.usagedata.recording.uploading;

public class UploadResult {

	public static final int CANCELLED = 0;

	private final int returnCode;

	/**
	 * 
	 * @param returnCode
	 *            code describing result of operation; typically an HTTP return
	 *            code that results from the upload operation.
	 */
	public UploadResult(int returnCode) {
		this.returnCode = returnCode;
	}

	public int getReturnCode() {
		return returnCode;
	}

}
