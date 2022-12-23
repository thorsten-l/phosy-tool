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
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
import java.text.MessageFormat;
import java.util.HashMap;
import l9g.app.phosy.App;
import l9g.app.phosy.ldap.ConnectionHandler;
import l9g.app.phosy.config.LdapConfig;
import l9g.app.phosy.config.LdapUcwareType;
import l9g.app.phosy.config.PhonebookConfig;
import l9g.app.phosy.ldap.LdapUtil;
import l9g.app.phosy.ucware.UcwareAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class PhonebookLdapHandler
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(PhonebookLdapHandler.class.getName());

  private final static PhonebookLdapHandler SINGLETON = new PhonebookLdapHandler();

  private PhonebookLdapHandler()
  {
  }

  public static PhonebookLdapHandler getInstance()
  {
    return SINGLETON;
  }

  public static String buildSyncId(Entry entry)
  {
    String syncId = null;

    try
    {
      syncId = phonebookConfig.getPhonebookName() + ":" + DN.normalize(entry.
        getDN());
    }
    catch (LDAPException e)
    {
      LOGGER.error("Build syncId failed ", e);
      System.exit(0);
    }

    return syncId;
  }

  public void readLdapEntries(
    ASN1GeneralizedTime lastSyncTimestamp, boolean withAttributes)
    throws Throwable
  {
    ldapEntryMap.clear();
    String baseDn = ldapConfig.getBaseDn();
    LDAPConnection connection = new ConnectionHandler(ldapConfig).
      getConnection();

    String filter = new MessageFormat(
      ldapConfig.getFilter()).format(new Object[]
    {
      lastSyncTimestamp.toString()
    });

    LOGGER.debug("filter={}", filter);

    SearchRequest searchRequest;

    if (withAttributes)
    {
      String[] attributeNames = new String[phonebookConfig.getMapEntry().size()];
      int i = 0;
      for (LdapUcwareType luType : phonebookConfig.getMapEntry())
      {
        attributeNames[i] = luType.getName();
        LOGGER.debug("attributeName[{}] = {}", i, attributeNames[i]);
        i++;
      }

      searchRequest = new SearchRequest(baseDn, SearchScope.SUB, filter,
        attributeNames);
    }
    else
    {
      searchRequest = new SearchRequest(baseDn, SearchScope.SUB, filter, "dn");
    }

    int totalSourceEntries = 0;
    ASN1OctetString resumeCookie = null;
    SimplePagedResultsControl responseControl = null;

    int pagedResultSize = ldapConfig.getPagedResultSize() > 0
      ? ldapConfig.getPagedResultSize() : 1000;

    do
    {
      searchRequest.setControls(
        new SimplePagedResultsControl(pagedResultSize, resumeCookie));

      SearchResult sourceSearchResult = connection.search(searchRequest);

      int sourceEntries = sourceSearchResult.getEntryCount();
      totalSourceEntries += sourceEntries;

      if (sourceEntries > 0)
      {
        LOGGER.debug("build list from source DNs, {} entries", sourceEntries);

        for (Entry entry : sourceSearchResult.getSearchEntries())
        {
          ldapEntryMap.put(buildSyncId(entry), entry);
        }

        responseControl = SimplePagedResultsControl.get(sourceSearchResult);
        if ( responseControl != null )
        {
          resumeCookie = responseControl.getCookie();
        }
      }
    }
    while (responseControl != null && responseControl.moreResultsToReturn());

    if (totalSourceEntries == 0)
    {
      LOGGER.info("No entries to synchronize found");
    }
    else
    {
      LOGGER.info("build list from source DNs, {} entries", totalSourceEntries);    
    }
  }

  @Getter
  private final static HashMap<String, Entry> ldapEntryMap = new HashMap<>();

  private final static PhonebookConfig phonebookConfig = App.getConfig().
    getPhonebookConfig();

  private final static LdapConfig ldapConfig = App.getConfig().
    getPhonebookConfig().getLdapConfig();
}
