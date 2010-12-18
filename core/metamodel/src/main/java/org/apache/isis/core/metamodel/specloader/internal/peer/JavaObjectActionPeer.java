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


package org.apache.isis.core.metamodel.specloader.internal.peer;

import java.lang.reflect.Method;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.MemberType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.identifier.Util;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;


/*
 * TODO (in all Java...Peer classes) make all methodsArray throw ReflectiveActionException when 
 * an exception occurs when calling a method reflectively (see execute method).  Then instead of 
 * calling invocationExcpetion() the exception will be passed though, and dealt with generally by 
 * the reflection package (which will be the same for all reflectors and will allow the message to
 * be better passed back to the client).
 */
public class JavaObjectActionPeer extends JavaObjectMemberPeer implements ObjectActionPeer {

    private JavaObjectActionParamPeer[] paramPeers;

    public JavaObjectActionPeer(final Class<?> type, final Method method, final Class<?> returnType, final SpecificationLoader specificationLoader) {
        super(MemberType.ACTION, type, method, determineIdentifier(type, method), returnType, specificationLoader);
    }

    private static Identifier determineIdentifier(Class<?> type, Method method) {
        return Util.actionIdentifierFor(type, method);
    }

    
    // ////////////////////// Parameters ////////////////////


    @Override
    public ObjectActionParamPeer[] getParameters() {
        if (paramPeers == null) {
            paramPeers = Util.getParamPeers(getMethod(), getSpecificationLoader());
        }
        return paramPeers;
    }

    // ///////////////////////// toString, debugData /////////////////////////

    @Override
    public String toString() {
        final ActionInvocationFacet facet = getFacet(ActionInvocationFacet.class);
        final ObjectSpecification onType = facet.getOnType();
        return "JavaObjectActionPeer [name=" + getIdentifier().getMemberName() + ",type=" + onType.getShortName() + "]";
    }

}
