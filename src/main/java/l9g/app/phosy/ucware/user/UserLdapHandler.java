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
package l9g.app.phosy.ucware.user;

import com.unboundid.asn1.ASN1GeneralizedTime;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import l9g.app.phosy.App;
import l9g.app.phosy.Options;
import l9g.app.phosy.ldap.ConnectionHandler;
import l9g.app.phosy.config.LdapConfig;
import l9g.app.phosy.config.LdapUcwareType;
import l9g.app.phosy.config.UserConfig;
import l9g.app.phosy.ldap.LdapUtil;
import l9g.app.phosy.ucware.UcwareAttributeType;
import l9g.app.phosy.ucware.user.model.UcwareUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UserLdapHandler
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(UserLdapHandler.class.getName());

  private final static UserLdapHandler SINGLETON = new UserLdapHandler();

  private UserLdapHandler()
  {
  }

  public static UserLdapHandler getInstance()
  {
    return SINGLETON;
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

    String uidAttributeName = null;

    String[] attributeNames = new String[userConfig.getMapEntry().size()];
    int i = 0;
    for (LdapUcwareType luType : userConfig.getMapEntry())
    {
      attributeNames[i] = luType.getName();
      LOGGER.debug("attributeName[{}] = {}", i, attributeNames[i]);
      if (luType.getType() == UcwareAttributeType.LDAP_UID)
      {
        uidAttributeName = luType.getName();
      }
      i++;
    }

    if (uidAttributeName == null)
    {
      LOGGER.error(
        "ERROR: LDAP_UID must be set in config.xml <attributeTypeMapping>");
      System.exit(-1);
    }
    else
    {
      LOGGER.debug("uidAttributeName={}", uidAttributeName);
    }

    if (withAttributes)
    {
      searchRequest = new SearchRequest(baseDn, SearchScope.SUB, filter,
        attributeNames);
    }
    else
    {
      searchRequest = new SearchRequest(baseDn, SearchScope.SUB, filter,
        uidAttributeName);
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
          LdapUtil ldapUtil = new LdapUtil(userConfig, entry);
          ldapEntryMap.put(
            ldapUtil.value(UcwareAttributeType.LDAP_UID).trim().toLowerCase(),
            entry);
        }

        responseControl = SimplePagedResultsControl.get(sourceSearchResult);

        if (responseControl != null)
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

  public void readAllLdapEntryUIDs() throws Throwable
  {
    readLdapEntries(new ASN1GeneralizedTime(0), false);
  }

  public String addRoles(Options options, UserHandler userHandler)
    throws Throwable
  {
    LDAPConnection connection = new ConnectionHandler(ldapConfig).
      getConnection();
    String baseDn = ldapConfig.getBaseDn();

    String ucwareRole = DN.normalize(options.getLdapRole());

    LOGGER.info( "Searching for UCware auth backend : {}", options.getAuthBackendName());
    LOGGER.info( "Setting LDAP role: {}", ucwareRole );
    
    for (UcwareUser ucwareUser : userHandler.getUcwareUserMap().values())
    {
      if (options.getAuthBackendName().equals(ucwareUser.getAuthBackend()))
      {
        SearchRequest searchRequest = new SearchRequest(baseDn, SearchScope.SUB,
          "(uid=" + ucwareUser.getUsername() + ")", "nsRoleDn");

        SearchResult sourceSearchResult = connection.search(searchRequest);

        int sourceEntries = sourceSearchResult.getEntryCount();

        if (sourceEntries == 1)
        {
          SearchResultEntry entry
            = sourceSearchResult.getSearchEntries().get(0);
          String entryDn = DN.normalize(entry.getDN());
          String[] entryRoles = entry.getAttributeValues("nsRoleDN");

          LOGGER.debug("{} {} : {} : {} : {}", ucwareUser.getFirstname(),
            ucwareUser.getLastname(), ucwareUser.getUsername(),
            ucwareUser.getAuthBackend(), entryDn);

          LOGGER.debug("  * {}", ucwareUser.getExternalId());

          boolean userHasNotUcwareRole = true;

          for (String role : entryRoles)
          {
            String nsRoleDN = DN.normalize(role);
            LOGGER.debug("  - {}", nsRoleDN);
            if (ucwareRole.equals(nsRoleDN))
            {
              userHasNotUcwareRole = false;
              break;
            }
          }

          if (userHasNotUcwareRole)
          {
            LOGGER.debug("  + ADDING role {}", ucwareRole);

            List<Modification> modifications = new ArrayList<>();

            modifications.add(new Modification(
              ModificationType.ADD, "nsRoleDn", ucwareRole));

            // System.exit(0);
            if (options.isDryRun() == false)
            {
              connection.modify(entryDn, modifications);
            }
          }

          if (options.isDryRun() == false)
          {
            userHandler.modifyExternalId(ucwareUser, entryDn);
          }
        }
      }
    }
    return null;
  }

  @Getter
  private final HashMap<String, Entry> ldapEntryMap = new HashMap<>();

  private final static UserConfig userConfig = App.getConfig().
    getUserConfig();

  private final static LdapConfig ldapConfig = userConfig.getLdapConfig();
}
