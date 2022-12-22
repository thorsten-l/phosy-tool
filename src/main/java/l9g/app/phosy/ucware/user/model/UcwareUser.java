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
package l9g.app.phosy.ucware.user.model;

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
public class UcwareUser
{
  private int id;

  private String username;

  private String firstname;

  private String lastname;

  private String email;

  private String url;

  private String language;

  private String authBackend;

  private int group_id;

  private boolean privacy;

  private String externalId;

  private String[] slots;

  private UcwareUserCallBarring[] callBarring;

  private String[] clipNumbersExternal;

  private String[] clipNumbersInternal;

  private int[] groups;

  private String[] extensions;

  private String[] pickupGroups;

  private int[] licenses;
}
