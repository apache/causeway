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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

/**
 * REVIEW: this is a bit hacky having 'Contributed' subtypes of both {@link ObjectActionParameterParseable} and also
 * {@link OneToOneActionParameterImpl}.  However, the {@link ObjectActionParameterParseable parseable} version
 * only seems to be used by the DnD viewer; Scimpi, Wicket and RO do not.  So, we could hopefully simplify the
 * hierarchy at some point.
 */
public class ObjectActionParameterParseableContributee extends ObjectActionParameterParseable implements ObjectActionParameterContributee {

    private final ObjectAdapter serviceAdapter;
    @SuppressWarnings("unused")
    private final ObjectActionImpl serviceAction;
    private final ObjectActionParameter serviceActionParameter;
    @SuppressWarnings("unused")
    private final int serviceParamNumber;

    public ObjectActionParameterParseableContributee(
            final ObjectAdapter serviceAdapter,
            final ObjectActionImpl serviceAction,
            final ObjectActionParameterAbstract serviceActionParameter,
            final int serviceParamNumber,
            final int number,
            final ObjectActionContributee objectAction) {
        super(number, objectAction, serviceActionParameter.getPeer());
        this.serviceAdapter = serviceAdapter;
        this.serviceAction = serviceAction;
        this.serviceActionParameter = serviceActionParameter;
        this.serviceParamNumber = serviceParamNumber;
    }

    @Override
    public ObjectAdapter[] getAutoComplete(ObjectAdapter adapter, String searchArg) {
        return serviceActionParameter.getAutoComplete(serviceAdapter, searchArg);
    }

    @Override
    public ObjectAdapter[] getChoices(final ObjectAdapter adapter) {
        return serviceActionParameter.getChoices(serviceAdapter);
    }

    @Override
    public ObjectAdapter getDefault(final ObjectAdapter adapter) {
        return serviceActionParameter.getDefault(serviceAdapter);
    }

}
