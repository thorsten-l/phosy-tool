# PHOSY a phonesystem tool for Ucware

# Description

At this time PHOSY can deal with the user phonebook of the Ucware phone system.

- Synchronize LDAP entries with the specified phonebook
- export/backup a phonebook
- import/restore CSV or JSON file to a phonebook
- add phonebook
- remove phonebook
- show info about a phonebook

## Build with maven

Requirements 
- Java Development Kit (JDK) tested with versions 11 and 17 
- Maven build system

build task

- `mvn clean package`

## Run 

1. `unzip -o target/phosy-tool-app.zip`
2. `cd phosy-tool`
3. copy `config/config.xml.sample` to `config/config.xml`
4. edit `config/config.xml`
  - passwords must be stored encrypted in the config.xml.
  - encrypt a password with `./bin/phosy-tool -e (plain password)` 
5. Do NOT remove the created `config/secret.bin` file.

## "Configure" your own attribute set

1. edit `jsh/createPhonebookAttributes.jsh`
2. edit `jsh/createUserAttributes.jsh`

`jsh/*.jsh` is Java (NOT JavaScript) code, executed by an embedded JShell for every single LDAP entry during the synchronization process.

You can build your own phonenumber normalizer or specify how prefix and suffix will be created. Use the delivered code as a sample.


## Usage : ./bin/phosy-tool
```text
Usage: phosy-tool [options]

 --add-phonebook (-a)      : Add Ucware phonebook <NAME> required (Vorgabe:
                             false)
 --backup-phonebook (-b)   : Export Ucware phonebook <UUID> required (export
                             without UUIDs) (Vorgabe: false)
 --debug                   : Enable DEBUG logging (Vorgabe: false)
 --dry-run                 : Enable dry run for testing (Vorgabe: false)
 --encrypt (-e) WERT       : Encrypt given password
 --export-phonebook (-x)   : Export Ucware phonebook <UUID> required (Vorgabe:
                             false)
 --file (-f) WERT          : filename
 --generate (-g) N         : Generate random password (Vorgabe: 0)
 --help (-h)               : Displays this help (Vorgabe: true)
 --import-phonebook (-imp) : Import --file <CSV or JSON> file into --name
                             <phonebook name> (Vorgabe: false)
 --info-phonebook (-i)     : Info about Ucware phonebook <NAME> or <UUID>
                             required (Vorgabe: false)
 --name WERT               : phonebook name
 --remove-phonebook (-r)   : Remove Ucware phonebook <UUID> required (Vorgabe:
                             false)
 --sync-phonebook          : Sync Ucware user phonebook with LDAP (Vorgabe:
                             false)
 --sync-users              : Sync Ucware users with LDAP (Vorgabe: false)
 --uuid WERT               : phonebook uuid
 --verbose (-v)            : Detailed info (Vorgabe: false)
 --version                 : Display programm version (Vorgabe: false)
```