/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.isis.commons.internal.collections._Lists;
import org.wicketstuff.select2.ChoiceProvider;
import org.apache.wicket.Session;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.IsisConverterLocator;

public abstract class ObjectAdapterMementoProviderAbstract extends ChoiceProvider<ObjectAdapterMemento> {

    private static final long serialVersionUID = 1L;

    protected static final String NULL_PLACEHOLDER = "$$_isis_null_$$";
    private static final String NULL_DISPLAY_TEXT = "";

    private final ScalarModel scalarModel;
    private final WicketViewerSettings wicketViewerSettings;

    public ObjectAdapterMementoProviderAbstract(final ScalarModel scalarModel, final WicketViewerSettings wicketViewerSettings) {
        this.scalarModel = scalarModel;
        this.wicketViewerSettings = wicketViewerSettings;
    }

    @Override
    public String getDisplayValue(final ObjectAdapterMemento choice) {
        if (choice == null) {
            return NULL_DISPLAY_TEXT;
        }

        final ObjectAdapter objectAdapter =
                choice.getObjectAdapter(
                        ConcurrencyChecking.NO_CHECK, getPersistenceSession(), getSpecificationLoader());
        final IConverter<Object> converter = findConverter(objectAdapter);
        return converter != null
                ? converter.convertToString(objectAdapter.getPojo(), getLocale())
                        : objectAdapter.titleString(null);
    }

    protected Locale getLocale() {
        return Session.exists() ? Session.get().getLocale() : Locale.ENGLISH;
    }

    protected IConverter<Object> findConverter(final ObjectAdapter objectAdapter) {
        return IsisConverterLocator.findConverter(objectAdapter, wicketViewerSettings);
    }

    @Override
    public String getIdValue(final ObjectAdapterMemento choice) {
        return choice != null? choice.asString(): NULL_PLACEHOLDER;
    }

    @Override
    public void query(final String term, final int page, final org.wicketstuff.select2.Response<ObjectAdapterMemento> response) {

        final List<ObjectAdapterMemento> mementos = _Lists.newArrayList(obtainMementos(term));
        // if not mandatory, and the list doesn't contain null already, then add it in.
        if(!scalarModel.isRequired() && !mementos.contains(null)) {
            mementos.add(0, null);
        }
        response.addAll(mementos);
    }

    protected abstract List<ObjectAdapterMemento> obtainMementos(String term);

    /**
     * Filters all choices against a term by using their
     * {@link org.apache.isis.core.metamodel.adapter.ObjectAdapter#titleString(org.apache.isis.core.metamodel.adapter.ObjectAdapter) title string}
     *
     * @param term The term entered by the user
     * @param choicesMementos The collections of choices to filter
     * @return A list of all matching choices
     */
    protected final List<ObjectAdapterMemento> obtainMementos(String term, Collection<ObjectAdapterMemento> choicesMementos) {
        List<ObjectAdapterMemento> matches = _Lists.newArrayList();
        if (Strings.isEmpty(term)) {
            matches.addAll(choicesMementos);
        } else {
            for (ObjectAdapterMemento candidate : choicesMementos) {
                ObjectAdapter objectAdapter = candidate.getObjectAdapter(ConcurrencyChecking.NO_CHECK,
                        getPersistenceSession(), getSpecificationLoader());
                String title = objectAdapter.titleString(objectAdapter);
                if (title.toLowerCase().contains(term.toLowerCase())) {
                    matches.add(candidate);
                }
            }
        }

        return matches;
    }


    protected ScalarModel getScalarModel() {
        return scalarModel;
    }


    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////


    protected SpecificationLoader getSpecificationLoader() {
        return getIsisSessionFactory().getSpecificationLoader();
    }

    PersistenceSession getPersistenceSession() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession();
    }

    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    public AuthenticationSession getAuthenticationSession() {
        return getIsisSessionFactory().getCurrentSession().getAuthenticationSession();
    }

    public DeploymentCategory getDeploymentCategory() {
        return getIsisSessionFactory().getDeploymentCategory();
    }


}
