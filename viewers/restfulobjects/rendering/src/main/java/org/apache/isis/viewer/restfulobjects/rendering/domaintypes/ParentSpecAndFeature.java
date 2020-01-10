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
package org.apache.isis.viewer.restfulobjects.rendering.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;

public abstract class ParentSpecAndFeature<T extends ObjectFeature> {

    private final ObjectSpecification parentSpec;
    private final T objectFeature;

    public ParentSpecAndFeature(final ObjectSpecification objectSpecification, final T objectFeature) {
        this.parentSpec = objectSpecification;
        this.objectFeature = objectFeature;
    }

    public ObjectSpecification getParentSpec() {
        return parentSpec;
    }

    public T getObjectFeature() {
        return objectFeature;
    }

}