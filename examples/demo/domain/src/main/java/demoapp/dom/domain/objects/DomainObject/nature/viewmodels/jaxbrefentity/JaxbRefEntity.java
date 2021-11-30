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
package demoapp.dom.domain.objects.DomainObject.nature.viewmodels.jaxbrefentity;

import java.util.Objects;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Property;

import demoapp.dom._infra.values.ValueHolder;

@DomainObject(
        logicalTypeName = "demo.JaxbRefEntity" // shared permissions with concrete sub class
)
public abstract class JaxbRefEntity
implements
    ValueHolder<String> {

    @Override
    public String value() {
        return getName();
    }

    @ObjectSupport
    public String title() {
        return Objects.requireNonNull(getName(), "most likely a serialization or re-attach issue");
    }

    @Property
    public abstract String getName();
    protected abstract void setName(String value);

}
