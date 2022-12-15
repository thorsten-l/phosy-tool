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

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class BasicAuthenticator implements ClientRequestFilter
{

  /**
   * Constructs ...
   *
   *
   * @param user
   * @param password
   */
  public BasicAuthenticator(String user, String password)
  {
    this.user = user;
    this.password = password;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param requestContext
   *
   * @throws IOException
   */
  @Override
  public void filter(ClientRequestContext requestContext) throws IOException
  {
    requestContext.getHeaders().add("Authorization",
      "Basic "
      + DatatypeConverter.printBase64Binary((this.user + ":"
        + this.password).getBytes("UTF-8")));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final String password;

  /** Field description */
  private final String user;
}
