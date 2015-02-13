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
package org.apache.isis.core.metamodel.services.i18n;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import com.google.common.collect.TreeMultimap;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.TranslationService;

/**
 * Not annotated with &#64;DomainService, but is registered as a fallback by <tt>ServicesInstallerFallback</tt>.
 */
public class TranslationServiceLogging implements TranslationService {

    public static Logger LOG = LoggerFactory.getLogger(TranslationServiceLogging.class);

    private final TreeMultimap<String, String> messages = TreeMultimap.create();

    public TranslationServiceLogging() {

        LOG.info("");
        LOG.info("");
        LOG.info("");
        LOG.info("################################################################################");
        LOG.info("#");
        LOG.info("# " + LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        LOG.info("#");
        LOG.info("################################################################################");
        LOG.info("");
        LOG.info("");

    }

    @Override
    @Programmatic
    public String translate(final String context, final String originalText, final Locale targetLocale) {

        final NavigableSet<String> contexts = messages.get(originalText);
        final boolean added = contexts.add(context);
        if(added) {
            LOG.info(String.format("%s_%s: %s", targetLocale.getISO3Country(), targetLocale.getISO3Language(), originalText));
        }
        return originalText;
    }

    @Programmatic
    public Map<String, Collection<String>> messages() {
        return messages.asMap();
    }
}
