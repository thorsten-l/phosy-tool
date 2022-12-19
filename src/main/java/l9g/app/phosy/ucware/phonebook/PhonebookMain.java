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
import l9g.app.phosy.Options;
import l9g.app.phosy.TimestampUtil;
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

  private final static PhonebookMain SINGLETON = new PhonebookMain();

  private PhonebookMain()
  {
  }

  public static PhonebookMain getInstance()
  {
    return SINGLETON;
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
      TimestampUtil timestampUtil = new TimestampUtil("phonebook");

      PhonebookLdapHandler ldapHandler = PhonebookLdapHandler.getInstance();

      ldapHandler.readLdapEntries(new ASN1GeneralizedTime(0l), false);
      phonebookHandler.readContacts();

      phonebookHandler.setPhonebookWritable(true);
      phonebookHandler.removeUnknownContacts();
      ldapHandler.readLdapEntries(timestampUtil.getLastSyncTimestamp(), true);

      if (!ldapHandler.getLdapEntryMap().isEmpty())
      {
        LOGGER.info("Create or update {} entries.",
          ldapHandler.getLdapEntryMap().size());
        phonebookHandler.createUpdateContacts();
      }
      
      phonebookHandler.setPhonebookWritable(false);
      timestampUtil.writeCurrentTimestamp();
    }
  }
}
