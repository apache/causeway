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
@javax.xml.bind.annotation.XmlSchema(
        namespace = "http://isis.apache.org/schema/applib/layout",
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED
        // specifying the location seems to cause JaxbService#toXsd() to not generate the schema; not sure why...
        //, location = "http://isis.apache.org/schema/metamodel/layout/layout-1.0.xsd"
)
package org.apache.isis.applib.layout.v1_0;