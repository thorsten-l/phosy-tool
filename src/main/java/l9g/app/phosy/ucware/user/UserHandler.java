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
import l9g.app.phosy.App;
import l9g.app.phosy.config.UserConfig;
import l9g.app.phosy.ucware.UcwareClientFactory;
import l9g.app.phosy.ucware.UcwareAttributeType;
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
  
  public UcwareUser getUser(String username) throws Throwable
  {
    LOGGER.debug("getUser({})", username);
    return ucwareClient.getUser(username);
  }
  
  private final static UserConfig config = App.getConfig().getUserConfig();
  
  private final UcwareUserClient ucwareClient;
}
