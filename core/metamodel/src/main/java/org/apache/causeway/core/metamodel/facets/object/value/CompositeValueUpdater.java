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
package org.apache.causeway.core.metamodel.facets.object.value;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.delegate._Delegate;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.commons.ParameterConverters;
import org.apache.causeway.core.metamodel.consent.Allow;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.HasFacetedMethod;
import org.apache.causeway.core.metamodel.facets.object.value.CompositeValueUpdater.CompositeValueUpdaterForParameter;
import org.apache.causeway.core.metamodel.facets.object.value.CompositeValueUpdater.CompositeValueUpdaterForProperty;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.HasObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.MixedInAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

/**
 * Implementations are proxied in support of composite value types.
 * <p>
 * @implSpec The proxy mimics an {@link ObjectAction},
 *      hence extending {@link HasObjectAction} for delegation to {@link #mixedInAction()}.
 */
public sealed interface CompositeValueUpdater extends HasObjectAction
permits CompositeValueUpdaterForProperty, CompositeValueUpdaterForParameter {

    MixedInAction mixedInAction();
    ObjectSpecification returnType();
    ManagedObject map(final ManagedObject valueType);

    // HasObjectAction
    @Override default ObjectAction getObjectAction() { return mixedInAction(); }

    // -- OBJECT ACTION MOCKUP

    @Override default String getId() { return "proxiedCompositeValueUpdaterAction"; }
    @Override default Consent isVisible(final ManagedObject a, final InteractionInitiatedBy b, final Where c) { return Allow.DEFAULT; }
    @Override default Consent isUsable(final ManagedObject a, final InteractionInitiatedBy b, final Where c) { return Allow.DEFAULT; }
    @Override default PromptStyle getPromptStyle() { return PromptStyle.INLINE_AS_IF_EDIT; }
    @Override default SemanticsOf getSemantics() { return SemanticsOf.SAFE; }

    @Override default ManagedObject execute(
            final InteractionHead head, final Can<ManagedObject> parameters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return map(simpleExecute(head, parameters));
    }

    // -- IMPLEMENTATIONS

    record CompositeValueUpdaterForParameter(
        ParameterNegotiationModel parameterNegotiationModel,
        int paramIndex,
        MixedInAction mixedInAction) implements CompositeValueUpdater {

        @Override
        public ObjectSpecification returnType() {
            return parameterNegotiationModel.getParamMetamodel(paramIndex).getElementType();
        }

        @Override
        public ManagedObject map(final ManagedObject newParamValue) {
            parameterNegotiationModel.setParamValue(paramIndex, newParamValue);
            return newParamValue;
        }
    }

    record CompositeValueUpdaterForProperty(
        ManagedProperty managedProperty,
        MixedInAction mixedInAction
        ) implements CompositeValueUpdater {

        @Override
        public ObjectSpecification returnType() {
            return managedProperty.getElementType();
        }

        @Override
        public ManagedObject map(final ManagedObject valueType) {
            var propNeg = managedProperty.startNegotiation();
            propNeg.getValue().setValue(valueType);
            propNeg.submit();
            return managedProperty.getOwner();
        }
    }

    // -- FACTORIES

    static ObjectAction createProxyForParameter(
        final ParameterNegotiationModel parameterNegotiationModel,
        final int paramIndex,
        final MixedInAction mixedInAction) {
        return _Delegate.createProxy(ObjectAction.class,
                new CompositeValueUpdaterForParameter(parameterNegotiationModel, paramIndex, mixedInAction));
    }

    static ObjectAction createProxyForProperty(
        final ManagedProperty managedProperty,
        final MixedInAction mixedInAction) {
        return _Delegate.createProxy(ObjectAction.class,
                new CompositeValueUpdaterForProperty(managedProperty, mixedInAction));
    }

    // -- HELPER

    private ManagedObject simpleExecute(
            final InteractionHead head, final Can<ManagedObject> parameters) {

        var methodFacade = mixedInAction() instanceof HasFacetedMethod facetedMethodHolder
            ? facetedMethodHolder.getFacetedMethod().methodFacade()
            : null;
        if(methodFacade==null) return ManagedObject.empty(mixedInAction().getReturnType()); // unsupported MixedInAction

        var method = methodFacade.asMethodForIntrospection();
        final Object[] executionParameters = MmUnwrapUtils.multipleAsArray(parameters);
        final Object targetPojo = MmUnwrapUtils.single(head.target());
        var resultPojo = CanonicalInvoker
                .invokeWithConvertedArgs(method.method(), targetPojo,
                        methodFacade.getArguments(executionParameters, ParameterConverters.DEFAULT));

        return ManagedObject.value(mixedInAction().getReturnType(), resultPojo);
    }

}
