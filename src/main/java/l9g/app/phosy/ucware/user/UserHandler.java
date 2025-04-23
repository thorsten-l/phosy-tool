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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.script.Bindings;
import l9g.app.phosy.App;
import l9g.app.phosy.LogbackConfig;
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
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UserHandler
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(UserHandler.class.getName());

  private final static UserHandler SINGLETON = new UserHandler();

  private final static Marker MARKER
    = LogbackConfig.getInstance().getNotificationMarker();

  private UserHandler()
  {
    UcwareConfig ucwareConfig
      = App.getConfig().getUserConfig().getUcwareConfig();

    dryRun = App.getOPTIONS().isDryRun();

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

  public void readIgnoreGroups()
  {
    try
    {
      adminsGroup = groupClient.getGroupByName("admins");
      syncIgnoreGroup = groupClient.getGroupByName("syncignore", true);
    }
    catch (Throwable t)
    {
      // 
    }
  }

  private boolean saveDeleteUser(UcwareUser user)
  {
    boolean result = false;
    boolean ignore = false;

    if (user.getExternalId() != null
      && user.getExternalId().trim().length() > 0)
    {

      if (user.getGroups() != null)
      {
        for (int g : user.getGroups())
        {
          if (g == adminsGroup.getId()
            || ((syncIgnoreGroup != null) && g == syncIgnoreGroup.getId()))
          {
            ignore = true;
            break;
          }
        }

        if (ignore)
        {
          LOGGER.warn("\n{}\n{} {}\n{}",
            user.getUsername(),
            user.getFirstname(), user.getLastname(),
            user.getEmail());
          LOGGER.warn(MARKER,
            "IGNORE: DELETE user {} is in admins or syncIgnore group.",
            user.getUsername());
        }
        else
        {
          if (dryRun)
          {
            LOGGER.info("DRYRUN: - save delete user = {}", user.getUsername());
          }
          else
          {
            LOGGER.info("- save delete user = {}", user.getUsername());
            result = userClient.deleteUser(user.getId());
          }
        }
      }
    }
    else
    {
      LOGGER.debug("IGNORE: DELETE user {} has no external id", user.
        getUsername());
    }
    return result;
  }

  private boolean saveUpdateUser(
    LdapUtil ldapUtil, Bindings bindings, UcwareUser user, Entry entry) throws
    Throwable
  {
    boolean result = false;
    boolean ignore = false;

    if (user.getExternalId() != null
      && user.getExternalId().trim().length() > 0)
    {
      for (int g : user.getGroups())
      {
        if (g == adminsGroup.getId()
          || ((syncIgnoreGroup != null) && g == syncIgnoreGroup.getId()))
        {
          ignore = true;
          break;
        }
      }

      if (ignore)
      {
        LOGGER.warn("IGNORE: UPDATE user {} is in admins or syncIgnore group. {} {} ({})",
          user.getUsername(),
          user.getFirstname(), user.getLastname(),
          user.getEmail());
      }
      else
      {
        if (dryRun)
        {
          LOGGER.info("DRYRUN: * save update user = {}", user.getUsername());
        }
        else
        {
          LOGGER.info("* save update user = {}", user.getUsername());

          if (user.getExtensions().length > 1)
          {
            LOGGER.warn(MARKER,
              "IGNORE: UPDATE user {} has more than one phonenumber (extension).\n\n"
              + "If necessary, when something concerning UCware has changed, an administrator must update the entry in UCware manually:\n"
              + "- LDAP entry:   {}, {} | {} | {}\n"
              + "- UCware entry: {}, {} | {} | {}",
              user.getUsername(),
              ldapUtil.value(LDAP_SN), ldapUtil.value(LDAP_GIVENNAME),
              ldapUtil.value(LDAP_MAIL), ldapUtil.value(LDAP_TELEPHONENUMBER),
              user.getLastname(), user.getFirstname(),
              user.getEmail(), Arrays.toString(user.getExtensions()) );
          }
          else
          {
            String ucwarePhonenumber
              = (user.getExtensions() != null && user.getExtensions().length
              == 1)
                ? user.getExtensions()[0] : null;

            String ldapPhonenumber = ldapUtil.value(LDAP_TELEPHONENUMBER);

            if (ucwarePhonenumber != null && ldapPhonenumber != null
              && ucwarePhonenumber.equals(ldapPhonenumber))
            {
              // update
              LOGGER.info("  ** update user = {}", user.getUsername());

              //////////////////////////////////////////////////////////////////
              // update user
              //////////////////////////////////////////////////////////////////
              UcwareParamUser paramUser = new UcwareParamUser(
                bindings, config.getDefaultAuthBackend(),
                config.getDefaultLanguage());

              /*
              LOGGER.info("ucwarePhonenumber={}", ucwarePhonenumber);
              LOGGER.info("ldapPhonenumber={}", ldapPhonenumber);
              LOGGER.info("user={}", user);
              LOGGER.info("paramUser={}", paramUser);
               */
              // UcwareUser updatedUser = 
              userClient.updateUser(paramUser);
              // LOGGER.info("updated user={}", updatedUser);
            }
            else
            {
              //
              LOGGER.info("  ** recreate user = {}", user.getUsername());
              saveDeleteUser(user);
              createUsers(ldapUtil, bindings, entry);
            }
          }
        }
      }
    }
    else
    {
      LOGGER.debug("IGNORE: UPDATE user {} has no external id", user.
        getUsername());
    }

    return result;
  }

  public void removeUnknownUser() throws Throwable
  {
    LOGGER.debug("removeUnknownUser");

    HashMap<String, Entry> ldapEntryMap
      = UserLdapHandler.getInstance().getLdapEntryMap();

    for (UcwareUser user : ucwareUserMap.values())
    {
      if (!ldapEntryMap.containsKey(user.getUsername().trim().toLowerCase()))
      {
        LOGGER.debug("- removing {}", user.getUsername());
        saveDeleteUser(user);
      }
    }
  }

  public void removeAllSyncedUsers() throws Throwable
  {
    LOGGER.debug("removeAllSyncedUsers");

    for (UcwareUser user : ucwareUserMap.values())
    {
      if (user.getExternalId() != null
        && user.getExternalId().trim().length() > 0)
      {
        LOGGER.info("- removing {} = {}", user.getUsername(),
          saveDeleteUser(user));
      }
      else
      {
        LOGGER.info("- NOT removing {}", user.getUsername());
      }
    }
  }

  public void createUsers(
    LdapUtil ldapUtil, Bindings bindings, Entry entry) throws Throwable
  {
    if (dryRun)
    {
      LOGGER.info("DRYRUN: + creating {} {} {} {}",
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

      UcwareParamUser paramUser = new UcwareParamUser(
        bindings, config.getDefaultAuthBackend(),
        config.getDefaultLanguage());

      LOGGER.debug("paramUser={}", paramUser);

      UcwareUser user = userClient.newUser(paramUser);

      LOGGER.debug("new user = {}", user);

      // assign licenses
      ArrayList<Integer> licenses = (ArrayList) bindings.get("licenses");
      if (licenses != null)
      {
        for (int license : licenses)
        {
          userClient.assignLicense(user.getId(), license);
        }
      }

      // assign group members
      ArrayList<String> groupNames = (ArrayList) bindings.
        get("groupNames");

      if (groupNames != null)
      {
        for (String groupName : groupNames)
        {
          UcwareGroup group = groupClient.getGroupByName(groupName);
          groupClient.assignMember(user.getId(), group.getId());
        }
      }

      // assign extension (phonenumber)
      String phoneNumber = (String) bindings.get("phoneNumber");

      if (phoneNumber != null && phoneNumber.trim().length() > 0)
      {
        userClient.assignExtension(user.getId(), phoneNumber);

        // mac Slot
        ArrayList<String> slotTypes = (ArrayList) bindings.
          get("slotTypes");
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
            case "app":
              slot = slotClient.newSlot(
                new UcwareParamSlot(slotType, "Mobile-App", user.getId())
              );
              break;
          }

          if (slot != null)
          {
            LOGGER.debug("slot={}", slot);
            slotClient.assignExtension(slot.getId(), phoneNumber);
          }
        }
      }

      user = userClient.getUser(user.getUsername());
      LOGGER.debug("user = {}", user);
    }
  }

  public void createUpdateUsers() throws Throwable
  {
    LOGGER.debug("createUpdateUsers");

    for (Entry entry : UserLdapHandler.getInstance().getLdapEntryMap().values())
    {
      LdapUtil ldapUtil = new LdapUtil(config, entry);
      String uid = ldapUtil.value(LDAP_UID).trim().toLowerCase();
      Bindings bindings = UserScriptHandler.run(config, entry);

      // if (true)     // !matchIgnoreList(uid))
      // {
      if (ucwareUserMap.containsKey(uid))
      {
        if (bindings.get("doNotUpdate") != null
          && ((Boolean) bindings.get("doNotUpdate")))
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
          UcwareUser user = ucwareUserMap.get(ldapUtil.value(LDAP_UID));

          if (App.getOPTIONS().isUpdateRecreate())
          {
            if ( saveDeleteUser(user))
            {
              createUsers(ldapUtil, bindings, entry);
            }
          }
          else
          {
            saveUpdateUser(ldapUtil, bindings, user, entry);
          }
        }
      }
      else
      {
        if (bindings.get("doNotCreate") != null
          && ((Boolean) bindings.get("doNotCreate")))
        {
          LOGGER.info("- not creating {} {} {} {}",
            ldapUtil.value(LDAP_UID),
            ldapUtil.value(LDAP_MAIL),
            ldapUtil.value(LDAP_TELEPHONENUMBER),
            bindings.get("locality"));
        }
        else
        {
          createUsers(ldapUtil, bindings, entry);
        }
      }
    }
  }

  public void modifyExternalId(UcwareUser ucwareUser, String entryDn)
  {
    if (!entryDn.equals(ucwareUser.getExternalId()))
    {
      LOGGER.debug(
        "modifyExternalId {} : {}", ucwareUser.getUsername(), entryDn);
      UcwareParamUser paramUser = new UcwareParamUser(
        ucwareUser, entryDn);
      userClient.updateUser(paramUser);
    }
  }

  @Getter
  private final HashMap<String, UcwareUser> ucwareUserMap = new HashMap<>();

  private final static UserConfig config = App.getConfig().getUserConfig();

  private final UcwareUserClient userClient;

  private final UcwareGroupClient groupClient;

  private final UcwareSlotClient slotClient;

  private final boolean dryRun;

  private UcwareGroup adminsGroup;

  private UcwareGroup syncIgnoreGroup;
}
