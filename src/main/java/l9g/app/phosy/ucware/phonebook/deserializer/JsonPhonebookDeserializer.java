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
import l9g.app.phosy.ucware.phonebook.model.UcwareContactGroup;
import l9g.app.phosy.ucware.phonebook.model.UcwarePhonebook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class JsonPhonebookDeserializer extends StdDeserializer<UcwarePhonebook>
{
  private final static Logger LOGGER = LoggerFactory.getLogger(
    JsonPhonebookDeserializer.class.getName());

  private static final long serialVersionUID = -4145405889538342280L;

  public JsonPhonebookDeserializer()
  {
    this(null);
  }

  public JsonPhonebookDeserializer(Class<?> vc)
  {
    super(vc);
  }

  @Override
  public UcwarePhonebook deserialize(JsonParser jp,
    DeserializationContext ctxt) throws IOException, JsonProcessingException
  {
    // LOGGER.debug("deserialize UcwareContactGroup");
    ObjectMapper objectMapper = (ObjectMapper) jp.getCodec();
    UcwarePhonebook phoneBook = new UcwarePhonebook();

    JsonNode node = objectMapper.readTree(jp);
    phoneBook.setUuid(
      node.get("uuid") != null ? node.get("uuid").asText() : "");
    phoneBook.setName(
      node.get("name") != null ? node.get("name").asText() : "");
    phoneBook.setType(
      node.get("type") != null ? node.get("type").asText() : "");
    phoneBook.setWriteable(
      node.get("writeable") != null ? node.get("writeable").asBoolean() : true);

    JsonNode groupsNode = node.get("groups");

    if (groupsNode != null)
    {
      if (groupsNode.getNodeType() == JsonNodeType.OBJECT)
      {
        LinkedHashMap map = objectMapper.treeToValue(
          groupsNode, LinkedHashMap.class);

        for (Object key : map.keySet())
        {
          phoneBook.getGroups().add(objectMapper.convertValue(
            map.get(key), UcwareContactGroup.class));
        }
      }
      else if (groupsNode.getNodeType() == JsonNodeType.ARRAY)
      {
        List list = objectMapper.treeToValue(groupsNode, List.class);

        if (list != null)
        {
          for (Object object : list)
          {
            phoneBook.getGroups().add(objectMapper.convertValue(
              object, UcwareContactGroup.class));
          }
        }
        else
        {
          LOGGER.debug("ARRAY is null");
        }
      }
    }

    return phoneBook;
  }
}
