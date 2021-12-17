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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.List;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ActionInteractionHead
extends InteractionHead
implements HasMetaModel<ObjectAction> {

    @Getter(onMethod = @__(@Override))
    @NonNull private final ObjectAction metaModel;
    @Getter private final MultiselectChoices multiselectChoices;

    public static ActionInteractionHead of(
            final @NonNull ObjectAction objectAction,
            final @NonNull ManagedObject owner,
            final @NonNull ManagedObject target) {
        return new ActionInteractionHead(objectAction, owner, target, Can::empty);
    }

    public static ActionInteractionHead of(
            final @NonNull ObjectAction objectAction,
            final @NonNull ManagedObject owner,
            final @NonNull ManagedObject target,
            final @NonNull MultiselectChoices multiselectChoices) {
        return new ActionInteractionHead(objectAction, owner, target, multiselectChoices);
    }

    protected ActionInteractionHead(
            final @NonNull ObjectAction objectAction,
            final @NonNull ManagedObject owner,
            final @NonNull ManagedObject target,
            final @NonNull MultiselectChoices multiselectChoices) {
        super(owner, target);
        this.metaModel = objectAction;
        this.multiselectChoices = multiselectChoices;
    }

    /**
     * Immutable tuple of ManagedObjects, each representing {@code null} and each holding
     * the corresponding parameter's {@code ObjectSpecification}.
     * <p>
     * The size of the tuple corresponds to the number of parameters.
     */
    public Can<ManagedObject> getEmptyParameterValues() {
        return getMetaModel().getParameters().stream()
        .map(objectActionParameter->
            ManagedObject.empty(objectActionParameter.getElementType()))
        .collect(Can.toCan());
    }

    /**
     * Immutable tuple of ManagedObjects, wrapping the passed in argument pojos.
     * Nulls are allowed as arguments, but the list size must match the expected parameter count.
     * <p>
     * The size of the tuple corresponds to the number of parameters.
     * @param pojoArgList - argument pojos
     */
    public Can<ManagedObject> getPopulatedParameterValues(@Nullable final List<Object> pojoArgList) {

        val params = getMetaModel().getParameters();

        _Assert.assertEquals(params.size(), _NullSafe.size(pojoArgList));

        if(params.isEmpty()) {
            return Can.empty();
        }

        return params.zipMap(pojoArgList, (objectActionParameter, argPojo)->
            ManagedObject.of(objectActionParameter.getElementType(), argPojo));
    }

    public ParameterNegotiationModel emptyModel(final ManagedAction managedAction) {
        return ParameterNegotiationModel.of(managedAction, getEmptyParameterValues());
    }

    /**
     * See step 1 'Fill in defaults' in
     * <a href="https://cwiki.apache.org/confluence/display/ISIS/ActionParameterNegotiation">
     * ActionParameterNegotiation (wiki)
     * </a>
     */
    public ParameterNegotiationModel defaults(final ManagedAction managedAction) {

        // first pass ... empty values
        // second pass ... proposed default values
        // third pass ... verify we have found a fixed point
        final int maxIterations = 3;

        val params = getMetaModel().getParameters();

        val fixedPoint = fixedPointSearch(
                getEmptyParameterValues(),
                // vector of packed values - where each is either scalar or non-scalar
                paramVector->
                    params
                    .map(param->param
                            .getDefault(
                                    ParameterNegotiationModel
                                    .of(managedAction, paramVector)))
                ,
                maxIterations);

        if(fixedPoint.isRight()) {
            log.warn("Cannot find an initial fixed point for action "
                    + "parameter defaults on action {}.", getMetaModel());
        }

        return modelForParamValues(managedAction,
                fixedPoint.fold(
                left->left,
                right->right));
    }

    // -- HELPER

    private ParameterNegotiationModel modelForParamValues(
            final ManagedAction managedAction,
            @NonNull final Can<ManagedObject> paramValues) {
        return ParameterNegotiationModel.of(managedAction, paramValues);
    }

    /**
     * Returns either a fixed point or the last iteration.
     */
    private static <T> _Either<T, T> fixedPointSearch(
            final T start,
            final UnaryOperator<T> f,
            final int maxIterations) {

        T t1, t0 = start;
        for(int i=0; i<maxIterations; ++i) {
            t1 = f.apply(t0);
            if(t1.equals(t0)) {
                return _Either.left(t1);
            }
            t0 = t1;
        }

        return _Either.right(t0);
    }

}
