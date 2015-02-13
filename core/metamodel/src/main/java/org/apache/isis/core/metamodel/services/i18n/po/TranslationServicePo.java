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
package org.apache.isis.core.metamodel.services.i18n.po;

import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.TranslationService;

/**
 * Not annotated with &#64;DomainService, but is registered as a fallback by <tt>ServicesInstallerFallback</tt>.
 */
public class TranslationServicePo implements TranslationService {

    public static Logger LOG = LoggerFactory.getLogger(TranslationServicePo.class);

    private boolean prototype;

    private PoAbstract po;

    public TranslationServicePo() {
        po = new PotWriter(this);
    }

    //region > init, shutdown

    @Programmatic
    @PostConstruct
    public void init(final Map<String,String> config) {
        final String deploymentType = config.get("isis.deploymentType");
        prototype = deploymentType.toLowerCase().contains("prototype");

        if (!prototype) {
            po = new PoReader(this);
        }
        po.init(config);
    }

    @Programmatic
    @PreDestroy
    public void shutdown() {
        po.shutdown();
    }
    //endregion

    boolean isPrototype() {
        return prototype;
    }


    @Override
    @Programmatic
    public String translate(final String context, final String originalText, final Locale targetLocale) {
        return po.translate(context, originalText, targetLocale);
    }

    /**
     * Not API
     */
    public String toPo() {
        if (!prototype) {
            throw new IllegalStateException("Not in prototype mode");
        }
        return  ((PotWriter)po).toPo();
    }

}
