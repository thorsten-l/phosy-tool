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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.script.Bindings;
import l9g.app.phosy.App;
import l9g.app.phosy.config.MatchEntry;
import l9g.app.phosy.config.MatchType;
import l9g.app.phosy.config.UcwareConfig;
import l9g.app.phosy.config.UserConfig;
import l9g.app.phosy.ldap.LdapUtil;
import l9g.app.phosy.ucware.UcwareClientFactory;
import static l9g.app.phosy.ucware.UcwareAttributeType.*;
import l9g.app.phosy.ucware.UcwareGroupClient;
import l9g.app.phosy.ucware.UcwareSlotClient;
import l9g.app.phosy.ucware.UcwareUserClient;
import l9g.app.phosy.ucware.group.model.UcwareGroup;
import l9g.app.phosy.ucware.slot.model.UcwareSlot;
import l9g.app.phosy.ucware.slot.requestparam.UcwareParamSlot;
import l9g.app.phosy.ucware.user.model.UcwareUser;
import l9g.app.phosy.ucware.user.requestparam.UcwareParamUser;
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
    UcwareConfig ucwareConfig
      = App.getConfig().getUserConfig().getUcwareConfig();
    userClient = UcwareClientFactory.getUserClient(ucwareConfig);
    groupClient = UcwareClientFactory.getGroupClient(ucwareConfig);
    slotClient = UcwareClientFactory.getSlotClient(ucwareConfig);
  }

  public static UserHandler getInstance()
  {
    return SINGLETON;
  }

  public void readAllUsers() throws Throwable
  {
    LOGGER.debug("readAllUsers");
    ucwareUserMap.clear();
    List<UcwareUser> userList = userClient.getAll();
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
        LOGGER.info("- removing {}", uid);
        // TODO: remove user
      }
    }
  }

  /*
  
  # User anlegen
        (OK, result) = adminUser.newUser(user)

	# User Passwort setzen
        (OK, result) = adminUser.setPassword(userID, password)

	x = setPrivacyFlag(userID, privacy)

	# Lizenz zuweisen
        # Typ 5 = Bundle
        (OK, lic) = adminUser.assignLicense(userID, 5)
 

        # User in Standort-Gruppe zufügen, damit er ausgehend telefonieren kann
        (OK, result) = adminGroup.getGroupByName(UserGroup)

        # Durchwahl zuweisen
	(OK, result) = adminUser.assignExtension(userID, newExtension)

	# Slot für Tischtelefon
        (OK, result) = adminSlot.newSlot(userID, newExtension, "mac")

	# Durchwahl auf Tischtelefon zuweisen
        (OK, result) = adminSlot.assignExtensionToSlot(newExtension, slotid)

        # Slot für UCC-Client
        (OK, result) = adminSlot.newSlot(userID, newExtension, "webrtc")

	# Durchwahl auf UCC-Client
        (OK, result) = adminSlot.assignExtensionToSlot(newExtension, slotid)
  
   */
  public void createUpdateUsers() throws Throwable
  {
    LOGGER.debug("createUpdateUsers");

    for (Entry entry : UserLdapHandler.getInstance().getLdapEntryMap().values())
    {
      LdapUtil ldapUtil = new LdapUtil(config, entry);
      String uid = ldapUtil.value(LDAP_UID).trim().toLowerCase();
      Bindings bindings = UserScriptHandler.run(config, entry);

      if (!matchIgnoreList(uid))
      {
        if (ucwareUserMap.containsKey(uid))
        {
          if (((Boolean) bindings.get("doNotUpdate")).booleanValue())
          {
            LOGGER.info("- not updating {} {} {} {}",
              ldapUtil.value(LDAP_UID),
              ldapUtil.value(LDAP_MAIL),
              ldapUtil.value(LDAP_TELEPHONENUMBER),
              bindings.get("locality"));
          }
          else
          {
            LOGGER.info("* updating {} {} {} {}",
              ldapUtil.value(LDAP_UID),
              ldapUtil.value(LDAP_MAIL),
              ldapUtil.value(LDAP_TELEPHONENUMBER),
              bindings.get("locality"));
            // TODO: update user
          }
        }
        else
        {
          if (((Boolean) bindings.get("doNotCreate")).booleanValue())
          {
            LOGGER.info("- not creating {} {} {} {}",
              ldapUtil.value(LDAP_UID),
              ldapUtil.value(LDAP_MAIL),
              ldapUtil.value(LDAP_TELEPHONENUMBER),
              bindings.get("locality"));
          }
          else
          {
            LOGGER.info("+ creating {} {} {} {}",
              ldapUtil.value(LDAP_UID),
              ldapUtil.value(LDAP_MAIL),
              ldapUtil.value(LDAP_TELEPHONENUMBER),
              bindings.get("locality"));

            // TODO: create new user
            UcwareUser user = userClient.newUser(
              new UcwareParamUser(bindings)
            );

            LOGGER.debug("new user = {}", user);

            // assign licenses
            ArrayList<Integer> licenses = (ArrayList) bindings.get("licenses");
            for (int license : licenses)
            {
              userClient.assignLicense(user.getUsername(), license);
            }

            // assign group members
            ArrayList<String> groupNames = (ArrayList) bindings.
              get("groupNames");
            for (String groupName : groupNames)
            {
              UcwareGroup group = groupClient.getGroupByName(groupName);
              groupClient.assignMember(user.getId(), group.getId());
            }

            // assign extension (phonenumber)
            String phoneNumber = (String) bindings.get("phoneNumber");

            userClient.assignExtension(user.getUsername(), phoneNumber);

            // mac Slot
            ArrayList<String> slotTypes = (ArrayList) bindings.get("slotTypes");
            for (String slotType : slotTypes)
            {
              UcwareSlot slot = null;

              switch (slotType)
              {
                case "mac":
                  slot = slotClient.newSlot(
                    new UcwareParamSlot(slotType, "Tischtelefon", user.getId())
                  );
                  break;

                case "webrtc":
                  slot = slotClient.newSlot(
                    new UcwareParamSlot(slotType, "UCC-Client", user.getId())
                  );
                  break;

                case "sip-ua":
                  slot = slotClient.newSlot(
                    new UcwareParamSlot(slotType, "Softphone", user.getId())
                  );
                  break;

                case "mobile":
                  slot = slotClient.newSlot(
                    new UcwareParamSlot(slotType, "Mobiltelefon", user.getId())
                  );
                  break;

                case "ipei":
                  slot = slotClient.newSlot(
                    new UcwareParamSlot(slotType, "DECT-Telefon", user.getId())
                  );
                  break;
              }

              // ---------------------------------------------------------------
              if (slot != null)
              {
                //
                LOGGER.debug("slot={}", slot);
                slotClient.assignExtension(slot.getId(), phoneNumber);
              }
            }

            user = userClient.getUser(user.getUsername());
            LOGGER.debug("user = {}", user);

            System.exit(0);
          }
        }
      }
      else
      {
        LOGGER.info("# ignoring {} {} {}",
          ldapUtil.value(LDAP_UID),
          ldapUtil.value(LDAP_MAIL),
          ldapUtil.value(LDAP_TELEPHONENUMBER));
      }
    }
  }

  private final HashMap<String, UcwareUser> ucwareUserMap = new HashMap<>();

  private final static UserConfig config = App.getConfig().getUserConfig();

  private final UcwareUserClient userClient;

  private final UcwareGroupClient groupClient;

  private final UcwareSlotClient slotClient;
}
