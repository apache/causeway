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

package org.apache.isis.core.metamodel.facetdecorator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

public class FacetDecoratorSet implements ApplicationScopedComponent {

    private final Map<Class<? extends Facet>, List<FacetDecorator>> facetDecoratorByFacetType = new HashMap<>();
    private final Set<FacetDecorator> facetDecoratorSet = new LinkedHashSet<FacetDecorator>();

    // ////////////////////////////////////////////////////////////
    // init, shutdown
    // ////////////////////////////////////////////////////////////

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    // ////////////////////////////////////////////////////////////
    // add, get, isEmpty
    // ////////////////////////////////////////////////////////////

    public void add(final FacetDecorator decorator) {
        final Class<? extends Facet>[] decoratedFacetTypes = decorator.getFacetTypes();
        for (final Class<? extends Facet> decoratedFacetType : decoratedFacetTypes) {
            getFacetDecoratorList(decoratedFacetType).add(decorator);
            facetDecoratorSet.add(decorator);
        }
    }

    private List<FacetDecorator> getFacetDecoratorList(final Class<? extends Facet> decoratedFacetType) {
        List<FacetDecorator> facetDecoratorList = facetDecoratorByFacetType.get(decoratedFacetType);
        if (facetDecoratorList == null) {
            facetDecoratorList = new ArrayList<>();
            facetDecoratorByFacetType.put(decoratedFacetType, facetDecoratorList);
        }
        return facetDecoratorList;
    }

    public void add(final List<FacetDecorator> decorators) {
        for (final FacetDecorator decorator : decorators) {
            add(decorator);
        }
    }

    public Set<FacetDecorator> getFacetDecorators() {
        return Collections.unmodifiableSet(facetDecoratorSet);
    }

    public boolean isEmpty() {
        return facetDecoratorByFacetType.isEmpty();
    }

    // ////////////////////////////////////////////////////////////
    // decorate
    // ////////////////////////////////////////////////////////////

    /**
     * @param holder
     */
    public void decorate(final ObjectSpecification holder) {
        decorateAllFacets(holder);
        for (final ObjectAssociation objectAssociation : holder.getAssociations(Contributed.EXCLUDED)) {
            this.decorateAllFacets(objectAssociation);
        }
        for (final ObjectAction objectAction : holder.getObjectActions(Contributed.EXCLUDED)) {
            decorateAllFacets(objectAction);
            final List<ObjectActionParameter> parameters = objectAction.getParameters();
            for (final ObjectActionParameter parameter : parameters) {
                this.decorateAllFacets(parameter);
            }
        }
    }

    private void decorateAllFacets(final FacetHolder holder) {
        if (isEmpty()) {
            return;
        }
        final Class<? extends Facet>[] facetTypes = holder.getFacetTypes();
        for (final Class<? extends Facet> facetType : facetTypes) {
            final Facet facet = holder.getFacet(facetType);
            decorateFacet(facet, holder);
        }
    }

    /**
     * REVIEW: the design is a little clumsy here. We want to decorate the
     * provided {@link Facet}, but its owning {@link FacetHolder holder} turns
     * out to be a runtime peer (eg <tt>JavaAction</tt>) rather than the
     * metamodel (eg {@link ObjectAction}). Since we want to decorate the
     * {@link ObjectAction}, we have to pass it through.
     */
    private void decorateFacet(final Facet facet, final FacetHolder requiredHolder) {
        final Class<? extends Facet> facetType = facet.facetType();
        final Class<? extends Facet> cls = facetType;
        final List<FacetDecorator> decoratorList = facetDecoratorByFacetType.get(cls);
        if (decoratorList == null) {
            return;
        }
        for (final FacetDecorator facetDecorator : decoratorList) {
            final Facet decoratingFacet = facetDecorator.decorate(facet, requiredHolder);
            if (decoratingFacet == null) {
                continue;
            }
            ensureDecoratorMetContract(facetDecorator, decoratingFacet, facetType, requiredHolder);
        }
    }

    private static void ensureDecoratorMetContract(final FacetDecorator facetDecorator, final Facet decoratingFacet, final Class<? extends Facet> facetType, final FacetHolder originalFacetHolder) {
        if (decoratingFacet.facetType() != facetType) {
            throw new MetaModelException(MessageFormat.format("Problem with facet decorator '{0}'; inconsistent decorating facetType() for {1}; was {2} but expectected facetType() of {3}", facetDecorator.getClass().getName(), decoratingFacet.getClass().getName(), decoratingFacet.facetType()
                    .getName(), facetType.getName()));
        }
        final Facet facetForFacetType = originalFacetHolder.getFacet(decoratingFacet.facetType());
        if (facetForFacetType != decoratingFacet) {
            throw new MetaModelException(MessageFormat.format("Problem with facet decorator '{0}'; has not replaced original facet for facetType() of {1}", facetDecorator.getClass().getName(), facetType.getName()));
        }
    }

    // ////////////////////////////////////////////////////////////
    // debugging
    // ////////////////////////////////////////////////////////////

    public void debugData(final DebugBuilder str) {
        str.appendTitle("Facet decorators");
        final Set<Class<? extends Facet>> facetTypes = facetDecoratorByFacetType.keySet();
        if (facetTypes.size() == 0) {
            str.append("none");
        } else {
            for (final Class<? extends Facet> cls : facetTypes) {
                str.appendln(cls.getName(), facetDecoratorByFacetType.get(cls));
            }
        }
    }

}
