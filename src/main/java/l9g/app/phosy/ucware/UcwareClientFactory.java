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
package l9g.app.phosy.ucware;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import l9g.app.phosy.App;
import l9g.app.phosy.BuildProperties;
import l9g.app.phosy.config.Configuration;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UcwareClientFactory
{
  private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(
    UcwareClientFactory.class.getName());

  private UcwareClientFactory()
  {
  }

  public static UcwareClient getClient()
  {
    Client client = ClientBuilder.newClient().register(
      new BasicAuthenticator(config.getUcwareHost().getCredentials().getUid(),
        config.getUcwareHost().getCredentials().getPassword()));

    if ("development".equals(BuildProperties.getInstance().getProfile()))
    {
      client = client.register(new LoggingFeature(Logger.getLogger(
        UcwareClientFactory.class.getName()), Level.INFO, null, null));
    }

    WebTarget target = client.target(config.getUcwareHost().getApiUrl());
    return new UcwareClient(target);
  }
  
  private final static Configuration config = App.getConfig();;
}
