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
package demoapp.dom.homepage;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.HomePage;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.services.scratchpad.Scratchpad;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import demoapp.dom._infra.resources.AsciiDocReaderService;
import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@DomainObject(
    nature=Nature.VIEW_MODEL,
    objectType = "demo.Homepage"
)
@HomePage                                                       // <.>
public class DemoHomePage
        implements HasAsciiDocDescription {                     // <.>

    public String title() {                                     // <.>
        return "Hello, " + userService.getUser().getName();
    }

    public AsciiDoc getWelcome() {                              // <.>
        return asciiDocReaderService.readFor(this, "welcome");
    }

    @Inject UserService userService;                            // <.>
    @Inject AsciiDocReaderService asciiDocReaderService;
}
//end::class[]
