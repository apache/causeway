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
package org.apache.isis.core.metamodel.facets.object.logicaltype;


import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.metamodel.facetapi.Facet;


/**
 *  Corresponds to the value of {@link DomainObject#logicalTypeName()},
 *  that specifies the <i>logical type name</i> of a domain object.
 *  <p>
 *  The <i>logical type name</i> must be unique among non-abstract classes,
 *  but is allowed to be shared with interfaces and abstract classes.
 */
public interface LogicalTypeFacet extends Facet {

    LogicalType getLogicalType();

    default String value() {
        return getLogicalType().getLogicalTypeName();
    }

}
