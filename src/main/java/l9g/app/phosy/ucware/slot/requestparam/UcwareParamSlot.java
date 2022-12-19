/*
 * Copyright 2022 Thorsten Ludewig (t.ludewig@gmail.com)
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
package l9g.app.phosy.ucware.slot.requestparam;

import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 *
 */
@Getter
@ToString
public class UcwareParamSlot
{
  public UcwareParamSlot(String deviceType, String name, int userId)
  {
    this.deviceType = deviceType;
    this.name = name;
    this.userId = userId;

    this.clipExternal = "";
    this.clipInternal = "";
    this.clirExternal = false;
    this.clirInternal = false;

    this.callVolume = 8;
    this.ringerVolume = 8;
  }

  private final String clipInternal;

  private final String clipExternal;

  private final boolean clirInternal;

  private final boolean clirExternal;

  private final String deviceType;

  private final String name;

  private final int userId;

  private final int callVolume;

  private final int ringerVolume;
}
