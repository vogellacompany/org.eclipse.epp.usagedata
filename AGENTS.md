# Gemini Context: Eclipse EPP Usage Data Collector (UDC)

This project is the **Usage Data Collector (UDC)** for the Eclipse Packaging Project (EPP). It is designed to capture, record, and upload usage data from Eclipse users to help the community understand how the IDE is being used.

## Project Overview

- **Core Purpose:** Collect and upload anonymized usage data from the Eclipse IDE.
- **Main Technologies:**
  - **Java 21:** The project's required execution environment.
  - **OSGi / Eclipse Plugins:** The project is a collection of Eclipse plugins and features.
  - **Maven & Tycho:** Used for building OSGi bundles and managing dependencies within the Eclipse ecosystem.
  - **Eclipse Target Platform:** Based on the Eclipse 2025-06 release.
- **Architecture:**
  - `org.eclipse.epp.usagedata.gathering`: Handles the actual monitoring of user actions within the IDE. It defines extension points for custom monitors.
  - `org.eclipse.epp.usagedata.recording`: Manages the storage and filtering of collected data, and handles the upload process.
  - `org.eclipse.epp.usagedata.ui`: Provides the user interface for configuring settings, previewing data, and managing the upload process.
  - `org.eclipse.epp.usagedata.repository`: Releng project that generates the p2 update site.

## Building and Running

The project uses Maven with the Tycho plugin for building.

- **Build and Test:**
  ```bash
  mvn clean verify
  ```
- **Build a single module** (use `-am` to include its dependencies):
  ```bash
  mvn clean compile -pl :org.eclipse.epp.usagedata.recording -am
  ```
- **Target Platform:** The build relies on the target platform definition located at `target-platform/target-platform.target`.

## Development Conventions

- **Eclipse Plugin Style:** Follows standard Eclipse plugin development practices, including `MANIFEST.MF` for dependencies and `plugin.xml` for extensions and extension points.
- **Extensibility:** The `gathering` plugin provides the `org.eclipse.epp.usagedata.gathering.monitors` extension point, allowing other plugins to contribute their own `UsageMonitor` implementations.
- **Testing:** Each core plugin has a corresponding test plugin in the `test/` directory. Tests are typically executed during the `mvn verify` phase.
- **License:** The project is licensed under the **Eclipse Public License v1.0 (EPL-1.0)**.
- **Contributions:** Contributions require an **Eclipse Contributor Agreement (ECA)** and must include a `Signed-off-by` footer in commit messages.
- **Issue Tracking:** Development is tracked via the Eclipse Foundation's Bugzilla (Product: EPP).

## Key Files and Directories

- `pom.xml`: Root Maven configuration file.
- `plugins/`: Source code for the OSGi bundles.
- `features/`: Eclipse features that group the plugins.
- `test/`: Unit and integration test plugins.
- `releng/`: Release engineering projects (e.g., repository generation).
- `target-platform/`: Target definition for the Eclipse environment.
- `Readme.adoc`: Basic project overview and build instructions.
- `CONTRIBUTING.md`: Guidelines for contributing to the project.
