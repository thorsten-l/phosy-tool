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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.unboundid.ldap.sdk.Entry;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.script.Bindings;
import l9g.app.phosy.App;
import l9g.app.phosy.Options;
import l9g.app.phosy.config.PhonebookConfig;
import l9g.app.phosy.ucware.UcwarePhonebookClient;
import l9g.app.phosy.ucware.UcwareClientFactory;
import l9g.app.phosy.ucware.UcwareAttributeType;
import l9g.app.phosy.ucware.phonebook.model.UcwareContact;
import l9g.app.phosy.ucware.phonebook.model.UcwareContactGroup;
import l9g.app.phosy.ucware.phonebook.model.UcwarePhonebook;
import l9g.app.phosy.ucware.phonebook.requestparam.UcwareParamAttribute;
import l9g.app.phosy.ucware.phonebook.requestparam.UcwareParamContact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class PhonebookHandler
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(PhonebookHandler.class.getName());

  private final static PhonebookHandler SINGLETON = new PhonebookHandler();

  private PhonebookHandler()
  {
    ucwareClient = UcwareClientFactory.getPhonebookClient(
      App.getConfig().getPhonebookConfig().getUcwareConfig());
  }

  public static PhonebookHandler getInstance()
  {
    return SINGLETON;
  }

  public static String entryValue(Entry entry, UcwareAttributeType type)
  {
    String value = "";
    String attributeName = config.getLdapMap().get(type);

    if (attributeName != null)
    {
      String v = entry.getAttributeValue(attributeName);
      if (v != null)
      {
        value = v;
      }
    }

    return value;
  }

  public void readContacts() throws Throwable
  {
    LOGGER.debug("readContacts");

    phonebook = null;

    if (config.getPhonebookUUID() != null
      && config.getPhonebookUUID().trim().length() > 0)
    {
      phonebook = ucwareClient.getUserPhonebookByUUID(
        config.getPhonebookUUID()
      );

      if (phonebook != null)
      {
        LOGGER.info("found phonebook by uuid = {}", phonebook.getUuid());
        contactGroup = phonebook.getGroups().get(0);
      }
    }

    if (phonebook == null)
    {
      phonebook = ucwareClient.getUserPhonebookByName(
        config.getPhonebookName()
      );

      if (phonebook != null)
      {
        LOGGER.info("found phonebook by name = {} with uuid={}",
          phonebook.getName(), phonebook.getUuid());
        contactGroup = phonebook.getGroups().get(0);
      }
      else
      {
        phonebook = ucwareClient.newUserPhonebook(
          config.getPhonebookName(), false
        );

        LOGGER.info("phonebook {} created with uuid = {}",
          phonebook.getName(), phonebook.getUuid());

        contactGroup = phonebook.getGroups().get(0);
      }
    }

    if (contactGroup == null)
    {
      LOGGER.error("No contact group found");
      System.exit(0);
    }

    for (UcwareContact contact : contactGroup.getContacts())
    {
      LOGGER.debug("syncID={} v={}", contact.getSyncId(), contact);
      ucwareContactMap.put(contact.getSyncId(), contact);
    }

    LOGGER.info("{} phonebook contacts in '{}' found.",
      ucwareContactMap.size(), phonebook.getName());
  }

  public void removeUnknownContacts() throws Throwable
  {
    LOGGER.debug("deleteUnknownContacts");

    int counter = 0;

    for (String syncId : ucwareContactMap.keySet())
    {
      if (PhonebookLdapHandler.getLdapEntryMap().containsKey(syncId))
      {
        LOGGER.debug("syncId={} exist", syncId);
      }
      else
      {
        UcwareContact contact = ucwareContactMap.get(syncId);
        LOGGER.info("{} removing dn={} {} {}", counter++, syncId,
          contact.getFirstname(), contact.getLastname());

        if (!ucwareClient.deleteUserContact(contact.getUuid()))
        {
          LOGGER.error("ERROR: removing contact {}", contact);
          System.exit(0);
        }
      }
    }
  }

  public void createUpdateContacts() throws Throwable
  {
    LOGGER.debug("createMissingContacts");

    int counter = 0;

    String attributeGivenName = config.getLdapMap().get(
      UcwareAttributeType.LDAP_GIVENNAME);
    String attributeSn = config.getLdapMap().get(UcwareAttributeType.LDAP_SN);

    for (Entry entry : PhonebookLdapHandler.getLdapEntryMap().values())
    {
      String syncId = PhonebookLdapHandler.buildSyncId(entry);

      if (ucwareContactMap.containsKey(syncId))
      {
        LOGGER.info("* updating {}", entry.getDN());
        ucwareClient.deleteUserContact(ucwareContactMap.get(syncId).getUuid());
      }
      else
      {
        LOGGER.info("+ creating {}", entry.getDN());
      }

      LOGGER.debug("contactGroup={}", contactGroup);

      Bindings bindings = PhonebookScriptHandler.getInstance().run(
        config, entry);

      String company = (String) bindings.get("company");
      String department = (String) bindings.get("department");
      String hyperlink = (String) bindings.get("hyperlink");
      String position = (String) bindings.get("position");
      String phoneNumber = (String) bindings.get("phoneNumber");
      String prefix = (String) bindings.get("prefix");
      String suffix = (String) bindings.get("suffix");
      String locality = (String) bindings.get("locality");

      LOGGER.debug("company={}", company);
      LOGGER.debug("department={}", department);
      LOGGER.debug("hyperlink={}", hyperlink);
      LOGGER.debug("position={}", position);
      LOGGER.debug("phoneNumber={}", phoneNumber);
      LOGGER.debug("prefix={}", prefix);
      LOGGER.debug("suffix={}", suffix);
      LOGGER.debug("locality={}", locality);
      
      UcwareParamContact pContact = new UcwareParamContact(
        prefix,
        entry.getAttributeValue(attributeGivenName),
        entry.getAttributeValue(attributeSn),
        suffix,
        syncId
      );

      UcwareContact contact = ucwareClient.addUserContact(
        contactGroup.getUuid(), pContact);

      LOGGER.debug("contact={}", contact);

      UcwareParamAttribute pAttribute = new UcwareParamAttribute(
        "Telefon", phoneNumber,
        UcwareAttributeType.UCW_PHONENUMBER_HIGH_PRIORITY);
      ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

      pAttribute = new UcwareParamAttribute(
        "Email", entryValue(entry, UcwareAttributeType.UCW_EMAIL).toLowerCase(),
        UcwareAttributeType.UCW_EMAIL);
      ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

      pAttribute = new UcwareParamAttribute("Unternehmen", company,
        UcwareAttributeType.UCW_COMPANY);
      ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

      pAttribute = new UcwareParamAttribute(hyperlink, hyperlink,
        UcwareAttributeType.UCW_HYPERLINK);
      ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

      pAttribute = new UcwareParamAttribute(
        "Abteilung", department, UcwareAttributeType.UCW_DEPARTMENT);
      ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

      pAttribute = new UcwareParamAttribute("Funktion", position,
        UcwareAttributeType.UCW_POSITION);
      ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

      counter++;
      System.out.println("*** counter = " + counter + ", " + contact.
        getLastname() + ", " + contact.getFirstname());
    }
  }

  public void setPhonebookWritable(boolean writable)
  {
    UcwarePhonebook vpb = ucwareClient.updateUserPhonebook(
      phonebook.getUuid(), writable);
  }

