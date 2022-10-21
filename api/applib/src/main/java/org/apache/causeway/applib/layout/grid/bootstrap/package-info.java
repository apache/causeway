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
 * The classes in this package define how to layout the properties, collections and actions of a domain object - the
 * building blocks - as defined in the <code>members.v1</code> package.
 *
 * <p>
 *     The layout is modelled closely after <a href="http://getbootstrap.com/">Bootstrap</a>, and is intended to
 *     support the grid layouts implemented by that CSS framework.  This flexibility comes at the cost of some
 *     verbosity.
 * </p>
 */
@javax.xml.bind.annotation.XmlSchema(
        namespace = "http://causeway.apache.org/applib/layout/grid/bootstrap3",
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED,
        xmlns = {
                @javax.xml.bind.annotation.XmlNs(
                        namespaceURI = "http://causeway.apache.org/applib/layout/grid/bootstrap3", prefix = "bs")
        })
package org.apache.causeway.applib.layout.grid.bootstrap;
