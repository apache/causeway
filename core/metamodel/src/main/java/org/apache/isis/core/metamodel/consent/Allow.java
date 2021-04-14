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

package org.apache.isis.core.metamodel.consent;

import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * An instance of this type is used to allow something.
 */
public class Allow extends ConsentAbstract {

    private static final long serialVersionUID = 1L;

    public static final Allow DEFAULT = new Allow();

    private Allow() {
        this((String) null);
    }

    /**
     * Called by DnD viewer; we should instead find a way to put the calling
     * logic into {@link Facet}s so that it is available for use by other
     * viewers.
     *
     * @see Veto
     * @deprecated
     */
    @Deprecated
    public Allow(final String description) {
        super(description, null);
    }

    public Allow(final InteractionResult interactionResult) {
        super(interactionResult);
    }

}
