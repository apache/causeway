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
package org.apache.isis.applib.mixins.dto;

/**
 * Allows JAXB-annotated view models to act as a mixee in order that other
 * modules (and the core framework) can contribute behaviour.
 *
 * <p>
 *     A JAXB view model is one annotated with
 *     {@link javax.xml.bind.annotation.XmlRootElement}.
 * </p>
 *
 * <p>
 *     The two mixin behaviours contributed by the core framework are the
 *     ability to download the view model as XML (using {@link Dto_downloadXml})
 *     and to download the XSD schema for that XML (using
 *     {@link Dto_downloadXsd}).
 * </p>
 *
 * <p>
 * The interface is just a marker interface (with no members).
 * </p>
 *
 * @see Dto_downloadXml
 * @see Dto_downloadXsd
 *
 * @since 1.x {@index}
 */
public interface Dto {

}
