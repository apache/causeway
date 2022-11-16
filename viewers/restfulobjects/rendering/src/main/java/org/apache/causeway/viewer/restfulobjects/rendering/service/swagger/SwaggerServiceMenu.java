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
package org.apache.causeway.viewer.restfulobjects.rendering.service.swagger;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.swagger.Format;
import org.apache.causeway.applib.services.swagger.SwaggerService;
import org.apache.causeway.applib.services.swagger.Visibility;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.RestEasyConfiguration;
import org.apache.causeway.viewer.restfulobjects.rendering.CausewayModuleRestfulObjectsRendering;

import lombok.val;


/**
 * @since 1.x {@index}
 */
@Named(SwaggerServiceMenu.LOGICAL_TYPE_NAME)
@DomainService()
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class SwaggerServiceMenu {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleRestfulObjectsRendering.NAMESPACE +  ".SwaggerServiceMenu";

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

    public static abstract class ActionDomainEvent<T> extends CausewayModuleApplib.ActionDomainEvent<T> { }

    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = openSwaggerUi.ActionDomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-external-link-alt",
            sequence="500.600.1")
    public class openSwaggerUi {

        public class ActionDomainEvent extends SwaggerServiceMenu.ActionDomainEvent<openSwaggerUi> { }

        @MemberSupport public LocalResourcePath act() {
            return new LocalResourcePath("/swagger-ui/index.thtml");
        }

        @MemberSupport public String disableAct() { return disableReasonWhenRequiresROViewer(); }
    }




    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = openRestApi.ActionDomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-external-link-alt",
            sequence="500.600.2")
    public class openRestApi {

        public class ActionDomainEvent extends SwaggerServiceMenu.ActionDomainEvent<openRestApi> { }

        @MemberSupport public LocalResourcePath act() {
            return new LocalResourcePath(basePath);
        }

        @MemberSupport public String disableAct() { return disableReasonWhenRequiresROViewer(); }

    }



    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = downloadSwaggerSchemaDefinition.ActionDomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            sequence="500.600.3")

    public class downloadSwaggerSchemaDefinition {

        public class ActionDomainEvent extends SwaggerServiceMenu.ActionDomainEvent<downloadSwaggerSchemaDefinition> { }

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
                .lookupBeanById("causeway.viewer.ro.WebModuleJaxrsRestEasy");
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
