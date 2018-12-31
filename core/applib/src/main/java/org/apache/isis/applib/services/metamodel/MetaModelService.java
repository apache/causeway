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
package org.apache.isis.applib.services.metamodel;

import java.util.List;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * This service provides a formal API into Isis' metamodel.
 *
 * <p>
 * This API is currently extremely limited, but the intention is to extend it gradually as use cases emerge.
 * </p>
 */
public interface MetaModelService {

    /**
     * Provides a reverse lookup of a domain class' object type, as defined by {@link DomainObject#objectType()} or the (now-deprecated) {@link org.apache.isis.applib.annotation.ObjectType} annotation, or any other mechanism that corresponds to Isis' <code>ObjectSpecIdFacet</code>.
     */
    @Programmatic
    Class<?> fromObjectType(final String objectType);

    /**
     * Provides a lookup of a domain class' object type, as defined by {@link DomainObject#objectType()} or the (now-deprecated) {@link org.apache.isis.applib.annotation.ObjectType} annotation, or any other mechanism that corresponds to Isis' <code>ObjectSpecIdFacet</code>.
     */
    @Programmatic
    String toObjectType(final Class<?> domainType);


    @Programmatic
    void rebuild(final Class<?> domainType);

    /**
     * Returns a list of representations of each of member of each domain class.
     *
     * <p>
     *     Used by {@link MetaModelServicesMenu} to return a downloadable CSV.
     * </p>
     *
     * <p>
     *     Note that {@link MetaModelService6#exportMetaModel(MetaModelService6.Config)} provides a superset of the functionality provided by this method.
     * </p>
     *
     * @see MetaModelService6
     */
    @Programmatic
    List<DomainMember> export();


}
