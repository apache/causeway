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
package org.apache.isis.viewer.restfulobjects.rendering.service.swagger;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.DomainService;
import org.apache.isis.applib.annotations.DomainServiceLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.annotations.Programmatic;
import org.apache.isis.applib.annotations.RestrictTo;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.swagger.Format;
import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.applib.services.swagger.Visibility;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.RestEasyConfiguration;
import org.apache.isis.viewer.restfulobjects.rendering.IsisModuleRestfulObjectsRendering;

import lombok.val;


/**
 * @since 1.x {@index}
 */
@Named(SwaggerServiceMenu.LOGICAL_TYPE_NAME)
@DomainService(logicalTypeName = SwaggerServiceMenu.LOGICAL_TYPE_NAME)
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class SwaggerServiceMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleRestfulObjectsRendering.NAMESPACE +  ".SwaggerServiceMenu";

    private final SwaggerService swaggerService;
    private final ServiceRegistry serviceRegistry;
    private final RestEasyConfiguration restEasyConfiguration;
    private final String basePath;

    @Inject
    public SwaggerServiceMenu(
            final SwaggerService swaggerService,
            final ServiceRegistry serviceRegistry,
            final RestEasyConfiguration restEasyConfiguration) {
        this.swaggerService = swaggerService;
        this.serviceRegistry = serviceRegistry;
        this.restEasyConfiguration = restEasyConfiguration;
        this.basePath = this.restEasyConfiguration.getJaxrs().getDefaultPath() + "/";
    }

    public static abstract class ActionDomainEvent<T> extends IsisModuleApplib.ActionDomainEvent<T> { }

    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = openSwaggerUi.ActionEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-external-link-alt",
            sequence="500.600.1")
    public class openSwaggerUi {
        public class ActionEvent extends ActionDomainEvent<openSwaggerUi> { }

        @MemberSupport public LocalResourcePath act() {
            return new LocalResourcePath("/swagger-ui/index.thtml");
        }

        @MemberSupport public String disableAct() { return disableReasonWhenRequiresROViewer(); }
    }




    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = openRestApi.ActionEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-external-link-alt",
            sequence="500.600.2")
    public class openRestApi {

        public class ActionEvent extends ActionDomainEvent<openRestApi> { }

        @MemberSupport public LocalResourcePath act() {
            return new LocalResourcePath(basePath);
        }

        @MemberSupport public String disableAct() { return disableReasonWhenRequiresROViewer(); }

    }



    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = downloadSwaggerSchemaDefinition.ActionEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            sequence="500.600.3")

    public class downloadSwaggerSchemaDefinition {

        public class ActionEvent extends ActionDomainEvent<downloadSwaggerSchemaDefinition> { }

        @MemberSupport public Clob act(
                @ParameterLayout(named = "Filename")
                final String fileNamePrefix,
                final Visibility visibility,
                final Format format) {

            val fileName = buildFileName(fileNamePrefix, visibility, format);
            val spec = swaggerService.generateSwaggerSpec(visibility, format);
            return new Clob(fileName, format.mediaType(), spec);
        }

        @MemberSupport public String default0Act() { return "swagger"; }
        @MemberSupport public Visibility default1Act() { return Visibility.PRIVATE; }
        @MemberSupport public Format default2Act() { return Format.YAML; }
    }


    // -- HELPER
    @Programmatic String disableReasonWhenRequiresROViewer() {
        final Optional<?> moduleIfAny = serviceRegistry
                .lookupBeanById("isis.viewer.ro.WebModuleJaxrsRestEasy4");
        return moduleIfAny.isPresent()
                ? null
                : "RestfulObjects viewer is not configured";
    }

    private static String buildFileName(
            String fileNamePrefix,
            final Visibility visibility,
            final Format format) {
        final String formatLower = format.name().toLowerCase();
        int i = fileNamePrefix.lastIndexOf("." + formatLower);
        if(i > 0) {
            fileNamePrefix = fileNamePrefix.substring(0, i);
        }
        return _Strings.asFileNameWithExtension(
                fileNamePrefix + "-" + visibility.name().toLowerCase(),
                formatLower);
    }

}
