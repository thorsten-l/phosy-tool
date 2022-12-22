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

import java.util.List;
import l9g.app.phosy.ucware.common.response.UcwareBooleanResponse;
import javax.ws.rs.client.WebTarget;
import l9g.app.phosy.ucware.common.response.UcwareArrayOfIntResponse;
import l9g.app.phosy.ucware.common.response.UcwareArrayOfStringsResponse;
import l9g.app.phosy.ucware.user.model.UcwareUser;
import l9g.app.phosy.ucware.user.requestparam.UcwareParamUser;
import l9g.app.phosy.ucware.user.response.UcwareAllUsersResponse;
import l9g.app.phosy.ucware.user.response.UcwareUserResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UcwareUserClient extends UcwareClient
{
  private final static Logger LOGGER = LoggerFactory.getLogger(
    UcwareUserClient.class.
      getName());

  public UcwareUserClient(WebTarget target)
  {
    super(target.path("/admin/user"));
  }

  // -- USER -------------------------------------------------------------------
  public List<UcwareUser> getAll()
  {
    LOGGER.debug("getAll()");

    UcwareAllUsersResponse response = postRequest("getAll",
      UcwareAllUsersResponse.class);

    return response.getUserList();
  }

  public int[] assignLicense(String username, int license)
  {
    LOGGER.debug("assignLicense({},{})", username, license);

    UcwareArrayOfIntResponse response = postRequest("assignLicense",
      new Object[]
      {
        username, license
      },
      UcwareArrayOfIntResponse.class);

    return response.getResult();
  }

  public String[] assignExtension(String username, String extension)
  {
    LOGGER.debug("assignExtension({},{})", username, extension);

    UcwareArrayOfStringsResponse response = postRequest("assignExtension",
      new Object[]
      {
        username, extension, true
      },
      UcwareArrayOfStringsResponse.class);

    return response.getResult();
  }

  public UcwareUser newUser(UcwareParamUser paramUser)
  {
    LOGGER.debug("newUser({})", paramUser);

    UcwareUserResponse response = postRequest("newUser",
      new Object[]
      {
        paramUser
      },
      UcwareUserResponse.class);

    return response.getUser();
  }

  public UcwareUser getUser(String username)
  {
    LOGGER.debug("getUser({})", username);

    UcwareUserResponse response = postRequest("getUser",
      new String[]
      {
        username
      },
      UcwareUserResponse.class);

    return response.getUser();
  }

  public boolean deleteUser(String username)
  {
    LOGGER.debug("deleteUser({})", username);

    UcwareBooleanResponse response = postRequest("deleteUser",
      new String[]
      {
        username
      },
      UcwareBooleanResponse.class);

    return response.isResult();
  }

  public boolean setPassword(String username, String password)
  {
    LOGGER.debug("setPassword()");

    UcwareBooleanResponse response = postRequest("setPassword",
      new String[]
      {
        username, password
      },
      UcwareBooleanResponse.class);

    return response.isResult();
  }

  public boolean setPin(String username, String pin)
  {
    LOGGER.debug("setPin()");

    UcwareBooleanResponse response = postRequest("setPin",
      new String[]
      {
        username, pin
      },
      UcwareBooleanResponse.class);

    return response.isResult();
  }

}
