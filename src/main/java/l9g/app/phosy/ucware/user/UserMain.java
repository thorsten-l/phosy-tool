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

import l9g.app.phosy.Options;
import l9g.app.phosy.TimestampUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UserMain
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(UserMain.class.getName());

  private final static UserMain SINGLETON = new UserMain();

  private UserMain()
  {
  }

  public static UserMain getInstance()
  {
    return SINGLETON;
  }

  public void run(Options OPTIONS) throws Throwable
  {
    TimestampUtil timestampUtil = new TimestampUtil("users");
    UserHandler userHandler = UserHandler.getInstance();

    if (OPTIONS.isSyncUsers())
    {
      UserLdapHandler ldapHandler = UserLdapHandler.getInstance();

      userHandler.readIgnoreGroups();
      
////////////
//      userHandler.readAllUsers();
//      userHandler.removeAllSyncedUsers();
////////////
      
      userHandler.readAllUsers();
      ldapHandler.readAllLdapEntryUIDs();
      
      System.exit(0);
      
      userHandler.removeUnknownUser();

      ldapHandler.readLdapEntries(timestampUtil.getLastSyncTimestamp(), true);
      if (!ldapHandler.getLdapEntryMap().isEmpty())
      {
        userHandler.createUpdateUsers();
      }

      timestampUtil.writeCurrentTimestamp();
    }
  }
}
