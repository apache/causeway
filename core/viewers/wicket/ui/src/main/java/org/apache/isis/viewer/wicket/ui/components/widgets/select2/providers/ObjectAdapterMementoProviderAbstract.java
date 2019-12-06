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
import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;
import org.wicketstuff.select2.ChoiceProvider;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.IsisConverterLocator;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;

import lombok.Getter;
import lombok.val;

public abstract class ObjectAdapterMementoProviderAbstract extends ChoiceProvider<ObjectMemento> {

    private static final long serialVersionUID = 1L;

    protected static final String NULL_PLACEHOLDER = "$$_isis_null_$$";
    private static final String NULL_DISPLAY_TEXT = "";

    @Getter private final ScalarModel scalarModel;
    @Getter private final transient IsisWebAppCommonContext commonContext;
    @Getter private final transient WicketViewerSettings wicketViewerSettings;

    public ObjectAdapterMementoProviderAbstract(ScalarModel scalarModel) {
        
        this.scalarModel = scalarModel;
        this.commonContext = scalarModel.getCommonContext();
        this.wicketViewerSettings = commonContext.lookupServiceElseFail(WicketViewerSettings.class);
    }

    @Override
    public String getDisplayValue(final ObjectMemento choice) {
        if (choice == null) {
            return NULL_DISPLAY_TEXT;
        }

        val objectAdapter = commonContext.reconstructObject(choice); 
        final IConverter<Object> converter = findConverter(objectAdapter);
        return converter != null
                ? converter.convertToString(objectAdapter.getPojo(), getLocale())
                        : objectAdapter.titleString(null);
    }

    protected Locale getLocale() {
        return Session.exists() ? Session.get().getLocale() : Locale.ENGLISH;
    }

    protected IConverter<Object> findConverter(final ManagedObject objectAdapter) {
        return IsisConverterLocator.findConverter(objectAdapter, wicketViewerSettings);
    }

    @Override
    public String getIdValue(final ObjectMemento choice) {
        if (choice == null) {
            return NULL_PLACEHOLDER;
        }
        final ObjectSpecId objectSpecId = choice.getObjectSpecId();
        final ObjectSpecification spec = commonContext.getSpecificationLoader().lookupBySpecIdElseLoad(objectSpecId);

        // support enums that are implementing an interface; only know this late in the day
        // TODO: this is a hack, really should push this deeper so that Encodeable OAMs also prefix themselves with their objectSpecId
        if(spec != null && spec.isEncodeable()) {
            return objectSpecId.asString() + ":" + choice.asString();
        } else {
            return choice.asString();
        }
    }

    @Override
    public void query(final String term, final int page, final org.wicketstuff.select2.Response<ObjectMemento> response) {

        final List<ObjectMemento> mementos = _Lists.newArrayList(obtainMementos(term));
        // if not mandatory, and the list doesn't contain null already, then add it in.
        if(!scalarModel.isRequired() && !mementos.contains(null)) {
            mementos.add(0, null);
        }
        response.addAll(mementos);
    }

    protected abstract List<ObjectMemento> obtainMementos(String term);

    /**
     * Filters all choices against a term by using their
     * {@link ManagedObject#titleString(ManagedObject) title string}
     *
     * @param term The term entered by the user
     * @param choicesMementos The collections of choices to filter
     * @return A list of all matching choices
     */
    protected final List<ObjectMemento> obtainMementos(String term, Collection<ObjectMemento> choicesMementos) {
        List<ObjectMemento> matches = _Lists.newArrayList();
        if (Strings.isEmpty(term)) {
            matches.addAll(choicesMementos);
        } else {
            for (ObjectMemento candidate : choicesMementos) {
                val objectAdapter = commonContext.reconstructObject(candidate); 
                String title = objectAdapter.titleString(objectAdapter);
                if (title.toLowerCase().contains(term.toLowerCase())) {
                    matches.add(candidate);
                }
            }
        }

        return matches;
    }


}
