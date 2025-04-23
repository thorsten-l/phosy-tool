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

import java.util.Map;
import jakarta.ws.rs.client.WebTarget;
import l9g.app.phosy.ucware.common.response.UcwareArrayOfIntResponse;
import l9g.app.phosy.ucware.group.model.UcwareGroup;
import l9g.app.phosy.ucware.group.response.UcwareAllGroupsResponse;
import l9g.app.phosy.ucware.group.response.UcwareGroupResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UcwareGroupClient extends UcwareClient
{
  private final static Logger LOGGER = LoggerFactory.getLogger(
    UcwareGroupClient.class.
      getName());

  public UcwareGroupClient(WebTarget target)
  {
    super(target.path("/admin/group"));
  }

  // -- USER -------------------------------------------------------------------
  public Map<String, UcwareGroup> getAll()
  {
    LOGGER.debug("getAll()");

    UcwareAllGroupsResponse response = postRequest("getAll",
      UcwareAllGroupsResponse.class);

    return response.getGroupsMap();
  }

  public UcwareGroup getGroupByName(String groupName)
  {
    LOGGER.debug("getGroupByName({})", groupName);
    UcwareGroupResponse response = postRequest("getGroupByName",
      new Object[]
      {
        groupName
      },
      UcwareGroupResponse.class);

    LOGGER.debug("ucware group = {}", response.getGroup());
    return response.getGroup();
  }

  public UcwareGroup getGroupByName(String groupName, boolean ignoreErrors)
  {
    LOGGER.debug("getGroupByName({})", groupName);
    UcwareGroupResponse response = postRequest("getGroupByName",
      new Object[]
      {
        groupName
      },
      UcwareGroupResponse.class, ignoreErrors);

    LOGGER.debug("ucware group = {}", response.getGroup());
    return response.getGroup();
  }

  public int[] assignMember(int memberId, int groupId)
  {
    LOGGER.debug("assignMember({},{})", memberId, groupId);

    UcwareArrayOfIntResponse response = postRequest("assignMember",
      new Object[]
      {
        memberId, groupId
      },
      UcwareArrayOfIntResponse.class);

    return response.getResult();
  }

}
