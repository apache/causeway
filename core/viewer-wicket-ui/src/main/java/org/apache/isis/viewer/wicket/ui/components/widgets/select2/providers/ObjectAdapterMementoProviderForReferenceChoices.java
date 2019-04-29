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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

public class ObjectAdapterMementoProviderForReferenceChoices
        extends ObjectAdapterMementoProviderAbstract implements ObjectAdapterMementoProviderForChoices {

    private static final long serialVersionUID = 1L;
    private final List<ObjectAdapterMemento> choiceMementos;

    public ObjectAdapterMementoProviderForReferenceChoices(
            final ScalarModel model,
            final WicketViewerSettings wicketViewerSettings,
            final List<ObjectAdapterMemento> choiceMementos) {
        super(model, wicketViewerSettings);
        this.choiceMementos = choiceMementos;
    }

    @Override
    protected List<ObjectAdapterMemento> obtainMementos(String term) {
        return obtainMementos(term, choiceMementos);
    }

    public List<ObjectAdapterMemento> getChoiceMementos() {
        return choiceMementos;
    }

    @Override
    public Collection<ObjectAdapterMemento> toChoices(final Collection<String> ids) {
        final Function<String, ObjectAdapterMemento> function = new Function<String, ObjectAdapterMemento>() {

            @Override
            public ObjectAdapterMemento apply(final String input) {
                if(NULL_PLACEHOLDER.equals(input)) {
                    return null;
                }
                final RootOid oid = RootOid.deString(input);

                final ObjectSpecId objectSpecId = oid.getObjectSpecId();
                final ObjectSpecification spec = getSpecificationLoader().lookupBySpecId(objectSpecId);
                // TODO: this knowledge should live deeper down
                if(spec.isEncodeable()) {
                    return ObjectAdapterMemento.createForEncodeable(objectSpecId, oid.getIdentifier());
                } else {
                    return ObjectAdapterMemento.createPersistent(oid);
                }
            }
        };
        return Lists.newArrayList(Collections2.transform(ids, function));
    }


}
