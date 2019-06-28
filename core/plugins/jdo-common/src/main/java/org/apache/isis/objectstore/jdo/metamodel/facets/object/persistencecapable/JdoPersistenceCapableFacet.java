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
package org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable;


import javax.jdo.annotations.IdentityType;

import org.apache.isis.metamodel.facets.object.entity.EntityFacet;


/**
 * Corresponds to annotating the class with the {@link javax.jdo.annotations.PersistenceCapable} annotation.
 */
public interface JdoPersistenceCapableFacet extends EntityFacet {

    IdentityType getIdentityType();

    /**
     * Corresponds to {@link javax.jdo.annotations.PersistenceCapable#schema()}, or null if not specified.
     */
    String getSchema();

    /**
     * Corresponds to {@link javax.jdo.annotations.PersistenceCapable#table()}, or to the
     * class' {@link Class#getSimpleName() simple name} if no table specified.
     */
    String getTable();

}
