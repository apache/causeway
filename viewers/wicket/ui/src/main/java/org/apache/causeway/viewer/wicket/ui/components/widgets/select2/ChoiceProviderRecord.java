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
package org.apache.causeway.viewer.wicket.ui.components.widgets.select2;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.wicket.util.string.Strings;
import org.jspecify.annotations.Nullable;
import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Response;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.attrib.UiAttribute;
import org.apache.causeway.viewer.commons.model.attrib.UiParameter;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;

public record ChoiceProviderRecord(
    UiAttributeWkt attributeModel,
    UiAttribute.ChoiceProviderSort choiceProviderSort)
implements HasCommonContext, Serializable {

    public ChoiceProviderRecord(
            final UiAttributeWkt attributeModel) {
        this(attributeModel, UiAttribute.ChoiceProviderSort.valueOf(attributeModel));
    }
    
    /**
     * Get the value for displaying to an end user.
     */
    public String getDisplayValue(final ObjectMemento choiceMemento) {
        if (choiceMemento == null
                || choiceMemento.isEmpty()) {
            return getPlaceholderRenderService().asText(PlaceholderLiteral.NULL_REPRESENTATION);
        }
        return translate(choiceMemento.title());
    }

    /**
     * This method is called to get the id value of an object (used as the value attribute of a
     * choice element) The id can be extracted from the object like a primary key, or if the list is
     * stable you could just return a toString of the index.
     * <p>
     * Note that the given index can be {@code -1} if the object in question is not contained in the
     * available choices.
     */
    public String getIdValue(final ObjectMemento choiceMemento) {
        if (choiceMemento == null) {
            return ObjectMemento.NULL_ID;
        }
        return ObjectMemento.enstringToUrlBase64(choiceMemento);
    }

    /**
     * Queries application for choices that match the search {@code term} and adds them to the
     * {@code response}
     */
    public void query(
            final String term,
            final int page,
            final org.wicketstuff.select2.Response<ObjectMemento> response) {

        var mementosFiltered = query(term);

        if(isRequired()) {
            response.addAll(mementosFiltered.toList());
            return;
        }

        // else, if not mandatory, prepend null
        var mementosIncludingNull = mementosFiltered.toArrayList();
        mementosIncludingNull.add(0, null);

        response.addAll(mementosIncludingNull);
    }

    /**
     * Converts a list of choice ids back into application's choice objects. When the choice
     * provider is attached to a single-select component the {@code ids} collection will contain
     * exactly one id, and a collection containing exactly one choice should be returned.
     */
    public Collection<ObjectMemento> toChoices(final Collection<String> ids) {
        return _NullSafe.stream(ids)
                .map(this::mementoFromIdWithNullHandling)
                .collect(Collectors.toList());
    }
    
    // -- UTIL
    
    /** adapter method */
    ChoiceProvider<ObjectMemento> toSelect2ChoiceProvider() {
        var delegate = this;
        return new ChoiceProvider<ObjectMemento>() {
            private static final long serialVersionUID = 1L;

            @Override public Collection<ObjectMemento> toChoices(Collection<String> ids) {
                return delegate.toChoices(ids);
            }
            @Override public void query(String term, int page, Response<ObjectMemento> response) {
                delegate.query(term, page, response);
            }
            @Override public String getIdValue(ObjectMemento object) {
                return delegate.getIdValue(object);
            }
            @Override public String getDisplayValue(ObjectMemento object) {
                return delegate.getDisplayValue(object);
            }
        };
    }
    
    // -- HELPER
    
    private Can<ObjectMemento> queryAll() {
        return attributeModel().getChoices() // must not return detached entities
                .map(ManagedObject::getMementoElseFail);
    }

    private Can<ObjectMemento> queryWithAutoCompleteUsingObjectSpecification(final String term) {
        var autoCompleteAdapters = Facets
                .autoCompleteExecute(attributeModel().getElementType(), term);
        return autoCompleteAdapters
                .map(ManagedObject::getMementoElseFail);
    }

    private Can<ObjectMemento> queryWithAutoComplete(final String term) {
        var attributeModel = attributeModel();
        var pendingArgs = attributeModel.isParameter()
                ? ((UiParameter)attributeModel).getParameterNegotiationModel().getParamValues()
                : Can.<ManagedObject>empty();
        var pendingArgMementos = pendingArgs
                .map(ManagedObject::getMementoElseFail);

        if(attributeModel.isParameter()) {
            // recover any pendingArgs
            var paramModel = (UiParameter)attributeModel;

            paramModel
                .getParameterNegotiationModel()
                .setParamValues(
                        reconstructPendingArgs(paramModel, pendingArgMementos));
        }

        return attributeModel
                .getAutoComplete(term)
                .map(ManagedObject::getMementoElseFail);
    }

    private Can<ManagedObject> reconstructPendingArgs(
            final UiParameter parameterModel,
            final Can<ObjectMemento> pendingArgMementos) {
        var pendingArgsList = _NullSafe.stream(pendingArgMementos)
            .map(getObjectManager()::demementify)
            .collect(Can.toCan());

       return pendingArgsList;
    }
    
    private @Nullable ObjectMemento mementoFromIdWithNullHandling(final String id) {
        if(ObjectMemento.NULL_ID.equals(id)) return null;
        
        return mementoFromId(id);
    }
    
    /**
     * Whether to not prepend <code>null</code> as choice candidate.
     */
    private boolean isRequired() {
        return attributeModel().isRequired();
    }

    /**
     * Get choice candidates with filtering (don't include <code>null</code>).
     */
    private Can<ObjectMemento> query(final String term) {
        return switch(choiceProviderSort) {
            case CHOICES->filter(term, queryAll());
            case AUTO_COMPLETE->queryWithAutoComplete(term);
            case OBJECT_AUTO_COMPLETE->queryWithAutoCompleteUsingObjectSpecification(term);
            case NO_CHOICES->Can.empty();
        };
    }

    /**
     * Filters all choices against a term by using their
     * {@link ManagedObject#getTitle() title string}
     *
     * @param term The term entered by the user
     * @param choiceMementos The collections of choices to filter
     * @return A list of all matching choices
     */
    private Can<ObjectMemento> filter(
            final String term,
            final Can<ObjectMemento> choiceMementos) {

        if (Strings.isEmpty(term)) return choiceMementos;

        var translationContext = TranslationContext.empty();
        var translator = getTranslationService();
        var termLower = term.toLowerCase();

        return choiceMementos.filter((final ObjectMemento candidateMemento)->{
            var title = translator.translate(translationContext, candidateMemento.title());
            return title.toLowerCase().contains(termLower);
        });
    }

    private @Nullable ObjectMemento mementoFromId(final @Nullable String id) {
        return ObjectMemento.destringFromUrlBase64(id);
    }

}
