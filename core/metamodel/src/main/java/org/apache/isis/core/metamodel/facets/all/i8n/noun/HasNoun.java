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
package org.apache.isis.core.metamodel.facets.all.i8n.noun;

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.facets.all.i8n.HasMemoizableTranslation;

public interface HasNoun
extends HasMemoizableTranslation {

    /**
     * Originating text of preferred NounForm to be translated before use in the UI.
     */
    String preferredText();

    /**
     * Translated text of preferred NounForm to be used in the UI.
     */
    String preferredTranslated();

    /**
     * Originating text to be translated before use in the UI.
     */
    String text(NounForm nounForm);

    /**
     * Translated text to be used in the UI.
     */
    String translated(NounForm nounForm);

    ImmutableEnumSet<NounForm> getSupportedNounForms();

    @Nullable
    default String translatedElseNull(final NounForm nounForm) {
        return getSupportedNounForms().contains(nounForm)
                ? translated(nounForm)
                : null;
    }

    @Override
    default void memoizeTranslations() {
        getSupportedNounForms().forEach(this::translated);
    }

}
