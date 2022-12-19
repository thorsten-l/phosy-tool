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
package l9g.app.phosy;

import l9g.app.phosy.ucware.phonebook.PhonebookMain;
import jakarta.xml.bind.JAXB;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import l9g.app.phosy.config.Configuration;
import l9g.app.phosy.config.LdapUcwareType;
import l9g.app.phosy.crypto.AES256;
import l9g.app.phosy.crypto.AppSecretKey;
import l9g.app.phosy.crypto.PasswordGenerator;
import l9g.app.phosy.ucware.user.UserMain;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class App
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(App.class.getName());

  private static final String CONFIGURATION = "config"
    + File.separator + "config.xml";

  @Getter
  private final static Options OPTIONS = new Options();

  public static void buildInfo(PrintStream out)
  {
    BuildProperties build = BuildProperties.getInstance();
    out.println("Project Name    : " + build.getProjectName());
    out.println("Project Version : " + build.getProjectVersion());
    out.println("Build Timestamp : " + build.getTimestamp());
    out.flush();
  }

  public static void readConfiguration()
  {
    LOGGER.debug("reading configuration file config.xml");

    try
    {
      Configuration c = null;
      File configFile = new File(CONFIGURATION);

      LOGGER.debug("Config file: {}", configFile.getAbsolutePath());

      if (configFile.exists() && configFile.canRead())
      {
        c = JAXB.unmarshal(new FileReader(configFile), Configuration.class);

        LOGGER.trace("config={}", c);

        if (c != null)
        {
          LOGGER.debug("setting config");
          config = c;
          
          config.getUserConfig().setLdapMap(new HashMap<>());
          for (LdapUcwareType type : config.getUserConfig().getMapEntry())
          {
            config.getUserConfig().getLdapMap().put(
              type.getType(), type.getName());
          }
          
          config.getPhonebookConfig().setLdapMap(new HashMap<>());
          for (LdapUcwareType type : config.getPhonebookConfig().getMapEntry())
          {
            config.getPhonebookConfig().getLdapMap().put(
              type.getType(), type.getName());
          }
          
        }
      }
      else
      {
        LOGGER.error("Can NOT read config file");
      }

      if (config == null)
      {
        LOGGER.error("config file NOT found");
        System.exit(0);
      }
    }
    catch (Exception e)
    {
      LOGGER.error("Configuratione file config.xml not found ", e);
      System.exit(0);
    }
  }

  public synchronized static Configuration getConfig()
  {
    if (config == null)
    {
      readConfiguration();
    }
    return config;
  }

  public static void main(String[] args) throws Throwable
  {
    CmdLineParser parser = new CmdLineParser(OPTIONS);
    getConfig();

    try
    {
      parser.parseArgument(args);
    }
    catch (CmdLineException ex)
    {
      LOGGER.error("Command line error\n");
      parser.printUsage(System.out);
      System.exit(-1);
    }

    if (OPTIONS.getGeneratePasswordLength() > 0)
    {
      AES256 cipher = new AES256(AppSecretKey.getSecret());
      String password = PasswordGenerator.generate(OPTIONS.
        getGeneratePasswordLength());
      System.out.println("\npassword:  '" + password + "'");
      System.out.println("encrypted: '" + cipher.encrypt(password) + "'\n");
      System.exit(0);
    }

    if (OPTIONS.getPassword() != null)
    {
      AES256 cipher = new AES256(AppSecretKey.getSecret());
      System.out.println("\npassword:  '" + OPTIONS.getPassword() + "'");
      System.out.println("encrypted: '" + cipher.encrypt(OPTIONS.getPassword())
        + "'\n");
      System.exit(0);
    }

    if (OPTIONS.isDisplayVersion())
    {
      buildInfo(System.out);
      System.exit(0);
    }

    if (OPTIONS.isDisplayHelp())
    {
      System.out.println("\nUsage: phosy-tool [options]\n");
      parser.printUsage(System.out);
      System.exit(0);
    }

    syncDone = false;

    if (config.getUserConfig().isEnabled())
    {
      LOGGER.debug( "User main enabled");
      UserMain.getInstance().run(OPTIONS);
    }

    if (config.getPhonebookConfig().isEnabled())
    {
      LOGGER.debug( "Phonebook main enabled");
      PhonebookMain.getInstance().run(OPTIONS);
    }

    if (syncDone)
    {
      TimestampUtil.writeCurrentTimestamp();
    }

    System.exit(0);
  }

  @Setter
  private static boolean syncDone;

  private static Configuration config;
}
