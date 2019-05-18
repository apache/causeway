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

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

public class ObjectAdapterMementoProviderForValueChoices
extends ObjectAdapterMementoProviderAbstract implements ObjectAdapterMementoProviderForChoices {

    private static final long serialVersionUID = 1L;
    private final List<ObjectAdapterMemento> choicesMementos;

    public ObjectAdapterMementoProviderForValueChoices(
            final ScalarModel scalarModel,
            final List<ObjectAdapterMemento> choicesMementos,
            final WicketViewerSettings wicketViewerSettings) {
        super(scalarModel, wicketViewerSettings);
        this.choicesMementos = choicesMementos;
    }

    @Override
    protected List<ObjectAdapterMemento> obtainMementos(String term) {
        return obtainMementos(term, choicesMementos);
    }

    @Override
    public List<ObjectAdapterMemento> getChoiceMementos() {
        return choicesMementos;
    }

    @Override
    public Collection<ObjectAdapterMemento> toChoices(final Collection<String> ids) {
        final List<ObjectAdapterMemento> mementos = obtainMementos(null);

        final Predicate<ObjectAdapterMemento> lookupOam = new Predicate<ObjectAdapterMemento>() {
            @Override
            public boolean apply(ObjectAdapterMemento input) {
                final String id = getIdValue(input);
                return ids.contains(id);
            }
        };
        return _Lists.newArrayList(FluentIterable.from(mementos).filter(lookupOam).toList());
    }

}
