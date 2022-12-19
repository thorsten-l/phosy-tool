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
package l9g.app.phosy.ldap;

import com.unboundid.ldap.sdk.Entry;
import java.io.Serializable;
import l9g.app.phosy.config.LdapMapConfig;
import l9g.app.phosy.ucware.UcwareAttributeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LdapUtil implements Serializable
{
  private static final long serialVersionUID = -5657167141755154528L;

  public String value(UcwareAttributeType type)
  {
    String value = "";

    if (type != null && config != null && config.getLdapMap() != null)
    {
      String attributeName = config.getLdapMap().get(type);

      if (attributeName != null)
      {
        String v = entry.getAttributeValue(attributeName);
        if (v != null)
        {
          value = v;
        }
      }
    }

    return value;
  }

  private LdapMapConfig config;

  private Entry entry;
}
