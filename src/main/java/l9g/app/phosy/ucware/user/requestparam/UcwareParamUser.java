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
  public UcwareParamUser(Bindings bindings)
  {
    this.username = (String)bindings.get("username");
    this.firstname = (String)bindings.get("firstname");
    this.lastname = (String)bindings.get("lastname");
    this.email = (String)bindings.get("email");
    this.url = (String)bindings.get("url");
    this.externalId = (String)bindings.get("externalId");
    this.language = (String)bindings.get("language");
    this.authBackend = (String)bindings.get("authBackend");
    this.privacy = (Boolean)bindings.get("privacy");
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
