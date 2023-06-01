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
package demoapp.dom.domain.objects.DomainObjectLayout.xxxUiEvent;

import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.events.ui.CssClassUiEvent;
import org.apache.causeway.applib.events.ui.IconUiEvent;
import org.apache.causeway.applib.events.ui.LayoutUiEvent;
import org.apache.causeway.applib.events.ui.TitleUiEvent;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;

//tag::class[]
@DomainObjectLayout(
    titleUiEvent = DomainObjectLayoutXxxUiEventEntity.TitleEvent.class,       // <.>
    iconUiEvent = DomainObjectLayoutXxxUiEventEntity.IconEvent.class,         // <.>
    cssClassUiEvent = DomainObjectLayoutXxxUiEventEntity.CssClassEvent.class, // <.>
    layoutUiEvent = DomainObjectLayoutXxxUiEventEntity.LayoutEvent.class      // <.>
)
public abstract class DomainObjectLayoutXxxUiEventEntity
//end::class[]
        implements
        HasAsciiDocDescription,
        ValueHolder<String>
//tag::class[]
{
    public static class TitleEvent                                            // <1>
            extends TitleUiEvent<DomainObjectLayoutXxxUiEventEntity> { }
    public static class IconEvent                                             // <2>
            extends IconUiEvent<DomainObjectLayoutXxxUiEventEntity> { }
    public static class CssClassEvent                                         // <3>
            extends CssClassUiEvent<DomainObjectLayoutXxxUiEventEntity> { }
    public static class LayoutEvent                                           // <4>
            extends LayoutUiEvent<DomainObjectLayoutXxxUiEventEntity> { }
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

//tag::class[]
}
//end::class[]
