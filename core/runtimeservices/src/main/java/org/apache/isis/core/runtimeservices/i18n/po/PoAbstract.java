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
package org.apache.isis.core.runtimeservices.i18n.po;

import org.apache.isis.applib.services.i18n.Mode;
import org.apache.isis.applib.services.i18n.TranslationContext;

abstract class PoAbstract {

    protected final TranslationServicePo translationServicePo;

    private final Mode mode;

    PoAbstract(final TranslationServicePo translationServicePo, final Mode mode) {
        this.translationServicePo = translationServicePo;
        this.mode = mode;
    }

    abstract String translate(final TranslationContext context, final String msgId);
    abstract String translate(final TranslationContext context, final String msgId, final String msgIdPlural, int num);

    Mode getMode() {
        return mode;
    }

    void logTranslations() {
        // default: do nothing
    }

}
