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
package org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.util.string.Strings;
import org.springframework.lang.Nullable;
import org.wicketstuff.select2.ChoiceProvider;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract.PlaceholderLiteral;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.config.IsisConfiguration.Viewer.Wicket;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMementoForEmpty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.Getter;
import lombok.val;

public abstract class ObjectAdapterMementoProviderAbstract
extends ChoiceProvider<ObjectMemento> {

    private static final long serialVersionUID = 1L;

    protected static final String NULL_PLACEHOLDER = "VGN6r6zKTiLhUsA0WkdQ17LvMU1IYdb0";

    @Getter private final ScalarModel scalarModel;
    private transient IsisAppCommonContext commonContext;

    public ObjectAdapterMementoProviderAbstract(final ScalarModel scalarModel) {
        this.scalarModel = scalarModel;
    }

    @Override
    public String getDisplayValue(final ObjectMemento choiceMemento) {
        if (choiceMemento == null
                || choiceMemento instanceof ObjectMementoForEmpty) {
            return PlaceholderLiteral.NULL_REPRESENTATION.asText(this::translate);
        }
        return translate(choiceMemento.getTitle());
    }

    @Override
    public String getIdValue(final ObjectMemento choiceMemento) {
        if (choiceMemento == null) {
            return NULL_PLACEHOLDER;
        }
        return choiceMemento.bookmark().stringify();
    }

    @Override
    public void query(
            final String term,
            final int page,
            final org.wicketstuff.select2.Response<ObjectMemento> response) {

        final List<ObjectMemento> mementos = _Lists.newArrayList(obtainMementos(term));
        // if not mandatory, and the list doesn't contain null already, then add it in.
        if(!scalarModel.isRequired()
                && !mementos.contains(null)) {
            mementos.add(0, null);
        }
        response.addAll(mementos);
    }

    protected abstract Can<ObjectMemento> obtainMementos(String term);

    /**
     * Filters all choices against a term by using their
     * {@link ManagedObject#titleString() title string}
     *
     * @param term The term entered by the user
     * @param choicesMementos The collections of choices to filter
     * @return A list of all matching choices
     */
    protected final Can<ObjectMemento> obtainMementos(
            final String term,
            final Can<ObjectMemento> choicesMementos) {

        if (Strings.isEmpty(term)) {
            return choicesMementos;
        }

        val commonContext = getCommonContext();

        return choicesMementos.filter((final ObjectMemento candidate)->{
            val objectAdapter = commonContext.reconstructObject(candidate);
            val title = objectAdapter.titleString();
            return title.toLowerCase().contains(term.toLowerCase());
        });

    }

    @Override
    public Collection<ObjectMemento> toChoices(final Collection<String> ids) {

        return _NullSafe.stream(ids)
                .map(this::idToMemento)
                .collect(Collectors.toList());
    }

    /** whether this adapter is dependent on previous (pending) arguments */
    public boolean dependsOnPreviousArgs() {
        return true;
    }

    // -- DEPENDENCIES

    protected IsisAppCommonContext getCommonContext() {
        if(commonContext==null) {
            commonContext = scalarModel.getCommonContext();
        }
        return commonContext;
    }

    protected final Wicket getSettings() {
        return getCommonContext().getConfiguration().getViewer().getWicket();
    }

    // -- HELPER

    private @Nullable ObjectMemento idToMemento(final String id) {
        if(NULL_PLACEHOLDER.equals(id)) {
            return null;
        }
        val memento = Bookmark.parse(id)
                .map(getCommonContext()::mementoForBookmark)
                .orElse(null);
        return memento;
    }

    /**
     * Translate without context: Tooltips, Button-Labels, etc.
     */
    private final String translate(final String input) {
        return getCommonContext().getTranslationService().translate(TranslationContext.empty(), input);
    }


}
