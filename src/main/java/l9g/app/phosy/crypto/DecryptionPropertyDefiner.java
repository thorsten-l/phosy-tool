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
package l9g.app.phosy.crypto;

import ch.qos.logback.core.PropertyDefinerBase;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class DecryptionPropertyDefiner extends PropertyDefinerBase
{
  private final AES256 CIPHER = new AES256(AppSecretKey.getSecret());

  /*
   * Return decrypted property value.
   *
   * @return decrypted property value
   */
  @Override
  public String getPropertyValue()
  {
    return CIPHER.decrypt(encryptedValue);
  }

  /**
   * Sets encrypted property value.
   *
   *
   * @param encryptedValue encrypted property
   */
  public void setEncryptedValue(String encryptedValue)
  {
    this.encryptedValue = encryptedValue;
  }

  //~--- fields ---------------------------------------------------------------
  /**
   * encrypted property
   */
  private String encryptedValue;

}
