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
package org.apache.causeway.core.runtimeservices;

import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import org.apache.causeway.core.codegen.bytebuddy.CausewayModuleCoreCodegenByteBuddy;
import org.apache.causeway.core.runtime.CausewayModuleCoreRuntime;
import org.apache.causeway.core.runtimeservices.bookmarks.BookmarkServiceDefault;
import org.apache.causeway.core.runtimeservices.command.CommandDtoFactoryDefault;
import org.apache.causeway.core.runtimeservices.command.CommandExecutorServiceDefault;
import org.apache.causeway.core.runtimeservices.command.SchemaValueMarshallerDefault;
import org.apache.causeway.core.runtimeservices.email.EmailServiceDefault;
import org.apache.causeway.core.runtimeservices.eventbus.EventBusServiceSpring;
import org.apache.causeway.core.runtimeservices.executor.MemberExecutorServiceDefault;
import org.apache.causeway.core.runtimeservices.factory.FactoryServiceDefault;
import org.apache.causeway.core.runtimeservices.homepage.HomePageResolverServiceDefault;
import org.apache.causeway.core.runtimeservices.i18n.po.TranslationServicePo;
import org.apache.causeway.core.runtimeservices.i18n.po.TranslationServicePoMenu;
import org.apache.causeway.core.runtimeservices.icons.ObjectIconServiceDefault;
import org.apache.causeway.core.runtimeservices.interaction.InteractionDtoFactoryDefault;
import org.apache.causeway.core.runtimeservices.jaxb.JaxbServiceDefault;
import org.apache.causeway.core.runtimeservices.locale.LanguageProviderDefault;
import org.apache.causeway.core.runtimeservices.locale.LocaleChoiceProviderDefault;
import org.apache.causeway.core.runtimeservices.menubars.MenuBarsLoaderServiceDefault;
import org.apache.causeway.core.runtimeservices.menubars.bootstrap.MenuBarsMarshallerServiceBootstrap;
import org.apache.causeway.core.runtimeservices.menubars.bootstrap.MenuBarsServiceBootstrap;
import org.apache.causeway.core.runtimeservices.message.MessageServiceDefault;
import org.apache.causeway.core.runtimeservices.placeholder.PlaceholderRenderServiceDefault;
import org.apache.causeway.core.runtimeservices.publish.CommandPublisherDefault;
import org.apache.causeway.core.runtimeservices.publish.EntityChangesPublisherDefault;
import org.apache.causeway.core.runtimeservices.publish.EntityPropertyChangePublisherDefault;
import org.apache.causeway.core.runtimeservices.publish.ExecutionPublisherDefault;
import org.apache.causeway.core.runtimeservices.publish.LifecycleCallbackNotifier;
import org.apache.causeway.core.runtimeservices.publish.ObjectLifecyclePublisherDefault;
import org.apache.causeway.core.runtimeservices.recognizer.ExceptionRecognizerServiceDefault;
import org.apache.causeway.core.runtimeservices.recognizer.dae.ExceptionRecognizerForDataAccessException;
import org.apache.causeway.core.runtimeservices.repository.RepositoryServiceDefault;
import org.apache.causeway.core.runtimeservices.routing.RoutingServiceDefault;
import org.apache.causeway.core.runtimeservices.scratchpad.ScratchpadDefault;
import org.apache.causeway.core.runtimeservices.serializing.SerializingAdapterDefault;
import org.apache.causeway.core.runtimeservices.session.InteractionIdGenerator;
import org.apache.causeway.core.runtimeservices.session.InteractionServiceDefault;
import org.apache.causeway.core.runtimeservices.sitemap.SitemapServiceDefault;
import org.apache.causeway.core.runtimeservices.spring.SpringBeansService;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;
import org.apache.causeway.core.runtimeservices.urlencoding.UrlEncodingServiceWithCompression;
import org.apache.causeway.core.runtimeservices.user.ImpersonateMenuAdvisorDefault;
import org.apache.causeway.core.runtimeservices.user.ImpersonatedUserHolderDefault;
import org.apache.causeway.core.runtimeservices.user.UserCurrentSessionTimeZoneHolderDefault;
import org.apache.causeway.core.runtimeservices.userreg.EmailNotificationServiceDefault;
import org.apache.causeway.core.runtimeservices.wrapper.WrapperFactoryDefault;
import org.apache.causeway.core.runtimeservices.xml.XmlServiceDefault;
import org.apache.causeway.core.runtimeservices.xmlsnapshot.XmlSnapshotServiceDefault;

@Configuration
@Import({
        // Modules
        CausewayModuleCoreRuntime.class,
        CausewayModuleCoreCodegenByteBuddy.class,

        // @Service's
        BookmarkServiceDefault.class,
        CommandDtoFactoryDefault.class,
        CommandExecutorServiceDefault.class,
        CommandPublisherDefault.class,
        EmailNotificationServiceDefault.class,
        EmailServiceDefault.class,
        EntityChangesPublisherDefault.class,
        EntityPropertyChangePublisherDefault.class,
        EventBusServiceSpring.class,
        ExceptionRecognizerServiceDefault.class,
        ExecutionPublisherDefault.class,
        FactoryServiceDefault.class,
        HomePageResolverServiceDefault.class,
        ImpersonateMenuAdvisorDefault.class,
        ImpersonatedUserHolderDefault.class,
        InteractionDtoFactoryDefault.class,
        InteractionIdGenerator.class,
        InteractionServiceDefault.class,
        JaxbServiceDefault.class,
        LanguageProviderDefault.class,
        LocaleChoiceProviderDefault.class,
        MemberExecutorServiceDefault.class,
        MenuBarsLoaderServiceDefault.class,
        MenuBarsMarshallerServiceBootstrap.class,
        MenuBarsServiceBootstrap.class,
        MessageServiceDefault.class,
        ObjectIconServiceDefault.class,
        ObjectLifecyclePublisherDefault.class,
        PlaceholderRenderServiceDefault.class,
        LifecycleCallbackNotifier.class,
        SchemaValueMarshallerDefault.class,
        ScratchpadDefault.class,
        SerializingAdapterDefault.class,
        SitemapServiceDefault.class,
        SpringBeansService.class,
        TransactionServiceSpring.class,
        TranslationServicePo.class,
        UrlEncodingServiceWithCompression.class,
        UserCurrentSessionTimeZoneHolderDefault.class,
        WrapperFactoryDefault.class,
        XmlServiceDefault.class,
        XmlSnapshotServiceDefault.class,

        // @Controller
        RoutingServiceDefault.class,

        // @Repository's
        RepositoryServiceDefault.class,

        // @DomainService's
        TranslationServicePoMenu.class,

        // Exception Recognizers
        ExceptionRecognizerForDataAccessException.class,

})
public class CausewayModuleCoreRuntimeServices {

    public static final String NAMESPACE = "causeway.runtimeservices";

    @Bean @Singleton // also used by _Spring utility
    public OrderComparator orderComparator() {
        return new AnnotationAwareOrderComparator();
    }

}
