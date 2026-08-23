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
package demoapp.dom.domain._interactions;

import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.services.urlencoding.UrlEncodingService;
import org.apache.causeway.applib.util.TitleBuffer;
import org.apache.causeway.applib.util.schema.InteractionDtoUtils;
import org.apache.causeway.core.runtimeservices.urlencoding.UrlEncodingServiceWithCompression;
import org.apache.causeway.schema.ixn.v2.InteractionDto;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//tag::class[]
@Named("demo.InteractionDtoVm")
@DomainObject(
        nature = Nature.VIEW_MODEL)
@NoArgsConstructor
@AllArgsConstructor
public class InteractionDtoVm implements ViewModel, HasAsciiDocDescription {

    private final static UrlEncodingService encodingService = new UrlEncodingServiceWithCompression();

    @ObjectSupport public String title() {
        // nb: not thread-safe
        // formats defined in https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        var format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        var exec = getInteractionDto().getExecution();
        var instant = exec.getMetrics().getTimings().getStartedAt().toGregorianCalendar().toInstant();

        var buf = new TitleBuffer();
        buf.append(format.format(Date.from(instant)));
        buf.append(" ").append(exec.getLogicalMemberIdentifier());
        return buf.toString();
    }

    @Property
    @PropertyLayout(labelPosition = LabelPosition.NONE)
    @Getter @Setter
    private InteractionDto interactionDto;

    // -- VIEWMODEL CONTRACT

    @Inject
    public InteractionDtoVm(final String memento) {
        interactionDto = InteractionDtoUtils.dtoMapper().read(encodingService.decodeToString(memento));
    }

    @Override
    public String viewModelMemento() {
        return encodingService.encodeString(InteractionDtoUtils.dtoMapper().toString(interactionDto));
    }

}
//end::class[]
