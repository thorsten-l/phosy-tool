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
package l9g.app.phosy.ucware.phonebook.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.unboundid.util.NotNull;
import l9g.app.phosy.ucware.phonebook.model.UcwareAttributeType;
import l9g.app.phosy.ucware.phonebook.deserializer.JsonAttributeTypeDeserializer;
import l9g.app.phosy.ucware.phonebook.serializer.JsonAttributeTypeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 *
 */
@Getter
@ToString
@AllArgsConstructor
public class UcwareParamAttribute
{
  private final String name;

  private final String value;

  @NotNull
  @JsonSerialize(using = JsonAttributeTypeSerializer.class)
  @JsonDeserialize(using = JsonAttributeTypeDeserializer.class)
  private final UcwareAttributeType type;
}
