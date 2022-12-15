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
import l9g.app.phosy.ucware.phonebook.model.UcwareAttribute;
import l9g.app.phosy.ucware.phonebook.model.UcwareContact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class JsonContactDeserializer extends StdDeserializer<UcwareContact>
{
  private final static Logger LOGGER = LoggerFactory.getLogger(
    JsonContactDeserializer.class.getName());

  private static final long serialVersionUID = 4154869439509903346L;

  public JsonContactDeserializer()
  {
    this(null);
  }

  public JsonContactDeserializer(Class<?> vc)
  {
    super(vc);
  }

  @Override
  public UcwareContact deserialize(JsonParser jp,
    DeserializationContext ctxt) throws IOException, JsonProcessingException
  {
    // LOGGER.debug("deserialize UcwareContactGroup");
    ObjectMapper objectMapper = (ObjectMapper) jp.getCodec();
    UcwareContact contact = new UcwareContact();

    JsonNode node = objectMapper.readTree(jp);
    contact.setUuid(
      node.get("uuid") != null ? node.get("uuid").asText() : "" );
    contact.setFirstname(
      node.get("firstname") != null ? node.get("firstname").asText() : "");
    contact.setLastname(
      node.get("lastname") != null ? node.get("lastname").asText() : "");
    contact.setPrefix(
      node.get("prefix") != null ? node.get("prefix").asText() : "");
    contact.setSuffix(
      node.get("suffix") != null ? node.get("suffix").asText() : "");
    contact.setSyncId(
      node.get("syncId") != null ? node.get("syncId").asText() : "");

    JsonNode attributesNode = node.get("attributes");

    if (attributesNode != null)
    {
      if (attributesNode.getNodeType() == JsonNodeType.OBJECT)
      {
        LinkedHashMap map = objectMapper.treeToValue(
          attributesNode, LinkedHashMap.class);

        for (Object key : map.keySet())
        {
          contact.getAttributes().add(objectMapper.convertValue(
            map.get(key), UcwareAttribute.class));
        }
      }
      else if (attributesNode.getNodeType() == JsonNodeType.ARRAY)
      {
        List list = objectMapper.treeToValue(attributesNode, List.class);

        if (list != null)
        {
          for (Object object : list)
          {
            contact.getAttributes().add(objectMapper.convertValue(
              object, UcwareAttribute.class));
          }
        }
        else
        {
          LOGGER.debug("ARRAY is null");
        }
      }
    }

    return contact;
  }
}
