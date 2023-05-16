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
package l9g.app.phosy.ucware.user.requestparam;

import javax.script.Bindings;
import l9g.app.phosy.ucware.user.model.UcwareUser;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 *
 */
@Getter
@ToString
public class UcwareParamUser
{
  public UcwareParamUser(Bindings bindings,
    String defaultAuthBackend, String defaultLanguage)
  {
    this.username = getStringValue(bindings, "username", "");
    this.firstname = getStringValue(bindings, "firstname", "");
    this.lastname = getStringValue(bindings, "lastname", "");
    this.email = getStringValue(bindings, "email", "");
    this.url = getStringValue(bindings, "url", "");
    this.externalId = getStringValue(bindings, "externalId", "");
    this.language = getStringValue(bindings, "language", defaultLanguage);
    this.authBackend = getStringValue(bindings, "authBackend",
      defaultAuthBackend);
    this.privacy = (bindings.get("privacy") != null) ? (Boolean) bindings.get(
      "privacy") : false;
  }

  public UcwareParamUser(UcwareUser ucwareUser, String externalId)
  {
    this.username = ucwareUser.getUsername();
    this.firstname = ucwareUser.getFirstname();
    this.lastname = ucwareUser.getLastname();
    this.email = ucwareUser.getEmail();
    this.url = ucwareUser.getUrl();
    this.externalId = externalId;
    this.language = ucwareUser.getLanguage();
    this.authBackend = ucwareUser.getAuthBackend();
    this.privacy = ucwareUser.isPrivacy();
  }

  private final String getStringValue(Bindings bindings, String name,
    String defaultValue)
  {
    return (bindings.get(name) != null) ? (String) bindings.get(name) : defaultValue;
  }

  private final String username;

  private final String firstname;

  private final String lastname;

  private final String email;

  private final String url;

  private final String externalId;

  private final String language;

  private final String authBackend;

  private final boolean privacy;
}
