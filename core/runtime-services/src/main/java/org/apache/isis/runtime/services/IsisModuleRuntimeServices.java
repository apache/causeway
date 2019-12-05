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
package org.apache.isis.runtime.services;

import org.apache.isis.codegen.bytebuddy.IsisModuleCodegenByteBuddy;
import org.apache.isis.runtime.IsisModuleRuntime;
import org.apache.isis.runtime.services.auth.AuthenticationSessionProviderDefault;
import org.apache.isis.runtime.services.auth.AuthorizationManagerStandard;
import org.apache.isis.runtime.services.background.CommandExecutorServiceDefault;
import org.apache.isis.runtime.services.bookmarks.BookmarkServiceInternalDefault;
import org.apache.isis.runtime.services.command.CommandDtoServiceInternalDefault;
import org.apache.isis.runtime.services.command.CommandServiceDefault;
import org.apache.isis.runtime.services.confmenu.ConfigurationViewServiceDefault;
import org.apache.isis.runtime.services.email.EmailServiceDefault;
import org.apache.isis.runtime.services.eventbus.EventBusServiceSpring;
import org.apache.isis.runtime.services.factory.FactoryServiceDefault;
import org.apache.isis.runtime.services.homepage.HomePageResolverServiceDefault;
import org.apache.isis.runtime.services.i18n.po.TranslationServicePo;
import org.apache.isis.runtime.services.i18n.po.TranslationServicePoMenu;
import org.apache.isis.runtime.services.ixn.InteractionDtoServiceInternalDefault;
import org.apache.isis.runtime.services.menubars.MenuBarsLoaderServiceDefault;
import org.apache.isis.runtime.services.menubars.bootstrap3.MenuBarsServiceBS3;
import org.apache.isis.runtime.services.message.MessageServiceDefault;
import org.apache.isis.runtime.services.publish.PublishingServiceInternalDefault;
import org.apache.isis.runtime.services.repository.RepositoryServiceDefault;
import org.apache.isis.runtime.services.routing.RoutingServiceDefault;
import org.apache.isis.runtime.services.sessmgmt.SessionManagementServiceDefault;
import org.apache.isis.runtime.services.sudo.SudoServiceDefault;
import org.apache.isis.runtime.services.userprof.UserProfileServiceDefault;
import org.apache.isis.runtime.services.userreg.EmailNotificationServiceDefault;
import org.apache.isis.runtime.services.wrapper.WrapperFactoryDefault;
import org.apache.isis.runtime.services.xactn.TransactionServiceSpring;
import org.apache.isis.runtime.services.xmlsnapshot.XmlSnapshotServiceDefault;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        // modules
        IsisModuleRuntime.class,
        IsisModuleCodegenByteBuddy.class,

        // @Service's
        AuthenticationSessionProviderDefault.class,
        AuthorizationManagerStandard.class,
        CommandExecutorServiceDefault.class,
        BookmarkServiceInternalDefault.class,
        CommandServiceDefault.class,
        EventBusServiceSpring.class,
        FactoryServiceDefault.class,
        HomePageResolverServiceDefault.class,
        TranslationServicePo.class,
        MenuBarsLoaderServiceDefault.class,
        MenuBarsServiceBS3.class,
        MessageServiceDefault.class,
        SessionManagementServiceDefault.class,
        WrapperFactoryDefault.class,
        TransactionServiceSpring.class,
        XmlSnapshotServiceDefault.class,

        // @Controller
        RoutingServiceDefault.class,

        // @Repository's
        RepositoryServiceDefault.class,

        // @DomainService's
        CommandDtoServiceInternalDefault.class,
        ConfigurationViewServiceDefault.class,
        EmailServiceDefault.class,
        TranslationServicePoMenu.class,
        InteractionDtoServiceInternalDefault.class,
        PublishingServiceInternalDefault.class,
        SudoServiceDefault.class,
        UserProfileServiceDefault.class,
        EmailNotificationServiceDefault.class,
})
public class IsisModuleRuntimeServices {

}
