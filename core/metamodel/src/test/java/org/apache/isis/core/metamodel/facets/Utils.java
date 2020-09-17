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

package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Method;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.core.metamodel.facetapi.FeatureType;

class Utils {

    static DomainEventHelper domainEventHelper() {
        return DomainEventHelper.ofEventService(null);
    }

    protected static boolean contains(final Class<?>[] array, final Class<?> val) {
        for (final Class<?> element : array) {
            if (element == val) {
                return true;
            }
        }
        return false;
    }

    protected static boolean contains(ImmutableEnumSet<FeatureType> featureTypes, final FeatureType featureType) {
        if(featureTypes==null || featureType==null) {
            return false;
        }
        return featureTypes.contains(featureType);
    }

    protected static Method findMethod(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
        try {
            return type.getMethod(methodName, methodTypes);
        } catch (final SecurityException e) {
            return null;
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    protected static Method findMethod(final Class<?> type, final String methodName) {
        return findMethod(type, methodName, _Constants.emptyClasses);
    }

}
