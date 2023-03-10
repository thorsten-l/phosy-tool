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
package l9g.app.phosy.ucware.user;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jdk.jshell.JShell;
import l9g.app.phosy.config.LdapMapConfig;
import l9g.app.phosy.ldap.LdapUtil;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class UserScriptHandler
{
  private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(
    UserScriptHandler.class.getName());

  public final static JShell JSHELL = JShell.builder().build();

  public final static String CREATE_ATTRIBUTES_FILENAME
    = "jsh" + File.separator + "createUserAttributes.jsh";

  private final ScriptEngineManager manager = new ScriptEngineManager();

  private final ScriptEngine engine = manager.getEngineByName("jshell");

  private final Compilable compiler = (Compilable) engine;

  private CompiledScript compiledScript;

  private final static UserScriptHandler SINGLETON
    = new UserScriptHandler();

  private UserScriptHandler()
  {
    try
    {
      compiledScript = compiler.compile(new FileReader(
        CREATE_ATTRIBUTES_FILENAME));
    }
    catch (FileNotFoundException | ScriptException ex)
    {
      LOGGER.error("Script ERROR: ", ex);
      System.exit(0);
    }
  }

  private Bindings _run(LdapMapConfig mapConfig, Entry entry)
    throws IOException, ScriptException, LDIFException
  {
    LdapUtil ldapUtil = new LdapUtil(mapConfig, entry);
    Bindings bindings = engine.createBindings();
    bindings.put("entry", entry);
    bindings.put("ldapUtil", ldapUtil);
    Object result = compiledScript.eval(bindings);
    LOGGER.debug("script result = {}", result);
    return bindings;
  }

  public static Bindings run(LdapMapConfig mapConfig, Entry entry)
    throws IOException, ScriptException, LDIFException
  {
    return SINGLETON._run(mapConfig, entry);
  }
}
