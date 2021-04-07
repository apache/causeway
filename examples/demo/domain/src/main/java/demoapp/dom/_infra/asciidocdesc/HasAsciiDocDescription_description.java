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
package demoapp.dom._infra.asciidocdesc;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Snapshot;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import demoapp.dom._infra.resources.AsciiDocReaderService;
import lombok.RequiredArgsConstructor;

@Property(snapshot = Snapshot.EXCLUDED)
@RequiredArgsConstructor
public class HasAsciiDocDescription_description {

    private final HasAsciiDocDescription hasAsciiDocDescription;

    @PropertyLayout(labelPosition = LabelPosition.NONE, hidden = Where.ALL_TABLES, 
            group = "description", sequence = "1")
    public AsciiDoc prop() {
        return asciiDocReaderService.readFor(hasAsciiDocDescription, "description");
    }

    @Inject
    AsciiDocReaderService asciiDocReaderService;

}
