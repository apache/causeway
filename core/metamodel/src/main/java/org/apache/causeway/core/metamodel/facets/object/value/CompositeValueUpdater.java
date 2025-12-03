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
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.commons.ParameterConverters;
import org.apache.causeway.core.metamodel.consent.Allow;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.HasFacetedMethod;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.metamodel.facets.object.value.CompositeValueUpdater.CompositeValueUpdaterForParameter;
import org.apache.causeway.core.metamodel.facets.object.value.CompositeValueUpdater.CompositeValueUpdaterForProperty;
import org.apache.causeway.core.metamodel.interactions.InteractionConstraint;
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
 * Implementations are implementing {@link HasObjectAction} and delegating to {@link #mixedInAction()}.
 */
public sealed interface CompositeValueUpdater extends HasObjectAction
permits CompositeValueUpdaterForProperty, CompositeValueUpdaterForParameter {

    MixedInAction mixedInAction();
    ObjectSpecification returnType();
    ManagedObject map(final ManagedObject valueType);

    // HasObjectAction
    @Override default ObjectAction getObjectAction() { return mixedInAction(); }

    // makes sure PromptStyle is always INLINE_AS_IF_EDIT
    default CompositeValueUpdater overrideFacets() {
        FacetUtil.computeIfAbsentExact(mixedInAction().getFacetHolder(),
            PromptStyleFacet.class,
            PromptStyleFacet.class,
            Precedence.HIGH,
            PromptStyleFacet::compositeValueEdit);
        return this;
    }

    // -- OBJECT ACTION MOCKUP

    @Override default Consent isVisible(final ManagedObject a, final InteractionConstraint iConstraint) { return Allow.DEFAULT; }
    @Override default Consent isUsable(final ManagedObject a, final InteractionConstraint iConstraint) { return Allow.DEFAULT; }
    @Override default PromptStyle getPromptStyle() { return PromptStyle.INLINE_AS_IF_EDIT; }
    @Override default SemanticsOf getSemantics() { return SemanticsOf.SAFE; }

    @Override default ManagedObject execute(
            final InteractionHead head, final Can<ManagedObject> parameters,
            final InteractionConstraint iConstraint) {
        return map(simpleExecute(head, parameters));
    }
    @Override default ManagedObject executeWithRuleChecking(
        final InteractionHead head, final Can<ManagedObject> parameters,
        final InteractionConstraint iConstraint) throws AuthorizationException {
        return execute(head, parameters, iConstraint);
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

    static ObjectAction createDelegatorForParameter(
        final ParameterNegotiationModel parameterNegotiationModel,
        final int paramIndex,
        final MixedInAction mixedInAction) {
        return new CompositeValueUpdaterForParameter(parameterNegotiationModel, paramIndex, mixedInAction)
            .overrideFacets();
    }

    static ObjectAction createDelegatorForProperty(
        final ManagedProperty managedProperty,
        final MixedInAction mixedInAction) {
        return new CompositeValueUpdaterForProperty(managedProperty, mixedInAction)
            .overrideFacets();
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
