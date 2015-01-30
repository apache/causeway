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
package org.apache.isis.viewer.wicket.ui.components.widgets;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.vaynberg.wicket.select2.TextChoiceProvider;
import org.apache.wicket.Session;
import org.apache.wicket.util.convert.IConverter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.IsisConverterLocator;

public abstract class ObjectAdapterMementoProviderAbstract extends TextChoiceProvider<ObjectAdapterMemento> {

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
    protected String getDisplayText(final ObjectAdapterMemento choice) {
        if (choice == null) {
            return NULL_DISPLAY_TEXT;
        }

        final ObjectAdapter objectAdapter = choice.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
        final IConverter<Object> converter = findConverter(objectAdapter);
        return converter != null
                ? converter.convertToString(objectAdapter.getObject(), getLocale())
                : objectAdapter.titleString(null);
    }

    protected Locale getLocale() {
        return Session.exists() ? Session.get().getLocale() : Locale.ENGLISH;
    }

    protected IConverter<Object> findConverter(final ObjectAdapter objectAdapter) {
        return IsisConverterLocator.findConverter(objectAdapter, wicketViewerSettings);
    }

    @Override
    protected Object getId(final ObjectAdapterMemento choice) {
        return choice != null? choice.asString(): NULL_PLACEHOLDER;
    }

    @Override
    public void query(final String term, final int page, final com.vaynberg.wicket.select2.Response<ObjectAdapterMemento> response) {
        
        final List<ObjectAdapterMemento> mementos = Lists.newArrayList(obtainMementos(term));
        // if not mandatory, and the list doesn't contain null already, then add it in.
        if(!scalarModel.isRequired() && !mementos.contains(null)) {
            mementos.add(0, null);
        }
        response.addAll(mementos);
    }

    protected abstract List<ObjectAdapterMemento> obtainMementos(String term);

    @Override
    public Collection<ObjectAdapterMemento> toChoices(final Collection<String> ids) {
        final Function<String, ObjectAdapterMemento> function = new Function<String, ObjectAdapterMemento>() {

            @Override
            public ObjectAdapterMemento apply(final String input) {
                if(NULL_PLACEHOLDER.equals(input)) {
                    return null;
                }
                final RootOid oid = RootOidDefault.deString(input, ObjectAdapterMemento.getOidMarshaller());
                return ObjectAdapterMemento.createPersistent(oid);
            }
        };
        return Collections2.transform(ids, function);
    }
    
    protected ScalarModel getScalarModel() {
        return scalarModel;
    }
}
