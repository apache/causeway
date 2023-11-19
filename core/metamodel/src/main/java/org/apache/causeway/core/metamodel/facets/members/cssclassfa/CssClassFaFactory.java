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
package org.apache.causeway.core.metamodel.facets.members.cssclassfa;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;

/**
 * @since 2.0
 */
public interface CssClassFaFactory {

    /**
     * Position of <i>Font Awesome</i> icon.
     */
    CssClassFaPosition getPosition();

    /**
     * Creates the model object that is used for FA icon rendering.
     */
    FontAwesomeLayers getLayers();

    /**
     * @implNote because {@link CssClassFaStaticFacetAbstract} has all the fa-icon logic,
     * we simply reuse it here by creating an anonymous instance
     */
    public static CssClassFaFactory ofIconAndPosition(final String faIcon, final CssClassFaPosition position) {
        return new CssClassFaStaticFacetAbstract(
                faIcon, position, null) {};
    }

}
