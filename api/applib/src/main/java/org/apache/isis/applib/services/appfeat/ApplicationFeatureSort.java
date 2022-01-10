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
package org.apache.isis.applib.services.appfeat;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Value;

/**
 *
 * @since 1.x revised for 2.0 {@index}
 */
@Value(logicalTypeName = IsisModuleApplib.NAMESPACE_FEAT + ".ApplicationFeatureSort")
public enum ApplicationFeatureSort {

    /**
     * logical namespace
     */
    NAMESPACE,

    /**
     * {@code namespace + "." + typeSimpleName}
     * makes up the fully qualified logical type
     */
    TYPE,

    /**
     * {@code namespace + "." + typeSimpleName + "." + memberName}
     * makes up the fully qualified logical member
     */
    MEMBER;

    public boolean isNamespace() {
        return this == ApplicationFeatureSort.NAMESPACE;
    }

    public boolean isType() {
        return this == ApplicationFeatureSort.TYPE;
    }

    public boolean isMember() {
        return this == ApplicationFeatureSort.MEMBER;
    }

    @Override
    public String toString() {
        return name();
    }


}
