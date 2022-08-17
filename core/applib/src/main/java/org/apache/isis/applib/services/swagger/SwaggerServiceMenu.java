/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.swagger;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Clob;


@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isisApplib.SwaggerServiceMenu"
)
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        menuOrder = "500.600"
)
public class SwaggerServiceMenu {

    public static abstract class ActionDomainEvent extends IsisApplibModule.ActionDomainEvent<SwaggerServiceMenu> {
    }


    public static class OpenSwaggerUiDomainEvent extends ActionDomainEvent {}

    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = OpenSwaggerUiDomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-external-link"
    )
    @MemberOrder(sequence="500.600.1")
    public URL openSwaggerUi() throws MalformedURLException {
        return new java.net.URL("http:///swagger-ui/index.html");
    }



    public static class OpenRestApiDomainEvent extends ActionDomainEvent {}

    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = OpenSwaggerUiDomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-external-link"
    )
    @MemberOrder(sequence="500.600.2")
    public URL openRestApi() throws MalformedURLException {
        return new java.net.URL("http:///restful/");
    }



    public static class DownloadSwaggerSpecDomainEvent extends ActionDomainEvent {}

    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = DownloadSwaggerSpecDomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-download"
    )
    @MemberOrder(sequence="500.600.3")
    public Clob downloadSwaggerSchemaDefinition(
            @ParameterLayout(named = "Filename")
            final String fileNamePrefix,
            final SwaggerService.Visibility visibility,
            final SwaggerService.Format format) {
        final String fileName = Util.buildFileName(fileNamePrefix, visibility, format);
        final String spec = swaggerService.generateSwaggerSpec(visibility, format);
        return new Clob(fileName, format.mediaType(), spec);
    }

    public String default0DownloadSwaggerSchemaDefinition() {
        return "swagger";
    }
    public SwaggerService.Visibility default1DownloadSwaggerSchemaDefinition() {
        return SwaggerService.Visibility.PRIVATE;
    }
    public SwaggerService.Format default2DownloadSwaggerSchemaDefinition() {
        return SwaggerService.Format.YAML;
    }



    @javax.inject.Inject
    SwaggerService swaggerService;
}
