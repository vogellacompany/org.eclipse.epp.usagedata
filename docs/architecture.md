# Eclipse Usage Data Collector (UDC)

The Usage Data Collector (UDC) captures anonymized usage data from the Eclipse IDE to help the Eclipse community understand how the platform is used in practice. It is structured as a set of OSGi bundles built with Maven/Tycho against the Eclipse target platform.

## Project Structure

```
plugins/
  org.eclipse.epp.usagedata.gathering    # Monitors user activity
  org.eclipse.epp.usagedata.recording    # Stores and uploads collected data
  org.eclipse.epp.usagedata.ui           # Preference pages and upload UI
features/
  org.eclipse.epp.usagedata.feature      # Groups plugins for installation
  org.eclipse.epp.usagedata.tests.feature
test/                                    # Test bundles for each plugin
releng/
  org.eclipse.epp.usagedata.repository   # Generates the p2 update site
target-platform/                         # Eclipse target platform definition
```

## Architecture

The system follows a pipeline model: **gather -> record -> upload**, with each stage handled by a separate plugin.

### Gathering

The `gathering` plugin observes user activity inside the running workbench. It defines the `org.eclipse.epp.usagedata.gathering.monitors` extension point, which accepts implementations of the `UsageMonitor` interface:

```java
public interface UsageMonitor {
    void startMonitoring(UsageDataService usageDataService);
    void stopMonitoring();
}
```

Built-in monitors track:

| Monitor | What it captures |
|---|---|
| `PartUsageMonitor` | View and editor open/close/activate events |
| `CommandUsageMonitor` | Command executions (menu items, shortcuts) |
| `BundleUsageMonitor` | Bundle lifecycle events (install, start, stop) |
| `SystemInfoMonitor` | OS, JVM, and locale information at startup |
| `LogMonitor` | Entries written to the Eclipse error log |

The plugin also exposes the `org.eclipse.epp.usagedata.listeners.event` extension point so that downstream components (like the recorder) can subscribe to captured events.

### Recording

The `recording` plugin implements the event listener extension point. `UsageDataRecorder` receives events from the gathering layer and persists them to local storage. It also owns the upload lifecycle:

- **Filtering** -- rules that determine which events should be kept or discarded before upload.
- **Uploading** -- manages the transfer of recorded data to the collection server. The `uploader` extension point allows the upload mechanism to be replaced.

### UI

The `ui` plugin contributes Eclipse preference pages for end-user configuration:

- **Capture preferences** -- enable or disable data collection.
- **Uploading preferences** -- configure upload frequency and server URL.
- **Terms of Use** -- display and manage user consent.
- **Upload preview** -- inspect collected data before it leaves the machine.

It also provides the `AskUserUploader`, which prompts the user for confirmation before each upload. Help content and a Welcome page extension are included as well.

## Extending the Collector

To add a new monitor, create a plugin that:

1. Depends on `org.eclipse.epp.usagedata.gathering`.
2. Implements `UsageMonitor`.
3. Registers the implementation via the `org.eclipse.epp.usagedata.gathering.monitors` extension point in `plugin.xml`.

The gathering framework will call `startMonitoring()` at workbench startup and `stopMonitoring()` at shutdown.

## Building

Requires Java 21 and Maven. The build uses the Tycho plugin to resolve OSGi dependencies against the target platform defined in `target-platform/target-platform.target`.

```bash
mvn clean verify
```

This compiles all plugins, runs the test suites, and assembles the p2 update site under `releng/org.eclipse.epp.usagedata.repository/target/`.

To build a single module with its dependencies, use `-pl :bundle-id -am`:

```bash
mvn clean compile -pl :org.eclipse.epp.usagedata.recording -am
```

## License

Eclipse Public License v1.0 (EPL-1.0). Contributions require a signed Eclipse Contributor Agreement (ECA) and a `Signed-off-by` trailer in commit messages. See [CONTRIBUTING.md](../CONTRIBUTING.md) for details.
