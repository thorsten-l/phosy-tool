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

import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import l9g.app.phosy.BuildProperties;
import l9g.app.phosy.config.UcwareConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UcwareClientFactory
{
  private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(
    UcwareClientFactory.class.getName());

  private UcwareClientFactory()
  {
  }

  private static Client _getClient(UcwareConfig config)
  {
    Client client = null;

    try
    {
      if (config.getUcwareHost().isIgnoreCertificate())
      {
        LOGGER.warn("Ignoring Ucware host certificate");
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, new TrustManager[]
        {
          new InsecureTrustManager()
        }, new java.security.SecureRandom());

        client = ClientBuilder.newBuilder()
          .sslContext(sslcontext)
          .hostnameVerifier((s1, s2) -> true)
          .build();
      }
      else
      {
        client = ClientBuilder.newClient();
      }

      client = client.register(
        new BasicAuthenticator(config.getCredentials().getUid(),
          config.getCredentials().getPassword()));

      if ("development".equals(BuildProperties.getInstance().getProfile()))
      {
        client = client.register(new LoggingFeature(Logger.getLogger(
          UcwareClientFactory.class.getName()), Level.INFO, null, null));
      }
    }
    catch (Throwable t)
    {
      LOGGER.error("ERROR: can't create webclient: {}", t.getMessage());
      System.exit(-1);
    }

    return client;
  }

  public static UcwarePhonebookClient getPhonebookClient(UcwareConfig config)
  {
    WebTarget target = _getClient(config)
      .target(config.getUcwareHost().getApiUrl());
    return new UcwarePhonebookClient(target);
  }

  public static UcwareUserClient getUserClient(UcwareConfig config)
  {
    WebTarget target = _getClient(config)
      .target(config.getUcwareHost().getApiUrl());
    return new UcwareUserClient(target);

  }

  public static UcwareGroupClient getGroupClient(UcwareConfig config)
  {
    WebTarget target = _getClient(config)
      .target(config.getUcwareHost().getApiUrl());
    return new UcwareGroupClient(target);
  }

  public static UcwareSlotClient getSlotClient(UcwareConfig config)
  {
    WebTarget target = _getClient(config)
      .target(config.getUcwareHost().getApiUrl());
    return new UcwareSlotClient(target);
  }

  public static UcwareDeviceClient getDeviceClient(UcwareConfig config)
  {
    WebTarget target = _getClient(config)
      .target(config.getUcwareHost().getApiUrl());
    return new UcwareDeviceClient(target);
  }
}

class InsecureTrustManager implements X509TrustManager
{
  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType)
    throws java.security.cert.CertificateException
  {
    //
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType)
    throws java.security.cert.CertificateException
  {
    //
  }

  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    return new X509Certificate[0];
  }
}
