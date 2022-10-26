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
 * The classes in this package provide layout metadata for a domain object's properties, collections and actions - the
 * &quot;building blocks&quot; which then must be arranged into some sort of layout.
 *
 * <p>
 *     The <code>bootstrap3</code> and <code>fixedcols</code> packages both provide different ways of doing the layout,
 *     and both reference the classes in this package.
 * </p>
 *
 */
@javax.xml.bind.annotation.XmlSchema(
        namespace = "http://causeway.apache.org/applib/layout/menubars/bootstrap3",
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED,
        xmlns = {
                @javax.xml.bind.annotation.XmlNs(
                        namespaceURI = "http://causeway.apache.org/applib/layout/menubars/bootstrap3", prefix = "mb3")
        }
        )
package org.apache.causeway.applib.layout.menubars.bootstrap;
