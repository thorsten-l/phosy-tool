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
package l9g.app.phosy.ucware;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Getter
@ToString
@JsonPropertyOrder({ "jsonrpc", "method", "params", "id" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UcwareRequest
{
  public UcwareRequest(String method)
  {
    this.method = method;
  }

  public UcwareRequest(String method, Object[] params)
  {
    this(method);
    this.params = params;
  }

  private String jsonrpc = "2.0";

  private String method;

  private Object[] params;

  private String id = "";
}
