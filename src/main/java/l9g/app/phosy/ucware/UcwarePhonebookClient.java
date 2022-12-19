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

import l9g.app.phosy.ucware.common.response.UcwareBooleanResponse;
import l9g.app.phosy.ucware.phonebook.requestparam.UcwareParamContact;
import l9g.app.phosy.ucware.phonebook.requestparam.UcwareParamAttribute;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.WebTarget;
import l9g.app.phosy.ucware.phonebook.model.UcwareContactAttribute;
import l9g.app.phosy.ucware.phonebook.response.UcwareAttributeResponse;
import l9g.app.phosy.ucware.phonebook.model.UcwareContact;
import l9g.app.phosy.ucware.phonebook.model.UcwareContactGroup;
import l9g.app.phosy.ucware.phonebook.response.UcwareContactResponse;
import l9g.app.phosy.ucware.phonebook.model.UcwarePhonebook;
import l9g.app.phosy.ucware.phonebook.requestparam.UcwareParamPhonebook;
import l9g.app.phosy.ucware.phonebook.response.UcwareAllPhonebooksResponse;
import l9g.app.phosy.ucware.phonebook.response.UcwareContactGroupResponse;
import l9g.app.phosy.ucware.phonebook.response.UcwarePhonebookResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UcwarePhonebookClient extends UcwareClient
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(UcwarePhonebookClient.class.getName());

  public UcwarePhonebookClient(WebTarget target)
  {
    super(target.path("/user/phonebook"));
  }

  public List<UcwarePhonebook> getAllUserPhonebooks()
  {
    LOGGER.debug("getAllUserPhonebooks()");

    UcwareAllPhonebooksResponse response
      = postRequest("getAll", UcwareAllPhonebooksResponse.class);

    final ArrayList<UcwarePhonebook> result = new ArrayList<>();

    response.getPhonebooks().forEach((k, v) ->
    {
      result.add(v);
    });

    return result;
  }

  public UcwarePhonebook newUserPhonebook(String name, boolean writable)
  {
    LOGGER.debug("newUserPhonebook()");

    UcwarePhonebookResponse response = postRequest(
      "newPhonebook",
      new Object[]
      {
        new UcwareParamPhonebook(null, name, writable)
      },
      UcwarePhonebookResponse.class);

    return response.getPhonebook();
  }

  public UcwarePhonebook updateUserPhonebook(String uuid, boolean writable)
  {
    LOGGER.debug("updateUserPhonebook({},{})", uuid, writable);

    UcwarePhonebookResponse response = postRequest(
      "updatePhonebook",
      new Object[]
      {
        new UcwareParamPhonebook(uuid, null, writable)
      },
      UcwarePhonebookResponse.class);

    return response.getPhonebook();
  }

  public UcwareContactGroup addUserContactGroup(String phonebookUuid,
    String name)
  {
    LOGGER.debug("addUserContactGroup");

    UcwareContactGroupResponse response = postRequest(
      "addContactGroup",
      new String[]
      {
        phonebookUuid, name
      },
      UcwareContactGroupResponse.class);

    return response.getContactGroup();
  }

  public boolean deleteUserPhonebook(String uuid)
  {
    LOGGER.debug("deleteUserPhonebook()");

    UcwareBooleanResponse response = postRequest(
      "deletePhonebook",
      new String[]
      {
        uuid
      },
      UcwareBooleanResponse.class);

    return response.isResult();
  }

  public boolean deleteUserContact(String uuid)
  {
    LOGGER.debug("deleteUserContact()");

    UcwareBooleanResponse response = postRequest(
      "deleteContact",
      new String[]
      {
        uuid
      },
      UcwareBooleanResponse.class);

    return response.isResult();
  }

  public UcwareContact addUserContact(String groupUuid,
    UcwareParamContact contact)
  {
    LOGGER.debug("addUserContact()");

    UcwareContactResponse response = postRequest(
      "addContact",
      new Object[]
      {
        groupUuid, contact
      },
      UcwareContactResponse.class);

    return response.getContact();
  }

  public UcwareContactAttribute addUserContactAttribute(String contactUuid,
    UcwareParamAttribute attribute)
  {
    LOGGER.debug("addUserContactAttribute()");

    UcwareAttributeResponse response = postRequest(
      "addAttribute",
      new Object[]
      {
        contactUuid, attribute
      },
      UcwareAttributeResponse.class);

    return response.getAttribute();
  }

  public UcwarePhonebook getUserPhonebookByName(String name)
  {
    UcwarePhonebook phonebook = null;

    List<UcwarePhonebook> phonebooks = getAllUserPhonebooks();

    for (UcwarePhonebook p : phonebooks)
    {
      if (p.getName().equals(name))
      {
        phonebook = p;
        break;
      }
    }

    return phonebook;
  }

  public UcwarePhonebook getUserPhonebookByUUID(String phonebookUuid)
  {
    LOGGER.debug("getUserPhonebookByUUID({})", phonebookUuid);

    UcwarePhonebookResponse response = postRequest(
      "getPhonebook",
      new Object[]
      {
        phonebookUuid
      },
      UcwarePhonebookResponse.class, true);

    return response.getPhonebook();
  }
}
