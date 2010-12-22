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


package org.apache.isis.core.metamodel.specloader.internal.cache;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;


/**
 * This is not API.
 * 
 * <p>
 * In an earlier version it was possible to inject the {@link SpecificationCache} into the 
 * {@link ObjectReflectorAbstract reflector}.  This was needed when the reflector was
 * original (what is now called) {@link SessionScopedComponent session scoped}, rather than 
 * {@link ApplicationScopedComponent application-scoped}.
 *
 * <p>
 * This interface has been left in for now, but will likely be removed.
 */
public interface SpecificationCache {

    /**
     * Returns the {@link ObjectSpecification}, or possibly <tt>null</tt> if has not
     * been cached.
     */
    ObjectSpecification get(String className);

    ObjectSpecification[] allSpecifications();

    void cache(String className, ObjectSpecification spec);

    void clear();

}
