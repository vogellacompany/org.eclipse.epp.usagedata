# Eclipse Usage Data Collector -- User Guide

The Usage Data Collector (UDC) records anonymized information about how you use the Eclipse IDE and periodically uploads it to the Eclipse Foundation. This guide covers installation, configuration, and privacy controls for end users.

## Installation

UDC is distributed as the feature `org.eclipse.epp.usagedata.feature` and can be installed through **Help > Install New Software...** using the update site produced by this repository (`releng/org.eclipse.epp.usagedata.repository`). Once installed and restarted, data capture is enabled by default.

## Preferences

All UDC settings live under **Window > Preferences > Usage Data Collector**.

### Capture

Controls whether usage data is recorded at all.

| Setting | Default | Description |
|---|---|---|
| Enable capture | On | Master switch. When off, no events are written to local storage and no uploads occur. |

Unchecking *Enable capture* is the simplest way to fully opt out.

### Uploading

Controls when and how recorded data is sent to the Eclipse server.

| Setting | Default | Description |
|---|---|---|
| Ask before uploading | On | Show a consent dialog before each upload. |
| Upload period (days) | 5 | Minimum interval between uploads (valid range: 1-90). |
| Last upload | -- | Read-only timestamp of the most recent upload. |
| Upload URL | `http://udc.eclipse.org/upload.php` | Read-only server endpoint. Administrators can override via a system property. |
| Upload Now | -- | Button that triggers an immediate upload, subject to the capture setting. |

### Terms of Use

Displays the terms that apply to collected data. You must accept them before the first upload can occur.

### Preview

A read-only table listing the events currently queued for upload. Useful to review what will leave your machine before it is sent.

## Upload Dialog

When an upload is due and *Ask before uploading* is enabled, UDC presents a wizard with four choices:

- **Upload Now** -- send the staged data once.
- **Upload Always** -- send now and stop asking for future uploads.
- **Skip This** -- defer until the next scheduled interval.
- **Never Upload Again** -- disable uploading permanently (capture can still be toggled separately).

## What Is Collected

UDC records only application-level events -- never file contents, selection text, or keystrokes. Built-in monitors capture:

- **Parts** -- views and editors opening, closing, and gaining focus; perspective changes.
- **Commands** -- executions of Eclipse commands (menu items, toolbar actions, key bindings) and whether they succeeded.
- **Bundles** -- install, resolve, start, and stop events for OSGi bundles, including version.
- **System information** -- operating system, JVM version, and locale, recorded once per session.
- **Log entries** -- messages written to the Eclipse error log.

Every event is tagged with a timestamp, a stable workstation identifier derived from your user home, an auto-generated workspace identifier, and the originating bundle's symbolic name and version.

## Local Data Storage

Events are appended to `usagedata.csv` inside the workspace metadata directory:

```
<workspace>/.metadata/.plugins/org.eclipse.epp.usagedata.recording/usagedata.csv
```

When an upload is scheduled, the active file is rotated to `upload0.csv`, `upload1.csv`, etc. You can delete any of these files while Eclipse is closed to discard data that has not yet been uploaded.

## Privacy Controls

In addition to disabling capture entirely, you can narrow what is recorded:

- **Eclipse bundles only** -- when enabled (default), events originating from non-`org.eclipse.*` bundles are dropped before they are written.
- **Filter patterns** -- a list of regular expressions. Bundles whose symbolic name matches any pattern are excluded.

The filter is applied both during recording and before upload, so tightening it also prevents already-recorded events from being sent.

## Troubleshooting

- **Uploads never happen.** Check that *Enable capture* is on, that the Terms of Use have been accepted, and that the upload period has elapsed since the *Last upload* timestamp.
- **Nothing appears in the Preview page.** Either capture is disabled, the filter is excluding everything, or no monitored events have occurred yet in this session.
- **You want to start over.** Close Eclipse, delete the files under `<workspace>/.metadata/.plugins/org.eclipse.epp.usagedata.recording/`, and restart.
