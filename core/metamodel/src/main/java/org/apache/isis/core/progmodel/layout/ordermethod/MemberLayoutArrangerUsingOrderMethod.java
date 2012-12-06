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
package org.apache.isis.core.progmodel.layout.ordermethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.lang.JavaClassUtils;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.object.orderactions.ActionOrderFacet;
import org.apache.isis.core.metamodel.facets.object.orderfields.FieldOrderFacet;
import org.apache.isis.core.metamodel.layout.MemberLayoutArranger;
import org.apache.isis.core.metamodel.layout.OrderSet;
import org.apache.isis.core.metamodel.layout.ordermethod.SimpleOrderSet;
import org.apache.isis.core.metamodel.methodutils.MethodFinderUtils;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class MemberLayoutArrangerUsingOrderMethod implements MemberLayoutArranger {

    private static final Logger LOG = Logger.getLogger(MemberLayoutArrangerUsingOrderMethod.class);

    private static final Object[] NO_PARAMETERS = new Object[0];
    private static final Class<?>[] NO_PARAMETERS_TYPES = new Class[0];

    private static final String FIELD_PREFIX = null;

    private static final String ACTION_PREFIX = null;

    // ////////////////////////////////////////////////////////////////////////////
    // constructor
    // ////////////////////////////////////////////////////////////////////////////

    public MemberLayoutArrangerUsingOrderMethod() {
    }

    // ////////////////////////////////////////////////////////////////////////////
    // associations
    // ////////////////////////////////////////////////////////////////////////////

    @Override
    public OrderSet createAssociationOrderSetFor(final ObjectSpecification spec, final List<FacetedMethod> associationMethods) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MemberLayoutArrangerUsingOrderMethod: createAssociationOrderSetFor " + spec.getFullIdentifier());
        }

        // ... and the ordering of the properties and collections
        final FieldOrderFacet fieldOrderFacet = spec.getFacet(FieldOrderFacet.class);
        String fieldOrder = fieldOrderFacet == null ? null : fieldOrderFacet.value();

        if (fieldOrder == null) {
            fieldOrder = invokeSortOrderMethod(spec, FIELD_PREFIX);
        }
        return createOrderSet(fieldOrder, associationMethods);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // actions
    // ////////////////////////////////////////////////////////////////////////////

    @Override
    public OrderSet createActionOrderSetFor(final ObjectSpecification spec, final List<FacetedMethod> actionFacetedMethodList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MemberLayoutArrangerUsingOrderMethod: createAssociationOrderSetFor " + spec.getFullIdentifier());
        }

        final ActionOrderFacet actionOrderFacet = spec.getFacet(ActionOrderFacet.class);
        String actionOrder = actionOrderFacet == null ? null : actionOrderFacet.value();
        if (actionOrder == null) {
            actionOrder = invokeSortOrderMethod(spec, ACTION_PREFIX);
        }
        return createOrderSet(actionOrder, actionFacetedMethodList);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // helpers
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Invokes a method called <tt>xxxOrder()</tt>, returning a {@link String}.
     * 
     * @param spec
     */
    private String invokeSortOrderMethod(final ObjectSpecification spec, final String methodNamePrefix) {
        final List<Method> methods = Arrays.asList(spec.getCorrespondingClass().getMethods());
        final Method method = MethodFinderUtils.findMethod(methods, MethodScope.CLASS, (methodNamePrefix + "Order"), String.class, NO_PARAMETERS_TYPES);
        if (method == null) {
            return null;
        }

        if (!JavaClassUtils.isStatic(method)) {
            LOG.warn("method " + spec.getFullIdentifier() + "." + methodNamePrefix + "Order() must be declared as static");
            return null;
        }

        try {
            final String s = (String) method.invoke(null, NO_PARAMETERS);
            if (StringUtils.isNullOrEmpty(s)) {
                return null;
            }
            return s;
        } catch (final IllegalArgumentException e) {
            LOG.warn("method " + spec.getFullIdentifier() + "#" + method.getName() + "() should accept no parameters");
            return null;
        } catch (final IllegalAccessException e) {
            LOG.warn("method " + spec.getFullIdentifier() + "#" + method.getName() + "() must be declared as public");
            return null;
        } catch (final InvocationTargetException e) {
            LOG.warn("method " + spec.getFullIdentifier() + "#" + method.getName() + "() has thrown an exception");
            return null;
        }
    }

    private OrderSet createOrderSet(final String order, final List<FacetedMethod> members) {
        if (order == null) {
            return null;
        }
        return SimpleOrderSet.createOrderSet(order, members);
    }
}
