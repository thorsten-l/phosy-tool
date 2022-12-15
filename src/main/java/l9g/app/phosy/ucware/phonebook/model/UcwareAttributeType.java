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
package l9g.app.phosy.ucware.phonebook.model;

import lombok.Getter;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public enum UcwareAttributeType
{
  UCW_GENERIC_TEXT(0),
  UCW_PHONENUMBER(1),
  UCW_FAXIMILENUMBER(2),
  UCW_EMAIL(3),
  UCW_HYPERLINK(4),
  UCW_PHONENUMBER_HIGH_PRIORITY(5),
  UCW_COMPANY(6),
  UCW_DEPARTMENT(7),
  UCW_POSITION(8),

  LDAP_SN(1000),
  LDAP_GIVENNAME(1001),
  LDAP_EMPLOYEETYPE(1002),
  LDAP_LOCALITY(1003),
  LDAP_ACADEMIC_TITLE(1004),
  LDAP_CN(1005);

  @Getter
  private final int value;

  private UcwareAttributeType(int value)
  {
    this.value = value;
  }
}
