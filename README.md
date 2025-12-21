# Spring Boot Log4j2 `logging.log4j2.config.override` reproducer

This repository is a minimal, test-driven reproducer for Spring Boot issue https://github.com/spring-projects/spring-boot/issues/48591.

The important detail for the issue is that the application uses **two distinct “log types”**:

1. Custom **Application log** (the “normal” app logging pipeline)
2. Custom **Admin log** (a separate logger name with dedicated appenders)

Both log types are configured using Log4j2 XML, with the second one expected to be contributed via Spring Boot’s `logging.log4j2.config.override` mechanism.

## Project shape

- Two logging entry points:
  - **Application logs**: any normal logger (example: logger named `reproducer`)
  - **Admin logs**: logger named `adminLog` (see `src/main/java/reproducer/logger/AdminLogger.java`)

The tests run the library inside a tiny Spring Boot application (`src/test/groovy/reproducer/TestSpringBootApplication.groovy`).

## Custom log type 1: Application log

### Intent

Captures standard Spring Boot application logging (root logger), writing to:

- console (pattern starts with `CUSTOM_APP_LOG`)
- a rolling file under `${env:LOG_HOME}`

### Configuration

The application log is defined by the primary Log4j2 config specified via `logging.config`:

- `src/test/resources/custom-application-log/log4j2.xml`

Notable aspects:

- Root logger at `INFO` (`<Root level="INFO">`)
- Appenders:
  - `customConsole` (console)
  - `customFile` (`RollingRandomAccessFile`)

### Usage

Example from the functional test (`src/test/groovy/reproducer/ApplicationLogOnlyFunctionalSpec.groovy`):

- `LogManager.getLogger("reproducer").info("...")`

The test asserts the message is written to `customFile`.

## Custom log type 2: Admin/audit log

### Intent

Captures a dedicated “admin” (audit-style) log stream that is intentionally **not** part of the normal root logger pipeline.

It is modeled as a separate logger name (`adminLog`) with dedicated appenders. In a real system this is often used for:

- security/audit events
- compliance logging
- “write-once” operational logs

### Code path

There are two layers:

1. `reproducer.logger.SpringAdminLogger` (Spring bean)
   - Conditional on property: `custom-admin-log.enabled=true`
   - Serializes an `AdminLog` record to JSON and delegates to `AdminLogger`

2. `reproducer.logger.AdminLogger` (static Log4j2 logger)
   - Uses an explicit logger name: `LogManager.getLogger("adminLog")`
   - Emits at `TRACE`

Supporting types:

- `src/main/java/reproducer/model/AdminLog.java` (a `record`)
- `src/main/java/reproducer/service/AdminLogService.java` (calls `SpringAdminLogger.trace(...)`)

### Configuration

The admin log is defined in a separate Log4j2 config:

- `src/test/resources/custom-admin-log/log4j2.xml`

Notable aspects:

- Dedicated async logger:
  - `<AsyncLogger additivity="false" name="adminLog" level="TRACE">`
- Appenders:
  - `adminConsole` (console)
  - `adminFile` (`RollingRandomAccessFile`)
- Layout patterns start with `CUSTOM_ADMIN_LOG:`

### How it is expected to be loaded

In the integration profile, the configuration is split as:

- Primary config: `logging.config=classpath:custom-application-log/log4j2.xml`
- Override config: `logging.log4j2.config.override=classpath:custom-admin-log/log4j2.xml`

See `src/test/resources/application-integration-test.properties`.

The intent is:

- The **application log** comes from the primary config.
- The **admin log** comes only from the override file, without having to merge both logs into a single `log4j2.xml` resource.

### Verification

`src/test/groovy/reproducer/service/AbstractAdminLogServiceIntegrationSpec.groovy`:

- calls `adminLogService.log("tc1", context)`
- expects a line like:
  - `CUSTOM_ADMIN_LOG: {"code":"tc1","context":"ctx-..."}`
- reads the target file path from Log4j2 internals by locating the `adminFile` appender on the `adminLog` logger.

## Profiles used by tests

- `applog-only-test`
  - `src/test/resources/application-applog-only-test.properties`
  - sets only `logging.config` (application log only)
  - asserts the `AdminLogService` bean is absent

- `integration-test`
  - `src/test/resources/application-integration-test.properties`
  - sets `logging.config` + `logging.log4j2.config.override`
  - enables `custom-admin-log.enabled=true`
  - asserts the admin log file receives the JSON record

## Running

- `./gradlew test`

`LOG_HOME` is set automatically by `build.gradle` for the test task.
