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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class AppSecretKey
{
  private final static Logger LOGGER = LoggerFactory.getLogger(
    AppSecretKey.class.getName());

  private final static String SECRET_FILE = "config" 
    + File.separator + "secret.bin";

  private final static AppSecretKey SINGLETON = new AppSecretKey();

  private AppSecretKey()
  {
  }

  private void initialize()
  {
    try
    {
      File secretFile = new File(SECRET_FILE);

      if (secretFile.exists())
      {
        secretKey = new byte[48];
        LOGGER.debug("Loading secret file");
        try (FileInputStream input = new FileInputStream(secretFile))
        {
          input.read(secretKey);
        }
      }
      else
      {
        LOGGER.info("Writing secret file");

        try (FileOutputStream output = new FileOutputStream(secretFile))
        {
          AES256 aes256 = new AES256();
          secretKey = aes256.getSecret();
          output.write(secretKey);
        }

        // file permissions - r-- --- ---
        secretFile.setExecutable(false, false);
        secretFile.setWritable(false, false);
        secretFile.setReadable(false, false);
        secretFile.setReadable(true, true);
      }
    }
    catch (IOException | NoSuchAlgorithmException e)
    {
      LOGGER.error("ERROR: secret file ", e);
      System.exit(-1);
    }
  }

  private synchronized byte[] _getSecret()
  {
    if (secretKey == null)
    {
      initialize();
    }

    return secretKey;
  }

  public static byte[] getSecret()
  {
    return SINGLETON._getSecret();
  }

  private byte[] secretKey = null;
}
