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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import l9g.app.phosy.ucware.phonebook.model.UcwareAttributeType;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class JsonAttributeTypeDeserializer extends JsonDeserializer<UcwareAttributeType>
{
  @Override
  public UcwareAttributeType deserialize(JsonParser jsonParser,
    DeserializationContext deserializationContext) throws IOException
  {
    UcwareAttributeType type = null;
    int value = jsonParser.getNumberValue().intValue();

    for (UcwareAttributeType t : UcwareAttributeType.values())
    {
      if (value == t.getValue())
      {
        type = t;
        break;
      }
    }

    return type;
  }
}
