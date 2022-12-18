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

  public UcwareClient(WebTarget target)
  {
    this.target = target;
  }

  public <T> T postRequest(String method, Object[] param, Class<T> type)
  {
    LOGGER.debug("postRequest({}, {}, {})", method, param, type);

    return target.request(MediaType.APPLICATION_JSON).
      post(Entity.entity(new UcwareRequest(method, param),
        MediaType.APPLICATION_JSON), type);
  }

  public <T> T postRequest(String method, Class<T> type)
  {
    return postRequest(method, null, type);
  }
}
