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
 * The {@link org.apache.isis.applib.mixins.dto.Dto} role interface is intended to be implemented by JAXB-annotated
 * view models, that is, annotated using <code>@XmlRootElement</code>. It enables the ability to download the XML and
 * XSD schema of those objects using two mixins, {@link org.apache.isis.applib.mixins.dto.Dto_downloadXml} and
 * {@link org.apache.isis.applib.mixins.dto.Dto_downloadXsd}.
 *
 * @see <a href="http://isis.apache.org/guides/rgcms/rgcms.html#_rgcms_classes_mixins_Dto">Classes, Methods &amp; Schema guide</a>
 */
package org.apache.isis.applib.mixins.dto;