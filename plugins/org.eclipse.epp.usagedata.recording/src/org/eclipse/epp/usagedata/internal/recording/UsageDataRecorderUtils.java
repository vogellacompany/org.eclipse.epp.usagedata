/*******************************************************************************
 * Copyright (c) 2009 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.recording;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;

public class UsageDataRecorderUtils {
	
	/**
	 * Write a header onto the {@link Writer}.
	 * 
	 * @param writer
	 *            a {@link Writer}. Must not be <code>null</code>.
	 * @throws IOException
	 *             if writing to the {@link Writer} fails.
	 */
	public static void writeHeader(Writer writer) throws IOException {
		writer.write("what"); //$NON-NLS-1$
		writer.write(","); //$NON-NLS-1$
		writer.write("kind"); //$NON-NLS-1$
		writer.write(","); //$NON-NLS-1$
		writer.write("bundleId"); //$NON-NLS-1$
		writer.write(","); //$NON-NLS-1$
		writer.write("bundleVersion"); //$NON-NLS-1$
		writer.write(","); //$NON-NLS-1$
		writer.write("description"); //$NON-NLS-1$
		writer.write(","); //$NON-NLS-1$
		writer.write("time"); //$NON-NLS-1$
		writer.write("\n"); //$NON-NLS-1$
	}
	
	/**
	 * Dump the event on the writer. This method assumes exclusive access to the
	 * writer.
	 * 
	 * @param writer
	 *            target for the event information. Must not be
	 *            <code>null</code>.
	 * @param event
	 *            event to write. Must not be <code>null</code>.
	 * 
	 * @throws IOException
	 *             if writing to the {@link Writer} fails.
	 */
	public static void writeEvent(Writer writer, UsageDataEvent event) throws IOException {
		writer.write(event.what);
		writer.write(","); //$NON-NLS-1$
		writer.write(event.kind);
		writer.write(","); //$NON-NLS-1$
		writer.write(event.bundleId != null ? event.bundleId : ""); //$NON-NLS-1$
		writer.write(","); //$NON-NLS-1$
		writer.write(event.bundleVersion != null ? event.bundleVersion : ""); //$NON-NLS-1$
		writer.write(","); //$NON-NLS-1$
		writer.write(event.description != null ? encode(event.description) : ""); //$NON-NLS-1$
		writer.write(","); //$NON-NLS-1$
		writer.write(String.valueOf(event.when));
		writer.write("\n"); //$NON-NLS-1$
	}

	/**
	 * This method encodes the description so that it can be successfully parsed
	 * as a single entry by a CSV parser. This takes care of most of the
	 * escape characters to ensure that the output gets dumped onto a single
	 * line. We probably take care of more escape characters than we really
	 * need to, but that's better than the opposite...
	 * <p>
	 * The escaped characters should only be an issue in the rare case when
	 * a status message contains them. Most of our monitors do not generate
	 * text that contains them.
	 * 
	 * @param description
	 *            a {@link String}. Must not be <code>null</code>.
	 * 
	 * @return a {@link String}.
	 */
	public static String encode(String description) {
		StringBuilder builder = new StringBuilder();
		builder.append('"');
		for(int index=0;index<description.length();index++) {
			char next = description.charAt(index);
			switch (next) {
			case '"' : 
				builder.append('"');
				builder.append(next);
				break;
			case '\\' :
				builder.append("\\\\"); //$NON-NLS-1$
				break;
			case '\n' :
				builder.append("\\n"); //$NON-NLS-1$
				break;
			case '\r' :
				builder.append("\\r"); //$NON-NLS-1$
				break;
			case '\b' :
				builder.append("\\b"); //$NON-NLS-1$
				break;
			case '\t' :
				builder.append("\\t"); //$NON-NLS-1$
				break;
			case '\f' :
				builder.append("\\f"); //$NON-NLS-1$
				break;				
			default :
				builder.append(next);
			}
		}
		builder.append('"');
		return builder.toString();
	}

	/**
	 * Split the String parameter into substrings. The parameter is assumed to
	 * be in CSV format. Comma separators are assumed. An entry that starts and
	 * ends with double-quotes may contain commas or escaped double-quotes.
	 * Double-quotes are escaped by putting two one-after-the-other. This method
	 * assumes that there are no extraneous white spaces (i.e. leading or
	 * trailing) in the input.
	 * <p>
	 * Note that we don't worry about trying to re-translate escaped characters
	 * back into their unescaped form. We assume that this method is used exclusively
	 * for displaying events in a preview pane, or for applying filters; we don't
	 * need to translate the escaped characters in either of these cases.
	 * <p>
	 * The value: "first,\"\"\"second\"\", third\",fourth" will be parsed into
	 * three strings: "first", "\"second\", third", and "fourth".
	 * 
	 * @param line
	 *            a {@link String}. Must not be <code>null</code>.
	 * 
	 * @return an array of {@link String}s.
	 */
	public static String[] splitLine(String line) {
		List<String> strings = new java.util.ArrayList<String>(); 
		Matcher matcher = Pattern.compile("(\"([^\"]|\"\")*\"|[^,]*)(,|$)").matcher(line); //$NON-NLS-1$
		while (matcher.find()) {
			String string = matcher.group();
			// Remove leading commas.
			string = string.replaceAll(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			// Remove optional leading and trailing double-quotes.
			string = string.replaceAll("^?\"(.*)\"$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
			// Replace double double-quotes with a single double-quote
			string = string.replaceAll("\"\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			
			strings.add( string ); 
		}
		return (String[]) strings.toArray(new String[strings.size()]);
	}
}
