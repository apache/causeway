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

package org.apache.isis.core.metamodel.facets.actions.layout;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaStaticFacetAbstract;

public class CssClassFaFacetForMenuBarXml
extends CssClassFaStaticFacetAbstract {

    public static Optional<CssClassFaFacet> create(
            final @Nullable ServiceActionLayoutData actionLayout,
            final FacetHolder holder) {

        return actionLayout != null
                ? _Strings.nonEmpty(actionLayout.getCssClassFa())
                        .map(cssClassFa->new CssClassFaFacetForMenuBarXml(
                                cssClassFa,
                                CssClassFaPosition.LEFT, // defaults to left
                                holder))
                : Optional.empty();
    }

    private CssClassFaFacetForMenuBarXml(
            final String value,
            final CssClassFaPosition position,
            final FacetHolder holder) {
        super(value, position, holder, Precedence.HIGH); // XML menu-bar entries overrule layout from annotations
    }

}
