/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.integtestsupport;

import java.io.PrintStream;

import com.google.common.base.Strings;

import org.apache.log4j.PropertyConfigurator;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import org.apache.isis.applib.Module;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.integtestsupport.logging.LogConfig;
import org.apache.isis.core.integtestsupport.logging.LogStream;

public abstract class IntegrationBootstrapAbstract extends IntegrationAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationBootstrapAbstract.class);

    private final LogConfig logConfig;
    protected static PrintStream logPrintStream(Level level) {
        return LogStream.logPrintStream(LOG, level);
    }

    private final static ThreadLocal<Boolean> setupLogging = new ThreadLocal<Boolean>() {{
        set(false);
    }};


    private final IsisSystemBootstrapper isisSystemBootstrapper;

    protected Long t0;

    protected IntegrationBootstrapAbstract(
            final Module module,
            final Class... additionalModuleClasses) {
        this(new LogConfig(Level.INFO), module, additionalModuleClasses);
    }

    protected IntegrationBootstrapAbstract(
            final LogConfig logConfig,
            final Module module,
            final Class... additionalModuleClasses) {

        this.logConfig = logConfig;

        final boolean firstTime = !setupLogging.get();
        if(firstTime) {
            PropertyConfigurator.configure(logConfig.getLoggingPropertyFile());
            System.setOut(logConfig.getFixtureTracing());
            setupLogging.set(true);
            t0 = System.currentTimeMillis();
        }

        final String moduleFqcn = System.getProperty("isis.integTest.module");

        final Module moduleToUse;
        final Class[] additionalModuleClassesToUse;
        if(!Strings.isNullOrEmpty(moduleFqcn)) {
            moduleToUse = InstanceUtil.createInstance(moduleFqcn, Module.class);
            additionalModuleClassesToUse = new Class<?>[] { };
        } else {
            moduleToUse = module;
            additionalModuleClassesToUse = additionalModuleClasses;
        }
        this.isisSystemBootstrapper =
                new IsisSystemBootstrapper(logConfig, moduleToUse, additionalModuleClassesToUse);
    }


    private LocalDate timeBeforeTest;

    protected void bootstrapAndSetupIfRequired() {

        System.setProperty("isis.integTest", "true");

        isisSystemBootstrapper.bootstrapIfRequired(t0);
        isisSystemBootstrapper.injectServicesInto(this);

        beginTransaction();

        isisSystemBootstrapper.setupModuleRefData();

        timeBeforeTest = Clock.getTimeAsLocalDate();
    }

    private void beginTransaction() {
        final IsisSystem isft = IsisSystem.get();
        isft.beginTran();
    }

    protected void tearDownAllModules() {

        final boolean testHealthy = transactionService != null;
        if(!testHealthy) {
            // avoid throwing an NPE here if something unexpected has occurred...
            return;
        }

        transactionService.nextTransaction();

        isisSystemBootstrapper.tearDownAllModules();

        // reinstate clock
        setFixtureClockDate(timeBeforeTest);
    }

    protected void log(final String message) {
        switch (logConfig.getTestLoggingLevel()) {
        case ERROR:
            LOG.error(message);
            break;
        case WARN:
            LOG.warn(message);
            break;
        case INFO:
            LOG.info(message);
            break;
        case DEBUG:
            LOG.debug(message);
            break;
        case TRACE:
            LOG.trace(message);
            break;
        }
    }
}