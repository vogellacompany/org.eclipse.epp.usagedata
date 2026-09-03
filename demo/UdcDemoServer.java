/*******************************************************************************
 * Copyright (c) 2026 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A throwaway Usage Data Collector backend for demonstrations. It accepts the
 * uploads a real Eclipse IDE sends, keeps them in memory, and serves a live
 * dashboard of what arrived.
 *
 * <pre>
 *   java demo/UdcDemoServer.java [port]
 * </pre>
 *
 * Point the IDE at http://localhost:&lt;port&gt;/upload.php through
 * Preferences &gt; Usage Data Collector &gt; Uploading, then press "Upload Now".
 * Open http://localhost:&lt;port&gt;/ to watch the data land.
 *
 * Nothing is written to disk and nothing outlives the process, which is the
 * point: it is a demo, not a collector.
 */
public class UdcDemoServer {

	private static final int DEFAULT_PORT = 8642 + 1;

	/** Every event row that has been uploaded, in arrival order. */
	private static final ConcurrentLinkedQueue<Event> EVENTS = new ConcurrentLinkedQueue<>();

	/** One entry per upload request, newest last. */
	private static final ConcurrentLinkedQueue<Upload> UPLOADS = new ConcurrentLinkedQueue<>();

	private static final AtomicLong BYTES_RECEIVED = new AtomicLong();
	private static final long STARTED_AT = System.currentTimeMillis();

	private record Event(String client, String what, String kind, String bundleId, String bundleVersion,
			String description, long time) {
	}

	private record Upload(long receivedAt, String userId, String workspaceId, String userAgent, long clientTime,
			int fileCount, int eventCount, long bytes, Map<String, String> preferences) {
	}

	public static void main(String[] args) throws IOException {
		int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/upload.php", UdcDemoServer::handleUpload);
		server.createContext("/api/data", UdcDemoServer::handleData);
		server.createContext("/", UdcDemoServer::handleDashboard);
		server.setExecutor(null);
		server.start();

		System.out.println("Usage Data Collector demo backend");
		System.out.println("  upload endpoint  http://localhost:" + port + "/upload.php");
		System.out.println("  dashboard        http://localhost:" + port + "/");
		System.out.println();
		System.out.println("Set the upload endpoint in Preferences > Usage Data Collector > Uploading,");
		System.out.println("then press \"Upload Now\". Ctrl+C stops the server and forgets everything.");
	}

	// ---------------------------------------------------------------- upload

	/**
	 * Accepts the multipart upload an Eclipse IDE sends. Answering 200 is what
	 * tells the IDE the data arrived, so it deletes its staged files.
	 */
	private static void handleUpload(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			respond(exchange, 405, "text/plain", "Send the usage data with POST.\n");
			return;
		}

		byte[] body = exchange.getRequestBody().readAllBytes();
		String contentType = header(exchange, "Content-Type", "");
		String boundary = boundaryOf(contentType);

		List<String> csvParts = boundary == null
			? List.of(new String(body, StandardCharsets.UTF_8))
			: splitMultipart(new String(body, StandardCharsets.UTF_8), boundary);

		String userId = header(exchange, "USERID", "unknown");
		String workspaceId = header(exchange, "WORKSPACEID", "unknown");
		String client = userId + "/" + workspaceId;

		int eventCount = 0;
		for (String csv : csvParts) {
			eventCount += parseCsv(client, csv);
		}

		Upload upload = new Upload(System.currentTimeMillis(), userId, workspaceId,
				header(exchange, "User-Agent", "unknown"),
				parseLong(header(exchange, "TIME", "0")), csvParts.size(), eventCount, body.length,
				preferencesOf(exchange));
		UPLOADS.add(upload);
		BYTES_RECEIVED.addAndGet(body.length);

		System.out.printf("received %d files, %d events, %d bytes from %s%n", csvParts.size(), eventCount, body.length,
				upload.userId());

