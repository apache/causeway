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


package org.apache.isis.core.metamodel.spec;



/**
 * Discovers what attributes and behaviour the type specified by this 
 * specification (in effect the SPI for {@link ObjectSpecification}).
 * 
 * <p>
 * As specifications are cyclic (specifically a class will reference its 
 * subclasses, which in turn reference their superclass) they need be created 
 * first, and then later work out its internals.  Hence we create 
 * {@link ObjectSpecification}s as we need them, and then introspect them later.
 */
public interface IntrospectableSpecification extends ObjectSpecification {

    /**
     * Builds actions and associations.
     * 
     * <p>
     * Is called prior to running the <tt>FacetDecoratorSet</tt>
     */
    public void introspectTypeHierarchyAndMembers();

    /**
     * Is called after to running the <tt>FacetDecoratorSet</tt>.
     * 
     * <p>
     * TODO: it's possible that this could be merged with {@link #introspectTypeHierarchyAndMembers()};
     * need to check though, because this would cause facets to be decorated at the end of
     * introspection, rather than midway as is currently.
     * 
     */
    public void completeIntrospection();

    public void markAsService();

	public boolean isIntrospected();

}

