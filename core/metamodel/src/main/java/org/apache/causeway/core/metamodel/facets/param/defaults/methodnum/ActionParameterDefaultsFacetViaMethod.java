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
package org.apache.causeway.core.metamodel.facets.param.defaults.methodnum;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacetAbstract;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmInvokeUtils;

import lombok.Getter;
import lombok.NonNull;

public class ActionParameterDefaultsFacetViaMethod
extends ActionParameterDefaultsFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;
    private final int paramNum;
    private final Optional<ResolvedConstructor> patConstructor;

    /**
     *
     * @param method
     * @param paramNum - which parameter this facet relates to.
     * @param holder
     */
    public ActionParameterDefaultsFacetViaMethod(
            final ResolvedMethod method,
            final int paramNum,
            final Optional<ResolvedConstructor> patConstructor,
            final FacetHolder holder) {

        super(holder);
        this.methods = ImperativeFacet.singleMethod(method, patConstructor);
        this.paramNum = paramNum;
        this.patConstructor = patConstructor;
    }

    @Override
    public Intent getIntent() {
        return Intent.DEFAULTS;
    }

    @Override
    public Can<ManagedObject> getDefault(@NonNull final ParameterNegotiationModel pendingArgs) {

        var method = methods.getFirstElseFail();
        var managedParam = pendingArgs.getParamModels().getElseFail(paramNum);

        // call with args: defaultNAct(X x, Y y, ...)

        var defaultValue = patConstructor.isPresent()
            // PAT programming model
            ? MmInvokeUtils
                    .invokeWithPAT(patConstructor.get(), method.asMethodForIntrospection(),
                            pendingArgs.getActionTarget(), pendingArgs.getParamValues())
            // else support legacy programming model, call any-arg defaultNAct(...)
            : MmInvokeUtils
                    .invokeAutofit(method.asMethodElseFail().method(),
                        pendingArgs.getActionTarget(), pendingArgs.getParamValues());

        return _NullSafe.streamAutodetect(defaultValue)
                .map(pojo->pojo!=null
                    ? getObjectManager().adapt(pojo)
                    : managedParam.getMetaModel().isPlural()
                        ? null // assuming for non-scalar parameters, including null makes no sense
                        : ManagedObject.empty(managedParam.getElementType()))
                .collect(Can.toCan());
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
    }
}