// -----------------------------------------------------------------------  
  public void exportPhonebook(boolean backup) throws Throwable
  {
    Options.checkUuidRequired();
    String uuid = App.getOPTIONS().getUuid();

    if (backup)
    {
      LOGGER.info("backup phonebook {}", uuid);
    }
    else
    {
      LOGGER.info("export phonebook {}", uuid);
    }

    String filename = "phonebook-" + (backup ? "backup" : "export")
      + "-" + uuid + ".json";

    UcwarePhonebook exportPhonebook = ucwareClient.getUserPhonebookByUUID(uuid);

    if (backup) // during import all UUIDs will be created
    {
      exportPhonebook.setUuid(null);
      exportPhonebook.getGroups().forEach(g ->
      {
        g.setUuid(null);
        g.getContacts().forEach(c ->
        {
          c.setUuid(null);
          c.getAttributes().forEach(a -> a.setUuid(null));
        });
      });
    }

    if (exportPhonebook != null)
    {
      ObjectMapper objectMapper = new ObjectMapper();

      try (FileWriter writer = new FileWriter(filename))
      {
        objectMapper.writeValue(writer, exportPhonebook);
      }
    }
    else
    {
      LOGGER.warn("Phonebook UUID <{}> not found.", uuid);
    }
  }

  public void showInfo() throws Throwable
  {
    Options.checkUuidOrNameRequired();

    String phonebookName = App.getOPTIONS().getName();
    String phonebookUuid = App.getOPTIONS().getUuid();
    boolean verbose = App.getOPTIONS().isVerbose();

    UcwarePhonebook infoPhonebook = null;

    if (phonebookUuid != null)
    {
      infoPhonebook = ucwareClient.getUserPhonebookByUUID(phonebookUuid);
    }

    if (infoPhonebook == null && phonebookName != null)
    {
      infoPhonebook = ucwareClient.getUserPhonebookByName(phonebookName);
    }

    if (infoPhonebook != null)
    {
      System.out.println("phonebook name = " + infoPhonebook.getName());
      System.out.println("phonebook uuid = " + infoPhonebook.getUuid());

      if (verbose)
      {
        System.out.println("phonebook # groups = " + infoPhonebook.getGroups().
          size());
        int numberOfContacts = 0;
        for (UcwareContactGroup group : infoPhonebook.getGroups())
        {
          System.out.println("  - phonebook groups name = " + group.getName());
          System.out.println("  - phonebook groups # contacts = "
            + group.getContacts().size());
          numberOfContacts += group.getContacts().size();
        }
        System.out.println("phonebook # contacts = " + numberOfContacts);
      }
    }
    else
    {
      System.out.println("Phonebook '" + phonebookName + "' not found.");
    }
  }

  public void addPhonebook()
  {
    Options.checkNameRequired();
    String phonebookName = App.getOPTIONS().getName();
    phonebook = ucwareClient.newUserPhonebook(phonebookName, true);
    System.out.println("phonebook name = " + phonebook.getName());
    System.out.println("phonebook uuid = " + phonebook.getUuid());
  }

  public void deletePhonebook()
  {
    Options.checkUuidRequired();
    String phonebookUuid = App.getOPTIONS().getUuid();
    ucwareClient.deleteUserPhonebook(phonebookUuid);
  }

  public void importPhonebookCSV(String filename) throws IOException,
                                                         CsvValidationException
  {
    LOGGER.debug("importPhonebookCSV");
    LOGGER.info("CSV import");
    Options.checkNameRequired();
    String name = App.getOPTIONS().getName();

    phonebook = ucwareClient.getUserPhonebookByName(name);
    contactGroup = phonebook.getGroups().get(0);

    if (phonebook == null)
    {
      LOGGER.error("Phonebook {} not found!", name);
      System.exit(0);
    }

    try (
      FileReader fileReader = new FileReader(filename);
      CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(
        new CSVParserBuilder().withSeparator(';').build()).build())
    {
      String[] line;
      while ((line = csvReader.readNext()) != null)
      {
        System.out.println(line[2]);

        LOGGER.debug("contactGroup={}", contactGroup);

        String company = line[5];
        String department = line[4];
        String position = line[8];
        String mail = line[7];
        String phoneNumber = line[6];
        String prefix = line[0];
        String suffix = line[1];

        phoneNumber = phoneNumber.replaceAll("\\s+", "");
        phoneNumber = phoneNumber.replaceAll("\\/", "");

        String givenName = line[2];
        String sn = line[3];

        LOGGER.debug("company={}", company);
        LOGGER.debug("department={}", department);
        LOGGER.debug("position={}", position);
        LOGGER.debug("phoneNumber={}", phoneNumber);
        LOGGER.debug("prefix={}", prefix);
        LOGGER.debug("suffix={}", suffix);

        UcwareParamContact pContact = new UcwareParamContact(
          prefix,
          givenName,
          sn,
          suffix,
          UUID.randomUUID().toString()
        );

        UcwareContact contact = ucwareClient.addUserContact(
          contactGroup.getUuid(), pContact);

        LOGGER.debug("contact={}", contact);

        UcwareParamAttribute pAttribute = new UcwareParamAttribute(
          "Telefon", phoneNumber,
          UcwareAttributeType.UCW_PHONENUMBER_HIGH_PRIORITY);
        ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

        pAttribute = new UcwareParamAttribute(
          "Email", mail.toLowerCase(),
          UcwareAttributeType.UCW_EMAIL);
        ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

        pAttribute = new UcwareParamAttribute("Unternehmen", company,
          UcwareAttributeType.UCW_COMPANY);
        ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

        pAttribute = new UcwareParamAttribute(
          "Abteilung", department, UcwareAttributeType.UCW_DEPARTMENT);
        ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

        pAttribute = new UcwareParamAttribute("Funktion", position,
          UcwareAttributeType.UCW_POSITION);
        ucwareClient.addUserContactAttribute(contact.getUuid(), pAttribute);

      }
    }
  }

  public void importPhonebookJSON(String filename) throws IOException
  {
    LOGGER.info("JSON import");
    ObjectMapper objectMapper = new ObjectMapper();

    UcwarePhonebook importPhonebook = objectMapper.readValue(
      new FileReader(filename), UcwarePhonebook.class);
    //
    LOGGER.debug("Import phonebook name={}", importPhonebook.getName());

    phonebook = ucwareClient.getUserPhonebookByName(importPhonebook.getName());

    if (phonebook != null)
    {
      LOGGER.debug("phonebook != null");
      ucwareClient.deleteUserPhonebook(phonebook.getUuid());
    }

    phonebook
      = ucwareClient.newUserPhonebook(importPhonebook.getName(), false);

    LOGGER.debug("phonebook={}", phonebook);

    counter = 0;

    final List<UcwareContactGroup> ctGroup = phonebook.getGroups();

    importPhonebook.getGroups().forEach(cg ->
    {
      LOGGER.debug("phonebook group = {} {}", cg.getName(), cg.getUuid());
      boolean found = false;

      for (UcwareContactGroup cg1 : ctGroup)
      {
        if (cg.getName().equals(cg1.getName()))
        {
          found = true;
          contactGroup = cg1;
          break;
        }
      }

      LOGGER.debug("found = {}", found);

      if (!found)
      {
        contactGroup = ucwareClient
          .addUserContactGroup(phonebook.getUuid(), cg.getName());
      }

      LOGGER.debug("userContactGroup uuid={} name={}",
        contactGroup.getUuid(), contactGroup.getName());

      cg.getContacts().forEach(c ->
      {
        final UcwareContact contact = ucwareClient.
          addUserContact(contactGroup.getUuid(),
            new UcwareParamContact(c.getPrefix(), c.getFirstname(),
              c.getLastname(), c.getSuffix(), c.getSyncId()));

        c.getAttributes().forEach(a ->
        {
          ucwareClient.addUserContactAttribute(contact.getUuid(),
            new UcwareParamAttribute(a.getName(), a.getValue(), a.getType()));
        });

        counter++;
        LOGGER.info("counter={} {} {}", counter, contact.getFirstname(),
          contact.getLastname());
      });
    });
  }

  public void importPhonebook() throws IOException, CsvValidationException
  {
    LOGGER.debug("importPhonebook");
    Options.checkFilenameRequired();

    String filename = App.getOPTIONS().getFilename();

    if (!new File(filename).exists())
    {
      LOGGER.error("File {} not found!", filename);
      System.exit(0);
    }

    LOGGER.info("Importing file '{}'", filename);

    if (filename.toLowerCase().endsWith(".csv"))
    {
      importPhonebookCSV(filename);
    }
    else if (filename.toLowerCase().endsWith(".json"))
    {
      importPhonebookJSON(filename);
    }
    else
    {
      LOGGER.error("CSV or JSON files allowed only.", filename);
      System.exit(0);
    }
  }

  private final static PhonebookConfig config = App.getConfig().
    getPhonebookConfig();

  private final HashMap<String, UcwareContact> ucwareContactMap = new HashMap<>();

  private final UcwarePhonebookClient ucwareClient;

  private UcwarePhonebook phonebook;

  private UcwareContactGroup contactGroup;

  private int counter;
}
