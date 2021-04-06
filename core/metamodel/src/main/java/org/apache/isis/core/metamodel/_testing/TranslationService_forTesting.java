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
package org.apache.isis.core.metamodel._testing;

import org.apache.isis.applib.services.i18n.Mode;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;

public class TranslationService_forTesting implements TranslationService {

    @Override
    public String translate(TranslationContext context, String text) {
        return text;
    }

    @Override
    public String translate(TranslationContext context, String singularText, String pluralText, int num) {
        return num==1 ? singularText : pluralText;
    }

    @Override
    public Mode getMode() {
        return Mode.READ;
    }

}
