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
package l9g.app.phosy.ucware.slot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UcwareSlot
{
  private int id;

  private String name;

  private String deviceType;

  private String deviceId;

  private String deviceExtension;

  private int userId;

  private String[] extensions;

  private int callVolume;

  private int ringerVolume;

  private String clipInternal;

  private String clipExternal;

  private boolean clirInternal;

  private boolean clirExternal;
  
  private boolean clirLocked;
}
