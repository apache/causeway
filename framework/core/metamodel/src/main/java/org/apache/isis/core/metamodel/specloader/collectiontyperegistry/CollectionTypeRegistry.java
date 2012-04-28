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

package org.apache.isis.core.metamodel.specloader.collectiontyperegistry;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.metamodel.spec.feature.OneToManyFeature.CollectionSemantics;

/**
 * Defines the types which are considered to be collections.
 * 
 * <p>
 * In this way there are similarities with the way in which value types are
 * specified using <tt>@Value</tt>. However, we need to maintain a repository of
 * these collection types once nominated so that when we introspect classes we
 * look for collections first, and then properties second.
 * 
 * <p>
 * TODO: plan is to allow new collection types to be installed dynamically,
 * allowing the domain programmer to declare custom classes to have collection
 * semantics.
 */
public interface CollectionTypeRegistry extends Injectable, ApplicationScopedComponent {

    public boolean isCollectionType(Class<?> cls);

    public boolean isArrayType(Class<?> cls);

    public Class<?>[] getCollectionType();

    public CollectionSemantics semanticsOf(Class<?> cls);

}
