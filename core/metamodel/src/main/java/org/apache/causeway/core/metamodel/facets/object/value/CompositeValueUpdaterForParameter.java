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

import org.apache.causeway.commons.internal.delegate._Delegate;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectActionMixedIn;

public class CompositeValueUpdaterForParameter
extends CompositeValueUpdater {

    public static ObjectAction createProxy(
            final ParameterNegotiationModel parameterNegotiationModel,
            final int paramIndex,
            final ObjectActionMixedIn delegate) {
        return _Delegate.createProxy(ObjectAction.class,
                new CompositeValueUpdaterForParameter(parameterNegotiationModel, paramIndex, delegate));
    }

    private final ParameterNegotiationModel parameterNegotiationModel;
    private final int paramIndex;

    protected CompositeValueUpdaterForParameter(
            final ParameterNegotiationModel parameterNegotiationModel,
            final int paramIndex,
            final ObjectActionMixedIn delegate) {
        super(delegate);
        this.parameterNegotiationModel = parameterNegotiationModel;
        this.paramIndex = paramIndex;
    }

    @Override
    public ObjectSpecification getReturnType() {
        return parameterNegotiationModel.getParamMetamodel(paramIndex).getElementType();
    }

    @Override
    protected ManagedObject map(final ManagedObject newParamValue) {
        parameterNegotiationModel.setParamValue(paramIndex, newParamValue);
        return newParamValue;
    }

}
