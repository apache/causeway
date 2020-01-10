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
package org.apache.isis.runtimeservices;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.codegen.bytebuddy.IsisModuleCodegenByteBuddy;
import org.apache.isis.runtime.IsisModuleRuntime;
import org.apache.isis.runtimeservices.auth.AuthenticationSessionProviderDefault;
import org.apache.isis.runtimeservices.bookmarks.BookmarkServiceInternalDefault;
import org.apache.isis.runtimeservices.command.CommandDtoServiceInternalDefault;
import org.apache.isis.runtimeservices.command.CommandExecutorServiceDefault;
import org.apache.isis.runtimeservices.command.CommandServiceDefault;
import org.apache.isis.runtimeservices.confmenu.ConfigurationViewServiceDefault;
import org.apache.isis.runtimeservices.email.EmailServiceDefault;
import org.apache.isis.runtimeservices.eventbus.EventBusServiceSpring;
import org.apache.isis.runtimeservices.factory.FactoryServiceDefault;
import org.apache.isis.runtimeservices.homepage.HomePageResolverServiceDefault;
import org.apache.isis.runtimeservices.i18n.po.TranslationServicePo;
import org.apache.isis.runtimeservices.i18n.po.TranslationServicePoMenu;
import org.apache.isis.runtimeservices.ixn.InteractionDtoServiceInternalDefault;
import org.apache.isis.runtimeservices.menubars.MenuBarsLoaderServiceDefault;
import org.apache.isis.runtimeservices.menubars.bootstrap3.MenuBarsServiceBS3;
import org.apache.isis.runtimeservices.message.MessageServiceDefault;
import org.apache.isis.runtimeservices.publish.PublisherDispatchServiceDefault;
import org.apache.isis.runtimeservices.repository.RepositoryServiceDefault;
import org.apache.isis.runtimeservices.routing.RoutingServiceDefault;
import org.apache.isis.runtimeservices.sessmgmt.SessionManagementServiceDefault;
import org.apache.isis.runtimeservices.sudo.SudoServiceDefault;
import org.apache.isis.runtimeservices.userprof.UserProfileServiceDefault;
import org.apache.isis.runtimeservices.userreg.EmailNotificationServiceDefault;
import org.apache.isis.runtimeservices.wrapper.WrapperFactoryDefault;
import org.apache.isis.runtimeservices.xactn.TransactionServiceSpring;
import org.apache.isis.runtimeservices.xmlsnapshot.XmlSnapshotServiceDefault;

@Configuration
@Import({
        // modules
        IsisModuleRuntime.class,
        IsisModuleCodegenByteBuddy.class,

        // @Service's
        AuthenticationSessionProviderDefault.class,
        BookmarkServiceInternalDefault.class,
        CommandDtoServiceInternalDefault.class,
        CommandExecutorServiceDefault.class,
        CommandServiceDefault.class,
        ConfigurationViewServiceDefault.class,
        EmailNotificationServiceDefault.class,
        EmailServiceDefault.class,
        EventBusServiceSpring.class,
        FactoryServiceDefault.class,
        HomePageResolverServiceDefault.class,
        InteractionDtoServiceInternalDefault.class,
        TranslationServicePo.class,
        MenuBarsLoaderServiceDefault.class,
        MenuBarsServiceBS3.class,
        MessageServiceDefault.class,
        PublisherDispatchServiceDefault.class,
        SessionManagementServiceDefault.class,
        SudoServiceDefault.class,
        TransactionServiceSpring.class,
        UserProfileServiceDefault.class,
        WrapperFactoryDefault.class,
        XmlSnapshotServiceDefault.class,

        // @Controller
        RoutingServiceDefault.class,

        // @Repository's
        RepositoryServiceDefault.class,

        // @DomainService's
        TranslationServicePoMenu.class,
})
public class IsisModuleRuntimeServices {

}
