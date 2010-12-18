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
package org.apache.isis.core.metamodel.spec.identifier;

import java.lang.reflect.Method;
import java.util.List;

import com.google.inject.internal.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.FeatureType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.internal.peer.TypedHolder;
import org.apache.isis.core.metamodel.specloader.internal.peer.TypedHolderImpl;

public class Util {

    public static List<TypedHolder> getParamPeers(final Method actionMethod, SpecificationLoader specificationLoader) {
        final Class<?>[] parameterTypes = actionMethod.getParameterTypes();
        final int numParameters = parameterTypes.length;
        final List<TypedHolder> actionParams = Lists.newArrayList();
        for (int j = 0; j < numParameters; j++) {
            actionParams.add(new TypedHolderImpl(FeatureType.ACTION_PARAMETER, parameterTypes[j]));
        }
        return actionParams;
    }

    public static boolean isAllParamTypesValid(final Method actionMethod, SpecificationLoader specificationLoader) {
        final Class<?>[] parameterTypes = actionMethod.getParameterTypes();
        final int numParameters = parameterTypes.length;
    
        for (int j = 0; j < numParameters; j++) {
            ObjectSpecification paramSpec = specificationLoader.loadSpecification(parameterTypes[j]);
            if (paramSpec == null) {
                return false;
            }
        }
        return true;
    }

}
