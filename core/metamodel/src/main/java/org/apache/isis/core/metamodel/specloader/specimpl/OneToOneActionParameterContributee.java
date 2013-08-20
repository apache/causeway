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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionParameterContributee.Util;

public class OneToOneActionParameterContributee extends OneToOneActionParameterImpl implements ObjectActionParameterContributee{

    private final ObjectAdapter serviceAdapter;
    private final ObjectActionImpl serviceAction;
    private final ObjectActionParameter serviceActionParameter;
    @SuppressWarnings("unused")
    private final int serviceParamNumber;
    @SuppressWarnings("unused")
    private final int contributeeParamNumber;
    private final ObjectActionContributee contributeeAction;

    public OneToOneActionParameterContributee(
            final ObjectAdapter serviceAdapter,
            final ObjectActionImpl serviceAction,
            final ObjectActionParameterAbstract serviceActionParameter,
            final int serviceParamNumber,
            final int contributeeParamNumber,
            final ObjectActionContributee contributeeAction) {
        super(contributeeParamNumber, contributeeAction, serviceActionParameter.getPeer());
        this.serviceAdapter = serviceAdapter;
        this.serviceAction = serviceAction;
        this.serviceActionParameter = serviceActionParameter;
        this.serviceParamNumber = serviceParamNumber;
        this.contributeeParamNumber = contributeeParamNumber;
        this.contributeeAction = contributeeAction;
    }

    @Override
    public ObjectAdapter[] getAutoComplete(ObjectAdapter adapter, String searchArg) {
        return serviceActionParameter.getAutoComplete(serviceAdapter, searchArg);
    }

    protected ObjectAdapter targetForDefaultOrChoices(ObjectAdapter adapter, final ObjectAdapter[] argumentsIfAvailable) {
        return serviceAdapter;
    }

    protected ObjectAdapter[] argsForDefaultOrChoices(final ObjectAdapter adapter, final ObjectAdapter[] argumentsIfAvailable) {
        final int required = serviceAction.getParameterCount();
        final int existing = contributeeAction.getContributeeParam();
        
        List<ObjectAdapter> input = Util.toList(argumentsIfAvailable);
        List<ObjectAdapter> output = Util.update(input, required, existing, adapter);
        
        return output.toArray(new ObjectAdapter[]{});
    }
    
}
