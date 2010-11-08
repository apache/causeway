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


package org.apache.isis.core.metamodel.spec.feature;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.interactions.ActionArgumentContext;
import org.apache.isis.core.metamodel.spec.NamedAndDescribed;

/**
 * Analogous to {@link ObjectAssociation}.
*/
public interface ObjectActionParameter extends ObjectFeature, NamedAndDescribed, CurrentHolder {

    /**
     * If true then can cast to a {@link OneToOneActionParameter}.
     * 
     * <p>
     * Either this or {@link #isCollection()} will be true.
     * 
     * <p>
     * Design note: modelled after {@link ObjectAssociation#isNotCollection()}
     */
    boolean isObject();

    /**
     * Only for symmetry with {@link ObjectAssociation}, however since the NOF does not support
     * collections as actions all implementations should return <tt>false</tt>.
     */
    boolean isCollection();

    /**
     * Owning {@link ObjectAction}.
     */
    ObjectAction getAction();

    /**
     * Returns a flag indicating if it can be left unset when the action can be invoked.
     */
    boolean isOptional();

    /**
     * Returns the 0-based index to this parameter.
     */
    int getNumber();

    ActionArgumentContext createProposedArgumentInteractionContext(
            AuthenticationSession session,
            InteractionInvocationMethod invocationMethod,
            ObjectAdapter targetObject,
            ObjectAdapter[] args,
            int position);

    /**
     * Whether proposed value for this parameter is valid.
     * 
     * @param adapter
     * @param proposedValue
     * @return
     */
    String isValid(ObjectAdapter adapter, Object proposedValue);

    
    ObjectAdapter[] getChoices(ObjectAdapter adapter);
    
    ObjectAdapter getDefault(ObjectAdapter adapter);
}
