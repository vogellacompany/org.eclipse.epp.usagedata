package org.eclipse.epp.usagedata.internal.recording.uploading;

import java.io.StringReader;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.junit.Test;
import static org.junit.Assert.*;

public class UsageDataFileReaderTests {
	@Test
	public void testIterate() throws Exception {
		String content = "what,kind,bundleId,bundleVersion,description,time\n"
				+ "what,kind,bundleId,bundleVersion,description,123456\n"
				+ "what,kind,bundleId,bundleVersion,description,123456\n"
				+ "what,kind,bundleId,bundleVersion,description,123456\n";
		
		final StringBuilder builder = new StringBuilder();

		UsageDataFileReader reader = new UsageDataFileReader(new StringReader(content));
		reader.iterate(new UsageDataFileReader.Iterator() {

			public void event(String line, UsageDataEvent event) {
				builder.append(line);
				builder.append("\n");
			}

			public void header(String header) {
				builder.append(header);
				builder.append("\n");
			}
		});
		assertEquals(content, builder.toString());
	}
}
