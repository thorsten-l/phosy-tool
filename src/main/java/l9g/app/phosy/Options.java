/*
 * Copyright 2022 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.app.phosy;

//~--- non-JDK imports --------------------------------------------------------
import lombok.Getter;
import lombok.Setter;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class Options
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(Options.class.getName());

  public static void checkNameRequired()
  {
    if (App.getOPTIONS().getName() == null
      || App.getOPTIONS().getName().trim().length() == 0)
    {
      LOGGER.warn("--name <name> is required");
      System.exit(0);
    }
  }

  public static void checkFilenameRequired()
  {
    if (App.getOPTIONS().getFilename() == null
      || App.getOPTIONS().getFilename().trim().length() == 0)
    {
      LOGGER.warn("--file <filename> is required");
      System.exit(0);
    }
  }

  public static void checkUuidRequired()
  {
    if (App.getOPTIONS().getUuid() == null
      || App.getOPTIONS().getUuid().trim().length() == 0)
    {
      LOGGER.warn("--uuid <uuid> is required");
      System.exit(0);
    }
  }

  public static void checkUuidOrNameRequired()
  {
    if ((App.getOPTIONS().getUuid() == null
      || App.getOPTIONS().getUuid().trim().length() == 0)
      && (App.getOPTIONS().getName() == null
      || App.getOPTIONS().getName().trim().length() == 0))
    {
      LOGGER.warn(
        "--uuid <uuid> OR --name <name> is required");
      System.exit(0);
    }
  }

  /**
   * Field description
   */
  @Option(
    name = "--help",
    aliases = "-h",
    usage = "Displays this help"
  )
  @Getter
  @Setter
  private boolean displayHelp = false;

  /**
   * Field description
   */
  @Option(
    name = "--version",
    usage = "Display programm version"
  )
  @Getter
  @Setter
  private boolean displayVersion = false;

  @Option(
    name = "--verbose",
    aliases = "-v",
    usage = "Detailed info"
  )
  @Getter
  @Setter
  private boolean verbose = false;

  @Option(
    name = "--debug",
    usage = "Enable DEBUG logging"
  )
  @Getter
  @Setter
  private boolean debugLogging = false;

  /**
   * Field description
   */
  @Option(
    name = "--generate",
    aliases = "-g",
    usage = "Generate random password"
  )
  @Getter
  @Setter
  private int generatePasswordLength = 0;

  /**
   * Field description
   */
  @Option(
    name = "--encrypt",
    aliases = "-e",
    usage = "Encrypt given password"
  )
  @Getter
  @Setter
  private String password = null;

  @Option(
    name = "--sync-phonebook",
    usage = "Sync Ucware user phonebook with LDAP"
  )
  @Getter
  @Setter
  private boolean syncPhonebook;

  @Option(
    name = "--sync-users",
    usage = "Sync Ucware users with LDAP"
  )
  @Getter
  @Setter
  private boolean syncUsers;

  @Option(
    name = "--file",
    aliases = "-f",
    usage = "filename"
  )
  @Getter
  @Setter
  private String filename = null;

  @Option(
    name = "--name",
    usage = "phonebook name"
  )
  @Getter
  @Setter
  private String name = null;

  @Option(
    name = "--uuid",
    usage = "phonebook uuid"
  )
  @Getter
  @Setter
  private String uuid = null;

  @Option(
    name = "--add-phonebook",
    aliases = "-a",
    usage = "Add Ucware phonebook <NAME> required"
  )
  @Getter
  @Setter
  private boolean addPhonebookName;

  @Option(
    name = "--info-phonebook",
    aliases = "-i",
    usage = "Info about Ucware phonebook <NAME> or <UUID> required"
  )
  @Getter
  @Setter
  private boolean infoPhonebookName = false;

  @Option(
    name = "--remove-phonebook",
    aliases = "-r",
    usage = "Remove Ucware phonebook <UUID> required"
  )
  @Getter
  @Setter
  private boolean removePhonebookUUID = false;

  @Option(
    name = "--export-phonebook",
    aliases = "-x",
    usage = "Export Ucware phonebook <UUID> required"
  )
  @Getter
  @Setter
  private boolean exportPhonebookUUID = false;

  @Option(
    name = "--backup-phonebook",
    aliases = "-b",
    usage = "Export Ucware phonebook <UUID> required (export without UUIDs)"
  )
  @Getter
  @Setter
  private boolean backupPhonebookUUID = false;

  @Option(
    name = "--import-phonebook",
    aliases = "-imp",
    usage = "Import --file <CSV or JSON> file into --name <phonebook name>"
  )
  @Getter
  @Setter
  private boolean importPhonebook = false;

}
