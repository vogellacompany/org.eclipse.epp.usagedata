package org.eclipse.epp.usagedata.recording.uploading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.epp.usagedata.gathering.events.UsageDataEvent;

public class UsageDataFileReader {
	private final BufferedReader reader;
 
	public UsageDataFileReader(File file) throws IOException {
		this(new FileInputStream(file));
	}

	public UsageDataFileReader(InputStream inputStream) throws IOException {
		this(new InputStreamReader(inputStream));
	}

	public UsageDataFileReader(Reader reader) throws IOException {
		this(new BufferedReader(reader));
	}
	
	public UsageDataFileReader(BufferedReader bufferedReader) throws IOException {
		reader = bufferedReader;
		reader.readLine(); // Clear out the header line.
	}

	public UsageDataEvent next() throws IOException {
		String line = reader.readLine();
		if (line == null) return null;
		String[] tokens = line.split("\\,");
		return new UsageDataEvent(tokens[0], tokens[1], tokens[4], tokens[2], tokens[3], Long.valueOf(tokens[5]));
	}

	public void close() throws IOException {
		reader.close();
	}

}
