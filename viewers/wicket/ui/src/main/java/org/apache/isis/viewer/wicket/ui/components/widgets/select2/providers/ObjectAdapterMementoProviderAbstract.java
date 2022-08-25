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

import org.apache.wicket.util.string.Strings;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.val;

public abstract class ObjectAdapterMementoProviderAbstract
extends ChoiceProviderForScalarModel {

    private static final long serialVersionUID = 1L;

    protected ObjectAdapterMementoProviderAbstract(final ScalarModel scalarModel) {
        super(scalarModel);
    }

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
        return super.filter(term, choicesMementos);
    }

    protected @Nullable ObjectMemento idToMemento(final String id) {
        val memento = Bookmark.parse(id)
                .map(getCommonContext()::mementoForBookmark)
                .orElse(null); //FIXME did something go wrong?
        return memento;
    }

}
