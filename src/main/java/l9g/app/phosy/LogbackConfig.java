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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.boolex.OnMarkerEvaluator;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.spi.CyclicBufferTracker;
import l9g.app.phosy.config.Configuration;
import l9g.app.phosy.config.MailConfig;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Getter
public class LogbackConfig
{
  private final static org.slf4j.Logger LOGGER
    = LoggerFactory.getLogger(LogbackConfig.class.getName());

  private final static LogbackConfig SINGLETON = new LogbackConfig();

  private final static String MARKER_NAME = "SMTP_NOTIFICATION";

  private LogbackConfig()
  {
    initialized = false;
  }

  public synchronized LogbackConfig initialize(Options options,
    Configuration _config)
  {
    this.config = _config.getMailConfig();
    this.options = options;

    if (!initialized)
    {
      LOGGER.info("Initializizng LogbackConfig() debugLogging={}", options.
        isDebugLogging());

      loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

      rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
      rootLogger.detachAndStopAllAppenders();
      rootLogger.setAdditive(false);

      rootLogger.setLevel(options.isDebugLogging() ? Level.DEBUG : Level.INFO);

      PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
      layoutEncoder.setContext(loggerContext);
      layoutEncoder.setPattern(
        "%date{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger:%line - %boldYellow(%msg) %n");
      layoutEncoder.start();

      if (config.isEnabled())
      {
        smtpAppender = buildSmtpAppender("SMTP", layoutEncoder, null);
        smtpMarkerAppender = buildSmtpAppender(MARKER_NAME,
          layoutEncoder, MARKER_NAME);
      }

      notificationMarker = MarkerFactory.getMarker(MARKER_NAME);

      // --------------------------------------------------------------------
      consoleAppender = new ConsoleAppender();
      consoleAppender.setContext(loggerContext);
      consoleAppender.setName("CONSOLE");
      consoleAppender.setEncoder(layoutEncoder);
      consoleAppender.start();

      // --------------------------------------------------------------------
      rootLogger.addAppender(consoleAppender);

      if (config.isEnabled())
      {
        rootLogger.addAppender(smtpAppender);
        rootLogger.addAppender(smtpMarkerAppender);
      }

      initialized = true;
    }
    else
    {
      LOGGER.warn("Already initialized.");
    }

    return SINGLETON;
  }

  private final SMTPAppender buildSmtpAppender(String name,
    PatternLayoutEncoder layoutEncoder, String markerName)
  {
    SMTPAppender appender = new SMTPAppender();

    CyclicBufferTracker bufferTracker = new CyclicBufferTracker();
    bufferTracker.setBufferSize(1);

    appender.setContext(loggerContext);
    appender.setName(name);
    appender.setFrom(config.getFrom());

    for (String to : config.getTo())
    {
      appender.addTo(to);
    }

    appender.setSmtpHost(config.getSmtpHost());
    appender.setSmtpPort(config.getSmtpPort());
    appender.setSTARTTLS(config.isStartTLS());
    appender.setSubject(config.getSubject());
    appender.setUsername(config.getCredentials().getUid());
    appender.setPassword(config.getCredentials().getPassword());
    appender.setLayout(layoutEncoder.getLayout());
    appender.setAsynchronousSending(false);
    appender.setCyclicBufferTracker(bufferTracker);

    if (markerName != null)
    {
      OnMarkerEvaluator evaluator = new OnMarkerEvaluator();
      evaluator.addMarker(markerName);
      appender.setEvaluator(evaluator);
    }

    appender.start();

    return appender;
  }

  public static LogbackConfig getInstance()
  {
    return SINGLETON;
  }

  private Options options;

  private MailConfig config;

  private LoggerContext loggerContext;

  private Logger rootLogger;

  private SMTPAppender smtpAppender;

  private SMTPAppender smtpMarkerAppender;

  private Marker notificationMarker;

  private ConsoleAppender consoleAppender;

  private boolean initialized;
}