		// The uploader echoes any "log:" line back into the Eclipse error log.
		respond(exchange, 200, "text/plain", "log:received " + eventCount + " events\n");
	}

	/**
	 * Collects the preference values the IDE reports about itself. Anything sent
	 * as an X-UDC-* header shows up on the dashboard, so adding a preference to
	 * the upload needs no change here.
	 */
	private static Map<String, String> preferencesOf(HttpExchange exchange) {
		Map<String, String> preferences = new LinkedHashMap<>();
		exchange.getRequestHeaders().forEach((name, values) -> {
			if (name.regionMatches(true, 0, "X-UDC-", 0, 6) && !values.isEmpty()) {
				preferences.put(name.substring(6).toLowerCase(Locale.ROOT), values.get(0));
			}
		});
		return preferences;
	}

	private static String boundaryOf(String contentType) {
		int index = contentType.indexOf("boundary=");
		if (index < 0) return null;
		String boundary = contentType.substring(index + "boundary=".length()).trim();
		if (boundary.startsWith("\"") && boundary.endsWith("\"") && boundary.length() > 1) {
			boundary = boundary.substring(1, boundary.length() - 1);
		}
		return boundary;
	}

	/** Pulls the body out of each part; the headers of a part end at a blank line. */
	private static List<String> splitMultipart(String body, String boundary) {
		List<String> parts = new ArrayList<>();
		for (String chunk : body.split("--" + java.util.regex.Pattern.quote(boundary))) {
			int blankLine = chunk.indexOf("\r\n\r\n");
			if (blankLine < 0) continue;
			String content = chunk.substring(blankLine + 4);
			if (content.endsWith("\r\n")) content = content.substring(0, content.length() - 2);
			if (!content.isBlank()) parts.add(content);
		}
		return parts;
	}

	/** Reads the recorded CSV. The header row names the columns and is skipped. */
	private static int parseCsv(String client, String csv) {
		int count = 0;
		for (String line : csv.split("\r?\n")) {
			if (line.isBlank() || line.startsWith("what,kind")) continue;
			List<String> fields = splitCsvLine(line);
			if (fields.size() < 6) continue;
			EVENTS.add(new Event(client, fields.get(0), fields.get(1), fields.get(2), fields.get(3),
					fields.get(4), parseLong(fields.get(5))));
			count++;
		}
		return count;
	}

	/** Splits one CSV row, honouring the quoting the recorder applies to the description. */
	private static List<String> splitCsvLine(String line) {
		List<String> fields = new ArrayList<>();
		StringBuilder field = new StringBuilder();
		boolean quoted = false;
		for (int i = 0; i < line.length(); i++) {
			char character = line.charAt(i);
			if (quoted) {
				if (character == '"') {
					boolean escaped = i + 1 < line.length() && line.charAt(i + 1) == '"';
					if (escaped) {
						field.append('"');
						i++;
					} else {
						quoted = false;
					}
				} else {
					field.append(character);
				}
			} else if (character == '"') {
				quoted = true;
			} else if (character == ',') {
				fields.add(field.toString());
				field.setLength(0);
			} else {
				field.append(character);
			}
		}
		fields.add(field.toString());
		return fields;
	}

	// ------------------------------------------------------------------ data

	private static void handleData(HttpExchange exchange) throws IOException {
		respond(exchange, 200, "application/json", buildJson());
	}

	private static String buildJson() {
		List<Event> events = new ArrayList<>(EVENTS);
		List<Upload> uploads = new ArrayList<>(UPLOADS);

		Map<String, Long> byKind = countBy(events, Event::kind);
		Map<String, Long> byVerb = countBy(events, Event::what);
		Map<String, Long> commands = countBy(events.stream().filter(e -> "command".equals(e.kind())).toList(),
				Event::description);
		Map<String, Long> views = countBy(events.stream().filter(e -> "view".equals(e.kind())).toList(),
				Event::description);
		Map<String, Long> editors = countBy(events.stream().filter(e -> "editor".equals(e.kind())).toList(),
				Event::description);
		Map<String, Long> bundles = countBy(events.stream().filter(e -> "bundle".equals(e.kind())).toList(),
				Event::bundleId);

		long sessions = events.stream().filter(e -> "sysinfo".equals(e.kind()) && "os".equals(e.what())).count();

		StringBuilder json = new StringBuilder();
		json.append('{');
		json.append("\"startedAt\":").append(STARTED_AT).append(',');
		json.append("\"now\":").append(System.currentTimeMillis()).append(',');
		json.append("\"eventCount\":").append(events.size()).append(',');
		json.append("\"uploadCount\":").append(uploads.size()).append(',');
		json.append("\"bytesReceived\":").append(BYTES_RECEIVED.get()).append(',');
		json.append("\"sessions\":").append(sessions).append(',');
		json.append("\"byKind\":").append(toJson(byKind)).append(',');
		json.append("\"byVerb\":").append(toJson(byVerb)).append(',');
		json.append("\"commands\":").append(toJson(topOf(commands, 12))).append(',');
		json.append("\"views\":").append(toJson(topOf(views, 8))).append(',');
		json.append("\"editors\":").append(toJson(topOf(editors, 8))).append(',');
		json.append("\"bundles\":").append(toJson(topOf(bundles, 10))).append(',');
		json.append("\"users\":").append(uploads.stream().map(Upload::userId).distinct().count()).append(',');
		json.append("\"workspaces\":")
			.append(uploads.stream().map(u -> u.userId() + "/" + u.workspaceId()).distinct().count()).append(',');
		json.append("\"javaVersions\":")
			.append(toJson(countUsersBy(events, uploads, "java.version", UdcDemoServer::majorJavaVersion))).append(',');
		json.append("\"usersPerDay\":").append(usersPerDayJson(events, uploads)).append(',');
		json.append("\"operatingSystems\":")
			.append(toJson(countUsersBy(events, uploads, "os", value -> value))).append(',');
		json.append("\"clients\":").append(clientsJson(events, uploads)).append(',');
		json.append("\"timeline\":").append(timelineJson(events)).append(',');
		json.append("\"uploads\":").append(uploadsJson(uploads)).append(',');
		json.append("\"recent\":").append(recentJson(events));
		json.append('}');
		return json.toString();
	}

	/**
	 * Counts workstations, not events, per reported value. A workstation that
	 * reports twice still counts once, and its most recent report is the one
	 * that stands, so the numbers add up to the number of workstations.
	 */
	private static Map<String, Long> countUsersBy(List<Event> events, List<Upload> uploads, String key,
			java.util.function.Function<String, String> normalise) {
		Map<String, String> clientToUser = new LinkedHashMap<>();
		for (Upload upload : uploads) {
			clientToUser.put(upload.userId() + "/" + upload.workspaceId(), upload.userId());
		}

		// Later events win, so a workstation that upgraded counts as upgraded.
		Map<String, String> userToValue = new LinkedHashMap<>();
		for (Event event : events) {
			if (!"sysinfo".equals(event.kind()) || !key.equals(event.what())) continue;
			String user = clientToUser.get(event.client());
			if (user == null) continue;
			userToValue.put(user, normalise.apply(event.description()));
		}

		Map<String, Long> counts = new LinkedHashMap<>();
		for (String value : userToValue.values()) {
			counts.merge(value, 1L, Long::sum);
		}
		return sortByCount(counts);
	}

	/**
	 * Counts the workstations that were active on each day. A workstation that
	 * worked all day still counts once, so the bars read as people rather than
	 * as how busy any one of them was.
	 */
	private static String usersPerDayJson(List<Event> events, List<Upload> uploads) {
		Map<String, String> clientToUser = new LinkedHashMap<>();
		for (Upload upload : uploads) {
			clientToUser.put(upload.userId() + "/" + upload.workspaceId(), upload.userId());
		}

		TreeMap<String, java.util.Set<String>> byDay = new TreeMap<>();
		for (Event event : events) {
			if (event.time() <= 0) continue;
			String user = clientToUser.get(event.client());
			if (user == null) continue;
			String day = Instant.ofEpochMilli(event.time()).atZone(ZoneId.systemDefault()).toLocalDate().toString();
			byDay.computeIfAbsent(day, key -> new java.util.LinkedHashSet<>()).add(user);
		}

		StringBuilder json = new StringBuilder("[");
		boolean first = true;
		for (Map.Entry<String, java.util.Set<String>> entry : byDay.entrySet()) {
			if (!first) json.append(',');
			first = false;
			json.append("{\"day\":").append(quote(entry.getKey()))
				.append(",\"users\":").append(entry.getValue().size()).append('}');
		}
		return json.append(']').toString();
	}

	/** Answers the feature-release number, so that 25.0.3 and 25.0.7 are one bar. */
	private static String majorJavaVersion(String version) {
		if (version == null || version.isBlank()) return "unknown";
		int dot = version.indexOf('.');
		String major = dot < 0 ? version : version.substring(0, dot);
		return major.isBlank() ? "unknown" : "Java " + major.trim();
	}

	/**
	 * Reports one entry per reporting workspace. Identity, configuration and the
	 * system-info block all belong to the client that sent them: merging them
	 * across clients would describe a workstation that does not exist.
	 */
	private static String clientsJson(List<Event> events, List<Upload> uploads) {
		Map<String, Map<String, String>> systemInfo = new LinkedHashMap<>();
		Map<String, Integer> eventCounts = new LinkedHashMap<>();
		Map<String, Long> sessions = new LinkedHashMap<>();
		for (Event event : events) {
			eventCounts.merge(event.client(), 1, Integer::sum);
			if (!"sysinfo".equals(event.kind())) continue;
			systemInfo.computeIfAbsent(event.client(), key -> new LinkedHashMap<>())
				.putIfAbsent(event.what(), event.description());
			if ("os".equals(event.what())) sessions.merge(event.client(), 1L, Long::sum);
		}

		// The newest upload from a client is the one whose settings still apply.
		Map<String, Upload> latest = new LinkedHashMap<>();
		Map<String, Integer> uploadCounts = new LinkedHashMap<>();
		for (Upload upload : uploads) {
			String client = upload.userId() + "/" + upload.workspaceId();
			latest.put(client, upload);
			uploadCounts.merge(client, 1, Integer::sum);
		}

		// One card per workstation; its workspaces are folded in underneath.
		Map<String, List<String>> userToClients = new LinkedHashMap<>();
		for (Map.Entry<String, Upload> entry : latest.entrySet()) {
			userToClients.computeIfAbsent(entry.getValue().userId(), key -> new ArrayList<>()).add(entry.getKey());
		}

		StringBuilder json = new StringBuilder("[");
		boolean first = true;
		for (Map.Entry<String, List<String>> user : userToClients.entrySet()) {
			List<String> clients = user.getValue();
			Upload newest = clients.stream().map(latest::get)
				.max(Comparator.comparingLong(Upload::receivedAt)).orElse(null);
			if (newest == null) continue;

			int userEvents = 0, userUploads = 0;
			long userSessions = 0;
			StringBuilder workspaces = new StringBuilder("[");
			for (int i = 0; i < clients.size(); i++) {
				String client = clients.get(i);
				userEvents += eventCounts.getOrDefault(client, 0);
				userUploads += uploadCounts.getOrDefault(client, 0);
				userSessions += sessions.getOrDefault(client, 0L);
				if (i > 0) workspaces.append(',');
				workspaces.append("{\"workspaceId\":").append(quote(latest.get(client).workspaceId()))
					.append(",\"events\":").append(eventCounts.getOrDefault(client, 0))
					.append(",\"sessions\":").append(sessions.getOrDefault(client, 0L))
					.append(",\"lastSeen\":").append(latest.get(client).receivedAt()).append('}');
			}
			workspaces.append(']');

			if (!first) json.append(',');
			first = false;
			json.append('{');
			json.append("\"userId\":").append(quote(user.getKey())).append(',');
			json.append("\"userAgent\":").append(quote(newest.userAgent())).append(',');
			json.append("\"lastSeen\":").append(newest.receivedAt()).append(',');
			json.append("\"uploads\":").append(userUploads).append(',');
			json.append("\"events\":").append(userEvents).append(',');
			json.append("\"sessions\":").append(userSessions).append(',');
			json.append("\"workspaces\":").append(workspaces).append(',');
			json.append("\"preferences\":").append(toJsonStrings(newest.preferences())).append(',');
			json.append("\"systemInfo\":").append(toJsonStrings(
				systemInfo.getOrDefault(user.getKey() + "/" + newest.workspaceId(), Map.of())));
			json.append('}');
		}
		return json.append(']').toString();
	}

	/** Buckets the events by minute, so the dashboard can draw when things happened. */
	private static String timelineJson(List<Event> events) {
		TreeMap<Long, long[]> buckets = new TreeMap<>();
		for (Event event : events) {
			if (event.time() <= 0) continue;
			long minute = event.time() / 60000L;
			long[] counts = buckets.computeIfAbsent(minute, key -> new long[2]);
			counts[0]++;
			if (!"bundle".equals(event.kind()) && !"sysinfo".equals(event.kind())) counts[1]++;
		}
		StringBuilder json = new StringBuilder("[");
		boolean first = true;
		for (Map.Entry<Long, long[]> entry : buckets.entrySet()) {
			if (!first) json.append(',');
			first = false;
			json.append("{\"minute\":").append(entry.getKey() * 60000L)
				.append(",\"total\":").append(entry.getValue()[0])
				.append(",\"interaction\":").append(entry.getValue()[1]).append('}');
		}
		return json.append(']').toString();
	}

	private static String uploadsJson(List<Upload> uploads) {
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < uploads.size(); i++) {
			Upload upload = uploads.get(i);
			if (i > 0) json.append(',');
			json.append('{');
			json.append("\"receivedAt\":").append(upload.receivedAt()).append(',');
			json.append("\"userId\":").append(quote(upload.userId())).append(',');
			json.append("\"workspaceId\":").append(quote(upload.workspaceId())).append(',');
			json.append("\"userAgent\":").append(quote(upload.userAgent())).append(',');
			json.append("\"clientTime\":").append(upload.clientTime()).append(',');
			json.append("\"fileCount\":").append(upload.fileCount()).append(',');
			json.append("\"eventCount\":").append(upload.eventCount()).append(',');
			json.append("\"bytes\":").append(upload.bytes()).append(',');
			json.append("\"preferences\":").append(toJsonStrings(upload.preferences()));
			json.append('}');
		}
		return json.append(']').toString();
	}

	private static String recentJson(List<Event> events) {
		int from = Math.max(0, events.size() - 40);
		List<Event> recent = new ArrayList<>(events.subList(from, events.size()));
		recent.sort(Comparator.comparingLong(Event::time).reversed());
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < recent.size(); i++) {
			Event event = recent.get(i);
			if (i > 0) json.append(',');
			json.append("{\"what\":").append(quote(event.what()))
				.append(",\"kind\":").append(quote(event.kind()))
				.append(",\"bundleId\":").append(quote(event.bundleId()))
				.append(",\"description\":").append(quote(event.description()))
				.append(",\"time\":").append(event.time()).append('}');
		}
		return json.append(']').toString();
	}

	private static <T> Map<String, Long> countBy(List<Event> events, java.util.function.Function<Event, String> key) {
		Map<String, Long> counts = new LinkedHashMap<>();
		for (Event event : events) {
			String value = key.apply(event);
			if (value == null || value.isBlank()) value = "(none)";
			counts.merge(value, 1L, Long::sum);
		}
		return sortByCount(counts);
	}

	private static Map<String, Long> sortByCount(Map<String, Long> counts) {
		Map<String, Long> sorted = new LinkedHashMap<>();
		counts.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
			.forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
		return sorted;
	}

	private static Map<String, Long> topOf(Map<String, Long> counts, int limit) {
		Map<String, Long> top = new LinkedHashMap<>();
		counts.entrySet().stream().limit(limit).forEach(entry -> top.put(entry.getKey(), entry.getValue()));
		return top;
	}

	private static String toJson(Map<String, Long> counts) {
		StringBuilder json = new StringBuilder("{");
		boolean first = true;
		for (Map.Entry<String, Long> entry : counts.entrySet()) {
			if (!first) json.append(',');
			first = false;
			json.append(quote(entry.getKey())).append(':').append(entry.getValue());
		}
		return json.append('}').toString();
	}

	private static String toJsonStrings(Map<String, String> values) {
		StringBuilder json = new StringBuilder("{");
		boolean first = true;
		for (Map.Entry<String, String> entry : values.entrySet()) {
			if (!first) json.append(',');
			first = false;
			json.append(quote(entry.getKey())).append(':').append(quote(entry.getValue()));
		}
		return json.append('}').toString();
	}

	private static String quote(String value) {
		if (value == null) return "\"\"";
		StringBuilder quoted = new StringBuilder("\"");
		for (int i = 0; i < value.length(); i++) {
			char character = value.charAt(i);
			switch (character) {
				case '"' -> quoted.append("\\\"");
				case '\\' -> quoted.append("\\\\");
				case '\n' -> quoted.append("\\n");
				case '\r' -> quoted.append("\\r");
				case '\t' -> quoted.append("\\t");
				default -> {
					if (character < 0x20) {
						quoted.append(String.format("\\u%04x", (int) character));
					} else {
						quoted.append(character);
					}
				}
			}
		}
		return quoted.append('"').toString();
	}

	// ------------------------------------------------------------- dashboard

	private static void handleDashboard(HttpExchange exchange) throws IOException {
		if (!"/".equals(exchange.getRequestURI().getPath())) {
			respond(exchange, 404, "text/plain", "Nothing here. The dashboard is at /\n");
			return;
		}
		respond(exchange, 200, "text/html; charset=utf-8", dashboardHtml());
	}

	/**
	 * Reads the dashboard from the file beside this one when it is there, so the
	 * page can be edited without restarting, and falls back to a message that
	 * says where it should be.
	 */
	private static String dashboardHtml() {
		for (Path candidate : List.of(Path.of("demo", "dashboard.html"), Path.of("dashboard.html"))) {
			try {
				if (Files.isReadable(candidate)) return Files.readString(candidate, StandardCharsets.UTF_8);
			} catch (IOException e) {
				// Fall through to the message below.
			}
		}
		return "<!doctype html><meta charset=utf-8><title>Dashboard missing</title>"
			+ "<p style=\"font:16px system-ui;padding:2rem\">dashboard.html was not found next to the server. "
			+ "Run the server from the repository root: <code>java demo/UdcDemoServer.java</code></p>";
	}

	// ---------------------------------------------------------------- plumbing

	private static void respond(HttpExchange exchange, int status, String contentType, String body) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", contentType);
		exchange.getResponseHeaders().add("Cache-Control", "no-store");
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(bytes);
		}
	}

	private static String header(HttpExchange exchange, String name, String fallback) {
		String value = exchange.getRequestHeaders().getFirst(name);
		return value == null || value.isBlank() ? fallback : value;
	}

	private static long parseLong(String value) {
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return 0L;
		}
	}
}
