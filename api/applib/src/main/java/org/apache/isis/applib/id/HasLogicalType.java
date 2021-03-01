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
package org.apache.isis.applib.id;

import org.apache.isis.applib.annotation.DomainObject;

/**
 * @since 2.0 {@index}
 */
public interface HasLogicalType {

    LogicalType getLogicalType();

    /**
     * Returns the (unique) logical-type-name, as per the {@link ObjectSpecIdFacet}.
     *
     * <p>
     * This will typically be the value of the {@link DomainObject#objectType()} annotation attribute.
     * If none has been specified then will default to the fully qualified class name (with
     * {@link ClassSubstitutorRegistry class name substituted} if necessary to allow for runtime 
     * bytecode enhancement.
     *
     * <p> 
     * The {@link ObjectSpecification} can be retrieved using 
     * {@link SpecificationLoader#lookupBySpecIdElseLoad(String)}} passing the logical-type-name as argument.
     */
    default String getLogicalTypeName() {
        return getLogicalType().getLogicalTypeName();
    }

}
