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

package org.apache.isis.core.metamodel.specloader.specimpl;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.metamodel.adapter.MutableProposedHolder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.propparam.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.propparam.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.objpropparam.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ParseableEntryActionParameter;

public class ObjectActionParameterParseable extends ObjectActionParameterAbstract implements ParseableEntryActionParameter {

    public ObjectActionParameterParseable(final int index, final ObjectActionImpl action, final TypedHolder peer) {
        super(index, action, peer);
    }

    @Override
    public int getNoLines() {
        final MultiLineFacet facet = getFacet(MultiLineFacet.class);
        return facet.numberOfLines();
    }

    @Override
    public boolean canWrap() {
        final MultiLineFacet facet = getFacet(MultiLineFacet.class);
        return !facet.preventWrapping();
    }

    @Override
    public int getMaximumLength() {
        final MaxLengthFacet facet = getFacet(MaxLengthFacet.class);
        return facet.value();
    }

    @Override
    public int getTypicalLineLength() {
        final TypicalLengthFacet facet = getFacet(TypicalLengthFacet.class);
        return facet.value();
    }
    
    protected ObjectAdapter doCoerceProposedValue(ObjectAdapter adapter, Object proposedValue, final Localization localization) {
        // try to parse
        if (!(proposedValue instanceof String)) {
            return null;
        }
        final String proposedString = (String) proposedValue;

        final ObjectSpecification parameterSpecification = getSpecification();
        final ParseableFacet p = parameterSpecification.getFacet(ParseableFacet.class);
        try {
            final ObjectAdapter parsedAdapter = p.parseTextEntry(null, proposedString, localization);
            return parsedAdapter;
        } catch(Exception ex) {
            return null;
        }
    }


    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////

    @Override
    public Instance getInstance(final ObjectAdapter adapter) {
        final OneToOneActionParameter specification = this;
        return adapter.getInstance(specification);
    }

    // //////////////////////////////////////////////////////////////////////
    // get, set
    // //////////////////////////////////////////////////////////////////////

    /**
     * Gets the proposed value of the {@link Instance} (downcast as a
     * {@link MutableProposed}, wrapping the proposed value into a
     * {@link ObjectAdapter}.
     */
    @Override
    public ObjectAdapter get(final ObjectAdapter owner) {
        final MutableProposedHolder proposedHolder = getProposedHolder(owner);
        final Object proposed = proposedHolder.getProposed();
        return getAdapterMap().adapterFor(proposed);
    }

    /**
     * Sets the proposed value of the {@link Instance} (downcast as a
     * {@link MutableProposed}, unwrapped the proposed value from a
     * {@link ObjectAdapter}.
     */
    public void set(final ObjectAdapter owner, final ObjectAdapter newValue) {
        final MutableProposedHolder proposedHolder = getProposedHolder(owner);
        final Object newValuePojo = newValue.getObject();
        proposedHolder.setProposed(newValuePojo);
    }

    private MutableProposedHolder getProposedHolder(final ObjectAdapter owner) {
        final Instance instance = getInstance(owner);
        if (!(instance instanceof MutableProposedHolder)) {
            throw new IllegalArgumentException("Instance should implement MutableProposedHolder");
        }
        final MutableProposedHolder proposedHolder = (MutableProposedHolder) instance;
        return proposedHolder;
    }

    @Override
    public FeatureType getFeatureType() {
        return FeatureType.ACTION_PARAMETER;
    }

}
