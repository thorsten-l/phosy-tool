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

import l9g.app.phosy.ucware.phonebook.response.UcwareDeviceResponse;
import l9g.app.phosy.ucware.phonebook.response.UcwareBooleanResponse;
import l9g.app.phosy.ucware.phonebook.request.UcwareParamContact;
import l9g.app.phosy.ucware.phonebook.request.UcwareParamAttribute;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import l9g.app.phosy.ucware.phonebook.model.UcwareAttribute;
import l9g.app.phosy.ucware.phonebook.response.UcwareAttributeResponse;
import l9g.app.phosy.ucware.phonebook.model.UcwareContact;
import l9g.app.phosy.ucware.phonebook.model.UcwareContactGroup;
import l9g.app.phosy.ucware.phonebook.response.UcwareContactResponse;
import l9g.app.phosy.ucware.phonebook.model.UcwarePhonebook;
import l9g.app.phosy.ucware.phonebook.request.UcwareParamPhonebook;
import l9g.app.phosy.ucware.phonebook.response.UcwareAllPhonebooksResponse;
import l9g.app.phosy.ucware.phonebook.response.UcwareContactGroupResponse;
import l9g.app.phosy.ucware.phonebook.response.UcwarePhonebookResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UcwareClient
{
  private final static Logger LOGGER = LoggerFactory.getLogger(
    UcwareClient.class.
      getName());

  private final WebTarget target;

  public UcwareClient(WebTarget target)
  {
    this.target = target;
  }

  public UcwareDevice getDevice(String mac)
  {
    LOGGER.debug("getDevice({})", mac);
    UcwareRequest request = new UcwareRequest("getDevice", new String[]
    {
      mac
    });
    UcwareDeviceResponse response = target.path("/admin/device").request(
      MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON),
        UcwareDeviceResponse.class);
    return response.getDevice();
  }

  public String getAllSlots()
  {
    LOGGER.debug("getAllSlots()");
    UcwareRequest request
      = new UcwareRequest("getAll");
    String response = target.path("/admin/slot").request(
      MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON), String.class);
    return null;
  }

  public List<UcwarePhonebook> getAllPhonebooks()
  {
    LOGGER.debug("getAllPhonebooks()");
    UcwareRequest request
      = new UcwareRequest("getAll");

    UcwareAllPhonebooksResponse response = target.path("/admin/phonebook").
      request(
        MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON),
        UcwareAllPhonebooksResponse.class);

    final ArrayList<UcwarePhonebook> result = new ArrayList<>();

    response.getPhonebooks().forEach((k, v) ->
    {
      result.add(v);
    });

    return result;
  }

  public List<UcwarePhonebook> getAllUserPhonebooks()
  {
    LOGGER.debug("getAllUserPhonebooks()");
    UcwareRequest request
      = new UcwareRequest("getAll");

    UcwareAllPhonebooksResponse response = target.path("/user/phonebook").
      request(
        MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON),
        UcwareAllPhonebooksResponse.class);

    final ArrayList<UcwarePhonebook> result = new ArrayList<>();

    if (response != null)
    {
      if (response.getError() == null)
      {
        response.getPhonebooks().forEach((k, v) ->
        {
          result.add(v);
        });
      }
      else
      {
        LOGGER.error( "Error: {}", response.getError());
        System.exit(0);
      }
    }

    return result;
  }

  public UcwarePhonebook newUserPhonebook(String name, boolean writable)
  {
    LOGGER.debug("newUserPhonebook()");

    UcwareRequest request
      = new UcwareRequest("newPhonebook",
        new Object[]
        {
          new UcwareParamPhonebook(null, name, writable)
        });

    UcwarePhonebookResponse response = target.path("/user/phonebook").
      request(
        MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON),
        UcwarePhonebookResponse.class);

    return response.getPhonebook();
  }

  public UcwareContactGroup addUserContactGroup(String phonebookUuid,
    String name)
  {
    LOGGER.debug("addUserContactGroup");

    UcwareRequest request
      = new UcwareRequest("addContactGroup",
        new String[]
        {
          phonebookUuid, name
        });

    UcwareContactGroupResponse response = target.path("/user/phonebook").
      request(
        MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON),
        UcwareContactGroupResponse.class);

    return response.getContactGroup();
  }

  public boolean deleteUserPhonebook(String uuid)
  {
    LOGGER.debug("deleteUserPhonebook()");

    UcwareRequest request
      = new UcwareRequest("deletePhonebook",
        new String[]
        {
          uuid
        });

    UcwareBooleanResponse response = target.path("/user/phonebook").request(
      MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON),
        UcwareBooleanResponse.class);

    return response != null ? response.isResult() : false;
  }

  public boolean deleteUserContact(String uuid)
  {
    LOGGER.debug("deleteUserContact()");

    UcwareRequest request
      = new UcwareRequest("deleteContact",
        new String[]
        {
          uuid
        });

    UcwareBooleanResponse response = target.path("/user/phonebook").request(
      MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON),
        UcwareBooleanResponse.class);

    return response != null ? response.isResult() : false;
  }

  public UcwareContact addUserContact(String groupUuid,
    UcwareParamContact contact)
  {
    LOGGER.debug("addUserContact()");

    UcwareRequest request
      = new UcwareRequest("addContact",
        new Object[]
        {
          groupUuid, contact
        });

    UcwareContactResponse response = target.path("/user/phonebook").
      request(
        MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON),
        UcwareContactResponse.class);

    LOGGER.debug("reponse={}", response);

    return response.getContact();
  }

  public UcwareAttribute addUserContactAttribute(String contactUuid,
    UcwareParamAttribute attribute)
  {
    LOGGER.debug("addUserContactAttribute()");

    UcwareRequest request
      = new UcwareRequest("addAttribute",
        new Object[]
        {
          contactUuid, attribute
        });

    UcwareAttributeResponse response = target.path("/user/phonebook").request(
      MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON),
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
    LOGGER.debug("getUserPhonebookByUUID()");

    UcwareRequest request
      = new UcwareRequest("getPhonebook",
        new Object[]
        {
          phonebookUuid
        });

    UcwarePhonebookResponse response = target.path("/user/phonebook").request(
      MediaType.APPLICATION_JSON).
      post(Entity.entity(request, MediaType.APPLICATION_JSON),
        UcwarePhonebookResponse.class);

    return response.getPhonebook();
  }

}
