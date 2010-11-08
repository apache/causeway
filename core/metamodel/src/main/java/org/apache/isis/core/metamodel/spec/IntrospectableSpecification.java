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

import org.apache.isis.core.metamodel.facetdecorator.FacetDecoratorSet;


/**
 * In effect the SPI for {@link ObjectSpecification}.
 */
public interface IntrospectableSpecification {

    /**
     * Discovers what attributes and behaviour the type specified by this specification. 
     * 
     * <p>
     * As specifications are cyclic (specifically a class will reference its subclasses, which in turn reference their superclass)
     * they need be created first, and then later work out its internals. This allows for cyclic references to
     * the be accommodated as there should always a specification available even though it might not be
     * complete.
     */
    public void introspect(FacetDecoratorSet decorator);

    public void markAsService();

	public boolean isIntrospected();

}

