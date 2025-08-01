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
package org.apache.causeway.core.runtimeservices.i18n.po;

import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.i18n.LanguageProvider;
import org.apache.causeway.applib.services.i18n.Mode;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.i18n.TranslationsResolver;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.Getter;

/**
 * Implementation of {@link TranslationService} that uses <code>.po</code>po file format.
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".TranslationServicePo")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Po")
public class TranslationServicePo implements TranslationService {

    private PoAbstract po;
    private Runnable onShutdown;

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

        final Mode translationMode = configuration.core().runtimeServices().translation().po().mode();

        if(translationMode == Mode.DISABLED) {
            // switch to disabled mode
            po = new PoDisabled(this);
            return;
        }

        if(getTranslationsResolver() == null) {
            // remain in write mode
            return;
        }

        if(translationMode != Mode.READ) {
            // remain in write mode
            return;
        }

        // switch to read mode
        final PoReader poReader = new PoReader(this);
        poReader.init();
        po = poReader;

        if(!systemEnvironment.isUnitTesting()) {
            onShutdown = po::logTranslations;
        }
    }

    @PreDestroy
    public void shutdown() {
        if(onShutdown!=null) {
            onShutdown.run();
            onShutdown = null;
        }
    }

    @Override
    public String translate(final TranslationContext context, final String text) {
        return po.translate(context, text);
    }

    @Override
    public String translate(final TranslationContext context, final String singularText, final String pluralText, final int num) {
        return po.translate(context, singularText, pluralText, num);
    }

    @Override
    public Mode getMode() {
        return po.getMode();
    }

    /**
     * Not API
     */
    public Optional<String> toPot() {
        if (!getMode().isWrite()) {
            return Optional.empty();
        }
        var buf = new StringBuilder();
        ((PoWriter)po).toPot(buf);
        return Optional.of(buf.toString());
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

    @Inject private CausewaySystemEnvironment systemEnvironment;
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private CausewayConfiguration configuration;

    @Getter
    @Inject private LanguageProvider languageProvider;

    private _Lazy<Can<TranslationsResolver>> translationsResolvers = _Lazy.threadSafe(()->
    serviceRegistry.select(TranslationsResolver.class) );

    Can<TranslationsResolver> getTranslationsResolver() {
        return translationsResolvers.get();
    }

}
