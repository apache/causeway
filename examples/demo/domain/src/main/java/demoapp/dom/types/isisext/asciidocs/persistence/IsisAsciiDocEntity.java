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
package demoapp.dom.types.isisext.asciidocs.persistence;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;
import demoapp.dom.types.isisext.asciidocs.holder.IsisAsciiDocHolder2;

@DomainObject(
        logicalTypeName = "demo.IsisAsciiDocEntity" // shared permissions with concrete sub class
)
public abstract class IsisAsciiDocEntity
implements
    HasAsciiDocDescription,
    IsisAsciiDocHolder2,
    ValueHolder<AsciiDoc> {

    @Override
    public AsciiDoc value() {
        return getReadOnlyProperty();
    }

}
