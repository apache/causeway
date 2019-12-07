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
package org.apache.isis.runtime.services.i18n.po;

import lombok.extern.log4j.Log4j2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.i18n.LocaleProvider;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.i18n.TranslationsResolver;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.config.IsisConfiguration;

@Service
@Named("isisRuntimeServices.TranslationServicePo")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Po")
@Log4j2
public class TranslationServicePo implements TranslationService {

    private PoAbstract po;

    /**
     * Defaults to writer mode because the service won't have been init'd while the metamodel is bring instantiated,
     * and we want to ensure that we capture all requests for translation.
     */
    public TranslationServicePo() {
        po = new PoWriter(this);
    }

    // -- init, shutdown

    @PostConstruct
    public void init() {

        final Mode translationMode = configuration.getServices().getTranslation().getPo().getMode();

        final boolean translationDisabled = (Mode.DISABLED == translationMode || Mode.DISABLE == translationMode);
        if(translationDisabled) {
            // switch to disabled mode
            po = new PoDisabled(this);
            return;
        }

        if(getLocaleProvider() == null || getTranslationsResolver() == null) {
            // remain in write mode
            return;
        }

        final boolean forceRead = (Mode.READ == translationMode);

        if(!forceRead) {
            // remain in write mode
            return;
        }

        // switch to read mode
        final PoReader poReader = new PoReader(this);
        poReader.init();
        po = poReader;
    }

    protected boolean isPrototypeOrTest() {
        return systemEnvironment.isPrototyping() || systemEnvironment.isUnitTesting();
    }

    @PreDestroy
    public void shutdown() {
        po.logTranslations();
    }

    @Override
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
    public String toPot() {
        if (!getMode().isWrite()) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        ((PoWriter)po).toPot(buf);
        return buf.toString();
    }


    /**
     * Not API
     */
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

    // -- DEPENDENCIES

    @Inject private IsisSystemEnvironment systemEnvironment;
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private IsisConfiguration configuration;
    
    private _Lazy<Can<TranslationsResolver>> translationsResolvers = _Lazy.threadSafe(()->
    serviceRegistry.select(TranslationsResolver.class) );

    Can<TranslationsResolver> getTranslationsResolver() {
        return translationsResolvers.get();
    }

    private _Lazy<Can<LocaleProvider>> localeProviders = _Lazy.threadSafe(()->
    serviceRegistry.select(LocaleProvider.class) );

    Can<LocaleProvider> getLocaleProvider() {
        return localeProviders.get();
    }

    

}
