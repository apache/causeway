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
package org.apache.isis.core.runtime.services.i18n.po;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.LocaleProvider;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.i18n.TranslationsResolver;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class TranslationServicePo implements TranslationService {

    public static Logger LOG = LoggerFactory.getLogger(TranslationServicePo.class);

    public static final String KEY_PO_MODE = "isis.services.translation.po.mode";

    private PoAbstract po;

    /**
     * Defaults to writer mode because the service won't have been init'd while the metamodel is bring instantiated,
     * and we want to ensure that we capture all requests for translation.
     */
    public TranslationServicePo() {
        po = new PoWriter(this);
    }

    //region > init, shutdown

    @Programmatic
    @PostConstruct
    public void init(final Map<String,String> config) {

        if(getLocaleProvider() == null || getTranslationsResolver() == null) {
            // remain in write mode
            return;
        }

        final boolean prototypeOrTest = isPrototypeOrTest();

        final String translationMode = config.get(KEY_PO_MODE);
        final boolean forceRead =
                translationMode != null &&
                        ("read".equalsIgnoreCase(translationMode) ||
                         "reader".equalsIgnoreCase(translationMode));

        if(prototypeOrTest && !forceRead) {
            // remain in write mode
            return;
        }

        // switch to read mode
        final PoReader poReader = new PoReader(this);
        poReader.init();
        po = poReader;
    }

    protected boolean isPrototypeOrTest() {
        final DeploymentType deploymentType = getDeploymentType();
        return !deploymentType.isProduction();
    }

    @Programmatic
    @PreDestroy
    public void shutdown() {
        po.shutdown();
    }
    //endregion


    @Override
    @Programmatic
    public String translate(final String context, final String text) {
        return po.translate(context, text);
    }

    @Override
    public String translate(final String context, final String singularText, final String pluralText, final int num) {
        return po.translate(context, singularText, pluralText, num);
    }

    @Override
    public Mode getMode() {
        return po.getMode();
    }

    /**
     * Not API
     */
    @Programmatic
    public String toPot() {
        if (!getMode().isWrite()) {
            return null;
        }
        return  ((PoWriter)po).toPot();
    }


    /**
     * Not API
     */
    @Programmatic
    void clearCache() {
        if (!getMode().isRead()) {
            return;
        }
        ((PoReader)po).clearCache();
    }

    private PoReader previousPoReader;
    private PoWriter previousPoWriter;

    /**
     * Not API
     */
    @Programmatic
    public void toggleMode() {
        if(getMode().isRead()) {
            previousPoReader = (PoReader) po;
            if (previousPoWriter != null) {
                po = previousPoWriter;
            } else {
                po = new PoWriter(this);
            }
        } else {
            previousPoWriter = (PoWriter)po;
            if(previousPoReader != null) {
                previousPoReader.clearCache();
                po = previousPoReader;
            } else {
                final PoReader poReader = new PoReader(this);
                poReader.init();
                po = poReader;
            }
        }
    }

    // //////////////////////////////////////

    DeploymentType getDeploymentType() {
        return IsisContext.getDeploymentType();
    }


    @Inject
    private
    TranslationsResolver translationsResolver;

    @Programmatic
    TranslationsResolver getTranslationsResolver() {
        return translationsResolver;
    }

    @Inject
    private
    LocaleProvider localeProvider;

    @Programmatic
    LocaleProvider getLocaleProvider() {
        return localeProvider;
    }

}
