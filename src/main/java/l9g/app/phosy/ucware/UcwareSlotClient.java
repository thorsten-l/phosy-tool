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
import jakarta.ws.rs.client.WebTarget;
import l9g.app.phosy.ucware.slot.model.UcwareSlot;
import l9g.app.phosy.ucware.slot.requestparam.UcwareParamSlot;
import l9g.app.phosy.ucware.slot.response.UcwareAllSlotsResponse;
import l9g.app.phosy.ucware.slot.response.UcwareSlotResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UcwareSlotClient extends UcwareClient
{
  private final static Logger LOGGER = LoggerFactory.getLogger(
    UcwareSlotClient.class.
      getName());

  public UcwareSlotClient(WebTarget target)
  {
    super(target.path("/admin/slot"));
  }

  // -- USER -------------------------------------------------------------------
  public List<UcwareSlot> getAll()
  {
    LOGGER.debug("getAll()");

    UcwareAllSlotsResponse response = postRequest("getAll",
      UcwareAllSlotsResponse.class);

    return response.getSlotList();
  }

  public UcwareSlot newSlot(UcwareParamSlot paramSlot)
  {
    LOGGER.debug("newSlot({})", paramSlot);

    UcwareSlotResponse response = postRequest("newSlot",
      new Object[]
      {
        paramSlot
      },
      UcwareSlotResponse.class);

    return response.getSlot();
  }

  public UcwareSlot assignExtension(int slotId, String extension)
  {
    LOGGER.debug("assignExtension({},{})", slotId, extension);

    UcwareSlotResponse response = postRequest("assignExtension",
      new Object[]
      {
        slotId, extension
      },
      UcwareSlotResponse.class);

    return response.getSlot();
  }

}
