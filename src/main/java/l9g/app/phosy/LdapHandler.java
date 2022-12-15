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

import com.unboundid.asn1.ASN1GeneralizedTime;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import java.text.MessageFormat;
import java.util.HashMap;
import l9g.app.phosy.config.Configuration;
import l9g.app.phosy.config.LdapUcwareType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class LdapHandler
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(LdapHandler.class.getName());

  private final static LdapHandler SINGLETON = new LdapHandler();

  private LdapHandler()
  {
  }

  public static LdapHandler getInstance()
  {
    return SINGLETON;
  }

  public static String buildSyncId(Entry entry)
  {
    String syncId = null;

    try
    {
      syncId = config.getPhonebookName() + ":" + DN.normalize(entry.getDN());
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
    String baseDn = App.getConfig().getBaseDn();
    LDAPConnection connection = ConnectionHandler.getSourceConnection();

    String filter = new MessageFormat(
      config.getFilter()).format(new Object[]
    {
      lastSyncTimestamp.toString()
    });

    LOGGER.debug("filter={}", filter);

    SearchRequest searchRequest;

    if (withAttributes)
    {
      String[] attributeNames = new String[config.getMapEntry().size()];
      int i = 0;
      for (LdapUcwareType luType : config.getMapEntry())
      {
        attributeNames[i] = luType.getLdapName();
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

    SearchResult sourceSearchResult = connection.search(searchRequest);

    int sourceEntries = sourceSearchResult.getEntryCount();

    if (sourceEntries > 0)
    {
      LOGGER.info("build list from source DNs, {} entries", sourceEntries);

      for (Entry entry : sourceSearchResult.getSearchEntries())
      {
        ldapEntryMap.put(buildSyncId(entry), entry);
      }
    }
    else
    {
      LOGGER.info("No entries to synchronize found");
    }
  }

  @Getter
  private final static HashMap<String, Entry> ldapEntryMap = new HashMap<>();

  private final static Configuration config = App.getConfig();
}
