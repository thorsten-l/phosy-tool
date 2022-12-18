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
package l9g.app.phosy.ucware.phonebook;

import com.unboundid.asn1.ASN1GeneralizedTime;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import l9g.app.phosy.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class PhonebookMain
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(PhonebookMain.class.getName());

  private static final String VAR_DIRECTORY_NAME = "var";

  private static final String TIMESTAMP_FILENAME = "lastsync.timestamp";

  private final static PhonebookMain SINGLETON = new PhonebookMain();

  private PhonebookMain()
  {
    if (System.getProperty("app.home") != null)
    {
      varDirectory = new File(System.getProperty("app.home")
        + File.separator + VAR_DIRECTORY_NAME);
    }
    else
    {
      varDirectory = new File(VAR_DIRECTORY_NAME);
    }

    LOGGER.debug("varDirectory={}", varDirectory.getAbsolutePath());

    if (!varDirectory.exists())
    {
      varDirectory.mkdirs();
    }

    timestampFile = new File(varDirectory, TIMESTAMP_FILENAME);

    LOGGER.debug("timestampFile={}", timestampFile.getAbsolutePath());
  }

  public static PhonebookMain getInstance()
  {
    return SINGLETON;
  }

  private ASN1GeneralizedTime readLastSyncTimestamp() throws Throwable
  {
    ASN1GeneralizedTime timestamp = new ASN1GeneralizedTime(0l);

    if (timestampFile.exists() && timestampFile.canRead())
    {
      String timestampString = null;

      try (BufferedReader reader = new BufferedReader(new FileReader(
        timestampFile)))
      {
        timestampString = reader.readLine().trim();
      }

      if (timestampString != null && timestampString.length() > 0)
      {
        timestamp = new ASN1GeneralizedTime(timestampString);
      }
    }

    LOGGER.info("last sync timestamp = {}", timestamp);
    return timestamp;
  }

  public void run(Options OPTIONS) throws Throwable
  {
    PhonebookHandler phonebookHandler = PhonebookHandler.getInstance();

    if (OPTIONS.isInfoPhonebookName())
    {
      phonebookHandler.showInfo();
      System.exit(0);
    }
    else if (OPTIONS.isExportPhonebookUUID())
    {
      phonebookHandler.exportPhonebook(false);
      System.exit(0);
    }
    else if (OPTIONS.isBackupPhonebookUUID())
    {
      phonebookHandler.exportPhonebook(true);
      System.exit(0);
    }
    else if (OPTIONS.isAddPhonebookName())
    {
      phonebookHandler.addPhonebook();
      System.exit(0);
    }
    else if (OPTIONS.isRemovePhonebookUUID())
    {
      phonebookHandler.deletePhonebook();
      System.exit(0);
    }
    else if (OPTIONS.isImportPhonebook())
    {
      phonebookHandler.importPhonebook();
      System.exit(0);
    }
    else if (OPTIONS.isSyncPhonebook())
    {
      ASN1GeneralizedTime currentTimestamp = new ASN1GeneralizedTime();
      ASN1GeneralizedTime lastSyncTimestamp = readLastSyncTimestamp();

      PhonebookLdapHandler ldapHandler = PhonebookLdapHandler.getInstance();

      ldapHandler.readLdapEntries(new ASN1GeneralizedTime(0l), false);
      phonebookHandler.readContacts();

      phonebookHandler.setPhonebookWritable(true);
      phonebookHandler.removeUnknownContacts();
      ldapHandler.readLdapEntries(lastSyncTimestamp, true);

      if (!ldapHandler.getLdapEntryMap().isEmpty())
      {
        LOGGER.info("Create or update {} entries.",
          ldapHandler.getLdapEntryMap().size());
        phonebookHandler.createUpdateContacts();
      }
      
      phonebookHandler.setPhonebookWritable(false);

      try (PrintWriter out = new PrintWriter(timestampFile))
      {
        out.println(currentTimestamp.toString());
      }
      System.exit(0);
    }
  }

  private final File varDirectory;

  private final File timestampFile;
}
