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

package org.apache.isis.core.metamodel.facets.object.icon;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * Icon for an object a class (for example, by placing an appropriately named image
 * file into a certain directory).
 *
 * <p>
 * The facet is checked each time the object is rendered, allowing the icon to be changed on an instance-by-instance basis. For
 * example, the icon might be adapted with an overlay to represent its state through some well-defined lifecycle (eg
 * pending approval, approved, rejected). Alternatively a {@link BoundedFacet bounded} class might have completely
 * different icons for its instances (eg Visa, Mastercard, Amex).
 *
 * <p>
 * In the standard Apache Isis Programming Model, typically corresponds to a method named {@code iconName}.
 *
 * @see TitleFacet
 * @see PluralFacet
 */
public interface IconFacet extends Facet {

    public String iconName(final ManagedObject object);
}
