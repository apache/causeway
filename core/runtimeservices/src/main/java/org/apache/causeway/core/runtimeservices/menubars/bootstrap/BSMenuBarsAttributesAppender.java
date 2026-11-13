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
package org.apache.causeway.core.runtimeservices.menubars.bootstrap;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBars;

record BSMenuBarsAttributesAppender() {

    static final String COMPONENT_TNS = "https://causeway.apache.org/applib/layout/component";
    static final String COMPONENT_SCHEMA_LOCATION = "https://causeway.apache.org/applib/layout/component/component.xsd";

    static final String LINKS_TNS = "https://causeway.apache.org/applib/layout/links";
    static final String LINKS_SCHEMA_LOCATION = "https://causeway.apache.org/applib/layout/links/links.xsd";

    static final String MB3_TNS = "https://causeway.apache.org/applib/layout/menubars/bootstrap3";
    static final String MB3_SCHEMA_LOCATION = "https://causeway.apache.org/applib/layout/menubars/bootstrap3/menubars.xsd";

    BSMenuBars appendAttributes(final BSMenuBars bsMenuBars) {
        bsMenuBars.setTnsAndSchemaLocation(tnsAndSchemaLocation());
        //bsMenuBars.attributes().put(Marshaller.JAXB_SCHEMA_LOCATION, tnsAndSchemaLocation());
        return bsMenuBars;
    }

    private String tnsAndSchemaLocation() {
        return Stream.of(
                MB3_TNS,
                MB3_SCHEMA_LOCATION,

                COMPONENT_TNS,
                COMPONENT_SCHEMA_LOCATION,

                LINKS_TNS,
                LINKS_SCHEMA_LOCATION
                )
                .collect(Collectors.joining(" "));
    }

}
