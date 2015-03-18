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

package org.apache.isis.applib.annotation;

import java.lang.annotation.*;

/**
 * Indicates that the class should be automatically recognized as a domain service.
 *
 * <p>
 * Also indicates whether the domain service acts as a repository for an entity, and menu ordering UI hints.
 * </p>
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainService {

    /**
     * If this domain service acts as a repository for an entity type, specify that entity type.
     */
    Class<?> repositoryFor() default Object.class;

    /**
     * The nature of this service, eg for menus, contributed actions, repository.
     */
    NatureOfService nature() default NatureOfService.VIEW;

    /**
     * Number in Dewey Decimal format representing the order.
     *
     * <p>
     * Same convention as {@link MemberOrder#sequence()}.  If not specified, placed after any named.
     * </p>
     *
     * @deprecated - use {@link DomainServiceLayout#menuOrder()} instead.
     */
    @Deprecated
    String menuOrder() default "" + Integer.MAX_VALUE;


}
