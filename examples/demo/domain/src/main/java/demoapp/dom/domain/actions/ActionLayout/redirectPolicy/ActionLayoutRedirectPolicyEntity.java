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
package demoapp.dom.domain.actions.ActionLayout.redirectPolicy;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Repainting;
import org.apache.causeway.applib.services.clock.ClockService;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

//tag::class[]
public abstract class ActionLayoutRedirectPolicyEntity
//end::class[]
        implements
        HasAsciiDocDescription,
        ValueHolder<String>
//tag::class[]
{
    // ...
//end::class[]

    public String title() {
        return value();
    }

    @Override
    public String value() {
        return getName();
    }

    public abstract String getName();
    public abstract void setName(String value);

    public abstract Integer getCount();
    public abstract void setCount(Integer value);

    @SneakyThrows
    @Property
    @PropertyLayout(repainting = Repainting.NO_REPAINT) // <.>
    public String getCurrentTime() {
        Thread.sleep(1000);
        return clockService.getClock().nowAsLocalDateTime().toString();
    }

    @Inject private ClockService clockService;

//tag::class[]
}
//end::class[]