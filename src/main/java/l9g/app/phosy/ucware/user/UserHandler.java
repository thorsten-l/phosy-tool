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

import com.unboundid.ldap.sdk.Entry;
import java.util.HashMap;
import java.util.List;
import javax.script.Bindings;
import l9g.app.phosy.App;
import l9g.app.phosy.config.MatchEntry;
import l9g.app.phosy.config.MatchType;
import l9g.app.phosy.config.UserConfig;
import l9g.app.phosy.ldap.LdapUtil;
import l9g.app.phosy.ucware.UcwareClientFactory;
import static l9g.app.phosy.ucware.UcwareAttributeType.*;
import l9g.app.phosy.ucware.UcwareUserClient;
import l9g.app.phosy.ucware.user.model.UcwareUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UserHandler
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(UserHandler.class.getName());

  private final static UserHandler SINGLETON = new UserHandler();

  private UserHandler()
  {
    ucwareClient = UcwareClientFactory.getUserClient(
      App.getConfig().getUserConfig().getUcwareConfig());
  }

  public static UserHandler getInstance()
  {
    return SINGLETON;
  }

  /*
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
   */
  public UcwareUser getUser(String username) throws Throwable
  {
    LOGGER.debug("getUser({})", username);
    return ucwareClient.getUser(username);
  }

  public void readAllUsers() throws Throwable
  {
    LOGGER.debug("readAllUsers");
    ucwareUserMap.clear();
    List<UcwareUser> userList = ucwareClient.getAll();
    if (userList != null && !userList.isEmpty())
    {
      for (UcwareUser user : userList)
      {
        ucwareUserMap.put(user.getUsername().trim().toLowerCase(), user);
      }
    }
    LOGGER.debug("{} user in ucware user map", ucwareUserMap.size());
  }

  private boolean matchIgnoreList(String value)
  {
    boolean match = false;

    for (MatchEntry matchEntry : config.getIgnoreList())
    {
      String entryUid = matchEntry.getValue();
      
      if (matchEntry.getMatch() == MatchType.equals
        && value.equals(entryUid))
      {
        match = true;
        break;
      }

      if (matchEntry.getMatch() == MatchType.startsWith
        && value.startsWith(entryUid))
      {
        match = true;
        break;
      }

      if (matchEntry.getMatch() == MatchType.endsWith
        && value.endsWith(entryUid))
      {
        match = true;
        break;
      }

      if (matchEntry.getMatch() == MatchType.contains
        && value.contains(entryUid))
      {
        match = true;
        break;
      }
    }

    return match;
  }

  public void removeUnknownUser() throws Throwable
  {
    LOGGER.debug("removeUnknownUser");
    
    HashMap<String, Entry> ldapEntryMap
      = UserLdapHandler.getInstance().getLdapEntryMap();
    
    LOGGER.debug("ignore list={}", config.getIgnoreList());
        
    for (String uid : ucwareUserMap.keySet().toArray(new String[0]))
    {
      if (!ldapEntryMap.containsKey(uid) && !matchIgnoreList(uid))
      {
        LOGGER.debug( "- removing {}", uid);
        // TODO: remove user
      }
    }
  }

  public void createUpdateUsers() throws Throwable
  {
    LOGGER.debug("createUpdateUsers");

    UserScriptHandler userScriptHandler = UserScriptHandler.getInstance();
    
    for (Entry entry : UserLdapHandler.getInstance().getLdapEntryMap().values())
    {
      LdapUtil ldapUtil = new LdapUtil(config, entry);
      String uid = ldapUtil.value(LDAP_UID).trim().toLowerCase();

      if (!matchIgnoreList(uid))
      {
        Bindings bindings = userScriptHandler.run(config, entry);
        
        if ( ucwareUserMap.containsKey(uid))
        {
          LOGGER.debug("* updating {} {} {} {}",
          ldapUtil.value(LDAP_UID),
          ldapUtil.value(LDAP_MAIL),
          ldapUtil.value(LDAP_TELEPHONENUMBER),
          bindings.get("locality"));
        }
        else
        {
          LOGGER.debug("+ creating {} {} {} {}",
          ldapUtil.value(LDAP_UID),
          ldapUtil.value(LDAP_MAIL),
          ldapUtil.value(LDAP_TELEPHONENUMBER),
          bindings.get("locality"));          
        }
      }
      else
      {
        LOGGER.debug("# ignoring {} {} {}",
          ldapUtil.value(LDAP_UID),
          ldapUtil.value(LDAP_MAIL),
          ldapUtil.value(LDAP_TELEPHONENUMBER));
      }
    }
  }

  private final HashMap<String, UcwareUser> ucwareUserMap = new HashMap<>();

  private final static UserConfig config = App.getConfig().getUserConfig();

  private final UcwareUserClient ucwareClient;
}
