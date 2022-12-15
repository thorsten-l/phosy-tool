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
package l9g.app.phosy.ucware.phonebook.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import l9g.app.phosy.ucware.phonebook.model.UcwareContact;
import l9g.app.phosy.ucware.phonebook.model.UcwareContactGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class JsonContactGroupDeserializer extends StdDeserializer<UcwareContactGroup>
{
  private final static Logger LOGGER = LoggerFactory.getLogger(
    JsonContactGroupDeserializer.class.getName());

  private static final long serialVersionUID = -2251098533213088481L;

  public JsonContactGroupDeserializer()
  {
    this(null);
  }

  public JsonContactGroupDeserializer(Class<?> vc)
  {
    super(vc);
  }

  @Override
  public UcwareContactGroup deserialize(JsonParser jp,
    DeserializationContext ctxt) throws IOException, JsonProcessingException
  {
    // LOGGER.debug("deserialize UcwareContactGroup");
    ObjectMapper objectMapper = (ObjectMapper) jp.getCodec();
    UcwareContactGroup contactGroup = new UcwareContactGroup();

    JsonNode node = objectMapper.readTree(jp);
    contactGroup.setUuid(
      node.get("uuid") != null ? node.get("uuid").asText() : "" );
    contactGroup.setName(
      node.get("name") != null ? node.get("name").asText() : "");

    JsonNode contactsNode = node.get("contacts");

    if (contactsNode != null)
    {
      if (contactsNode.getNodeType() == JsonNodeType.OBJECT)
      {
        LinkedHashMap map = objectMapper.treeToValue(
          contactsNode, LinkedHashMap.class);

        for (Object key : map.keySet())
        {
          contactGroup.getContacts().add(objectMapper.convertValue(
            map.get(key), UcwareContact.class));
        }
      }
      else if (contactsNode.getNodeType() == JsonNodeType.ARRAY)
      {
        List list = objectMapper.treeToValue(contactsNode, List.class);

        if (list != null)
        {
          for (Object object : list)
          {
            contactGroup.getContacts().add(objectMapper.convertValue(
              object, UcwareContact.class));
          }
        }
        else
        {
          LOGGER.debug("ARRAY is null");
        }
      }
    }

    return contactGroup;
  }
}
