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

/**
 * THIS STUFF ALL WORKS, BUT REMOVED FROM PUBLIC APPLIB BECAUSE IT LOOKS
 * LIKE THE BOOTSTRAP3 GRID WILL BE SUFFICIENT.
 *
 * The benefit of keeping this stuff around is that it reinforces where
 * separation of responsibilities are for grid components (tab etc)
 * vs common components (fieldset, action, property, collection, domainobject).
 *
 * --------------------
 *
 * The classes in this package define how to layout the properties, collections and actions of a domain object - the
 * building blocks - as defined in the <code>members.v1</code> package.
 *
 * <p>
 *     The layout is reasonably flexible, being a half-way house between the annotation/JSON style layouts (pre 1.12.0)
 *     vs the fully flexible layouts provided by the <code>bootstrap3</code> layouts.  In particular, they allow
 *     property fieldsets and collections to be grouped into tabs, with collections being laid out in any column.
 *     However, tab groups only appear in the central area of the page, and may only have a maximum of three columns.
 * </p>
 */
@javax.xml.bind.annotation.XmlSchema(
        namespace = "http://isis.apache.org/schema/applib/layout/fixedcols",
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED
)
package org.apache.isis.core.metamodel.services.grid.fixedcols.applib;