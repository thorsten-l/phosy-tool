# PHOSY - Phone System Tool for Ucware

PHOSY is a Java-based utility designed to manage and synchronize phonebook entries for the Ucware phone system. It primarily handles LDAP-to-Ucware synchronization, supporting complex attribute mapping and normalization through embedded JShell scripts.

## Project Overview

- **Purpose**: Synchronize LDAP entries with Ucware phonebooks, manage phonebooks (CRUD), and handle data import/export (CSV/JSON).
- **Target System**: Ucware Phone System (via REST/JSON-RPC API).
- **Core Technologies**:
  - **Java 11/17**: Core application logic.
  - **Maven**: Build and dependency management.
  - **UnboundID LDAP SDK**: Efficient LDAP communication.
  - **Jersey/Jackson**: REST client and JSON processing.
  - **JAXB**: XML configuration management.
  - **JShell**: Dynamic attribute mapping and normalization logic.
  - **Args4j**: Command-line argument parsing.

## Architecture

The application follows a structured approach to synchronization:
1. **App**: Main entry point and CLI handler.
2. **Configuration**: XML-based configuration (`config/config.xml`) with encrypted credentials.
3. **LDAP Handler**: Retrieves entries from LDAP using paged results for efficiency.
4. **Script Handler**: Executes JShell scripts (`jsh/*.jsh`) to transform LDAP attributes into Ucware-compatible formats.
5. **Ucware Client**: Performs the final API calls to update the Ucware phone system.

## Building and Running

### Build
Requires JDK 11+ and Maven.
```bash
mvn clean package
```
The build produces an assembly zip in `target/phosy-tool-app.zip`.

### Running
1. Unzip the distribution: `unzip target/phosy-tool-app.zip`
2. Enter the directory: `cd phosy-tool`
3. Configure: Copy `config/config.xml.sample` to `config/config.xml` and edit it.
4. Encrypt passwords: Use `./bin/phosy-tool -e (plain password)` to generate encrypted strings for the config file.
5. Execute: `./bin/phosy-tool [options]`

## Development Conventions

### JShell Scripting
Attribute normalization and mapping are handled in `jsh/createPhonebookAttributes.jsh` and `jsh/createUserAttributes.jsh`. These are **Java** snippets executed for every LDAP entry. 
- **Input**: `Entry entry` (LDAP entry), `LdapUtil ldapUtil`.
- **Output**: The script should evaluate to a `String` (typically the normalized phone number) or populate bindings.

### Configuration
- Passwords MUST be encrypted in `config.xml`.
- Do not delete `config/secret.bin`, as it contains the key for decryption.

### Testing
- Currently, there are no automated tests in `src/test/java`.
- Use `--dry-run` and `--verbose` flags for manual validation during development.

### Logging
- Logback is used for logging.
- Configuration is in `src/main/logback/logback.xml` (or `logback.devel.xml` when using the `development` profile).
