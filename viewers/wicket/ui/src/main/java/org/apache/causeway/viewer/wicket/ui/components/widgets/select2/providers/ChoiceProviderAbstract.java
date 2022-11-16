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
package org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.wicket.util.string.Strings;
import org.springframework.lang.Nullable;
import org.wicketstuff.select2.ChoiceProvider;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMementoForEmpty;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;

import lombok.val;

public abstract class ChoiceProviderAbstract
extends ChoiceProvider<ObjectMemento>
implements HasCommonContext {
    private static final long serialVersionUID = 1L;

    /**
     * Whether to not prepend <code>null</code> as choice candidate.
     */
    protected abstract boolean isRequired();

    /**
     * Get choice candidates with filtering (don't include <code>null</code>).
     */
    protected abstract Can<ObjectMemento> query(@Nullable String term);

    @Override
    public final String getDisplayValue(final ObjectMemento choiceMemento) {
        if (choiceMemento == null
                || choiceMemento instanceof ObjectMementoForEmpty) {
            return getPlaceholderRenderService().asText(PlaceholderLiteral.NULL_REPRESENTATION);
        }
        return translate(choiceMemento.getTitle());
    }

    @Override
    public final String getIdValue(final ObjectMemento choiceMemento) {
        if (choiceMemento == null) {
            return ObjectMemento.NULL_ID;
        }
        return ObjectMemento.enstringToUrlBase64(choiceMemento);
    }

    protected final @Nullable ObjectMemento mementoFromId(final @Nullable String id) {
        return ObjectMemento.destringFromUrlBase64(id);
    }

    @Override
    public final void query(
            final String term,
            final int page,
            final org.wicketstuff.select2.Response<ObjectMemento> response) {

        val mementosFiltered = query(term);

        if(isRequired()) {
            response.addAll(mementosFiltered.toList());
            return;
        }

        // else, if not mandatory, prepend null
        val mementosIncludingNull = mementosFiltered.toArrayList();
        mementosIncludingNull.add(0, null);

        response.addAll(mementosIncludingNull);
    }

    @Override
    public final Collection<ObjectMemento> toChoices(final Collection<String> ids) {
        return _NullSafe.stream(ids)
                .map(this::mementoFromIdWithNullHandling)
                .collect(Collectors.toList());
    }

    // -- UTILITY

    /**
     * Filters all choices against a term by using their
     * {@link ManagedObject#getTitle() title string}
     *
     * @param term The term entered by the user
     * @param choiceMementos The collections of choices to filter
     * @return A list of all matching choices
     */
    protected final Can<ObjectMemento> filter(
            final String term,
            final Can<ObjectMemento> choiceMementos) {

        if (Strings.isEmpty(term)) {
            return choiceMementos;
        }

        val translationContext = TranslationContext.empty();
        val translator = getTranslationService();
        val termLower = term.toLowerCase();

        return choiceMementos.filter((final ObjectMemento candidateMemento)->{
            val title = translator.translate(translationContext, candidateMemento.getTitle());
            return title.toLowerCase().contains(termLower);
        });

    }

    // -- HELPER

    private @Nullable ObjectMemento mementoFromIdWithNullHandling(final String id) {
        if(ObjectMemento.NULL_ID.equals(id)) {
            return null;
        }
        return mementoFromId(id);
    }

}
