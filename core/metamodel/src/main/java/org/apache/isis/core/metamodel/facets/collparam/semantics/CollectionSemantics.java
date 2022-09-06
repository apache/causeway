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
package org.apache.isis.core.metamodel.facets.collparam.semantics;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;

public interface CollectionSemantics {

    // -- FACTORIES

    static CollectionSemantics forMethodReturn(final Method method) {
        return create(method.getReturnType());
    }

    static CollectionSemantics forParameter(final Parameter param) {
        return create(param.getType());
    }

    static CollectionSemantics other() {
        return new CollectionSemantics() {
            @Override public boolean isSupportedInterfaceForActionParameters() {return false;}
            @Override public boolean isInheritedFromSet() {return false;}};
    }

    boolean isSupportedInterfaceForActionParameters();
    boolean isInheritedFromSet();

    // -- HELPER

    private static CollectionSemantics create(
            final Class<?> containerClass) {
        return ProgrammingModelConstants.CollectionType.valueOf(containerClass)
                .<CollectionSemantics>map(collectionType->create(containerClass, collectionType))
                .orElseGet(CollectionSemantics::other);
    }

    private static CollectionSemantics create(
            final Class<?> containerClass,
            final ProgrammingModelConstants.CollectionType collectionType) {
        return new CollectionSemantics() {
            @Override public boolean isSupportedInterfaceForActionParameters() {
                return collectionType.getContainerType().equals(containerClass);
            }
            @Override public boolean isInheritedFromSet() {
                return collectionType.isSetAny();
            }};
    }

}
