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

import l9g.app.phosy.ucware.common.request.UcwareRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import l9g.app.phosy.App;
import l9g.app.phosy.Options;
import l9g.app.phosy.ucware.common.response.UcwareResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UcwareClient
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(UcwareClient.class.getName());

  private final WebTarget target;
  private final Options options = App.getOPTIONS();

  public UcwareClient(WebTarget target)
  {
    this.target = target;
  }

  public <T> T postRequest(String method, Object[] param, Class<T> type,
    boolean ignoreErrors)
  {
    T response = null;
    LOGGER.debug("postRequest(uri={}, method={}, params={}, type={})", target.getUri(), method, param, type.getSimpleName());
    
    if (options.isDryRun() == false || method.startsWith("get"))
    {
      response = target.request(MediaType.APPLICATION_JSON).
        post(Entity.entity(new UcwareRequest(method, param),
          MediaType.APPLICATION_JSON), type);

      if (response instanceof UcwareResponse)
      {
        UcwareResponse ucwareResponse = (UcwareResponse) response;
        if (ucwareResponse != null && ucwareResponse.getError() != null)
        {
          LOGGER.error("ERROR: method={} param={} error={}",
            method, param, ucwareResponse.getError());
          if (!ignoreErrors)
          {
            System.exit(-1);
          }
        }
      }

      if (response == null)
      {
        LOGGER.error("ERROR: response == null");
        System.exit(-99);
      }
    }
    return response;
  }

  public <T> T postRequest(String method, Object[] param, Class<T> type)
  {
    return postRequest(method, param, type, false);
  }

  public <T> T postRequest(String method, Class<T> type, boolean ignoreErrors)
  {
    return postRequest(method, null, type, ignoreErrors);
  }

  public <T> T postRequest(String method, Class<T> type)
  {
    return postRequest(method, null, type, false);
  }
}
