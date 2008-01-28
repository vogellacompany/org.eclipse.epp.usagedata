package org.eclipse.epp.usagedata.internal.recording;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;

public class UsageDataRecorderUtils {

	private UsageDataRecorderUtils() {
	}

	public static void writeHeader(FileWriter writer) throws IOException {
		writer.write("what");
		writer.write(",");
		writer.write("kind");
		writer.write(",");
		writer.write("bundleId");
		writer.write(",");
		writer.write("bundleVersion");
		writer.write(",");
		writer.write("description");
		writer.write(",");
		writer.write("time");
		writer.write("\n");
	}
	
	/**
	 * Dump the event on the writer. This method assumes
	 * exclusive access to the writer.
	 * 
	 * @param writer target for the event information.
	 * @param event event to write.
	 * @throws IOException
	 */
	public static void writeEvent(Writer writer, UsageDataEvent event) throws IOException {
		writer.write(event.what);
		writer.write(",");
		writer.write(event.kind);
		writer.write(",");
		writer.write(event.bundleId != null ? event.bundleId : "");
		writer.write(",");
		writer.write(event.bundleVersion != null ? event.bundleVersion : "");
		writer.write(",");
		writer.write(event.description != null ? event.description : "");
		writer.write(",");
		writer.write(String.valueOf(event.when));
		writer.write("\n");
	}
}
