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
package org.apache.isis.core.runtimeservices;

import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import org.apache.isis.core.codegen.bytebuddy.IsisModuleCoreCodegenByteBuddy;
import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.core.runtimeservices.bookmarks.BookmarkServiceDefault;
import org.apache.isis.core.runtimeservices.command.CommandDtoFactoryDefault;
import org.apache.isis.core.runtimeservices.command.CommandExecutorServiceDefault;
import org.apache.isis.core.runtimeservices.command.SchemaValueMarshallerDefault;
import org.apache.isis.core.runtimeservices.email.EmailServiceDefault;
import org.apache.isis.core.runtimeservices.eventbus.EventBusServiceSpring;
import org.apache.isis.core.runtimeservices.executor.MemberExecutorServiceDefault;
import org.apache.isis.core.runtimeservices.factory.FactoryServiceDefault;
import org.apache.isis.core.runtimeservices.homepage.HomePageResolverServiceDefault;
import org.apache.isis.core.runtimeservices.i18n.po.TranslationServicePo;
import org.apache.isis.core.runtimeservices.i18n.po.TranslationServicePoMenu;
import org.apache.isis.core.runtimeservices.icons.ObjectIconServiceDefault;
import org.apache.isis.core.runtimeservices.interaction.InteractionDtoFactoryDefault;
import org.apache.isis.core.runtimeservices.jaxb.JaxbServiceDefault;
import org.apache.isis.core.runtimeservices.locale.LocaleChoiceProviderDefault;
import org.apache.isis.core.runtimeservices.locale.LanguageProviderDefault;
import org.apache.isis.core.runtimeservices.memento.ObjectMementoServiceDefault;
import org.apache.isis.core.runtimeservices.menubars.MenuBarsLoaderServiceDefault;
import org.apache.isis.core.runtimeservices.menubars.bootstrap3.MenuBarsServiceBS3;
import org.apache.isis.core.runtimeservices.message.MessageServiceDefault;
import org.apache.isis.core.runtimeservices.publish.CommandPublisherDefault;
import org.apache.isis.core.runtimeservices.publish.EntityChangesPublisherDefault;
import org.apache.isis.core.runtimeservices.publish.EntityPropertyChangePublisherDefault;
import org.apache.isis.core.runtimeservices.publish.ExecutionPublisherDefault;
import org.apache.isis.core.runtimeservices.publish.ObjectLifecyclePublisherDefault;
import org.apache.isis.core.runtimeservices.recognizer.ExceptionRecognizerServiceDefault;
import org.apache.isis.core.runtimeservices.recognizer.dae.ExceptionRecognizerForDataAccessException;
import org.apache.isis.core.runtimeservices.repository.RepositoryServiceDefault;
import org.apache.isis.core.runtimeservices.routing.RoutingServiceDefault;
import org.apache.isis.core.runtimeservices.scratchpad.ScratchpadDefault;
import org.apache.isis.core.runtimeservices.session.InteractionServiceDefault;
import org.apache.isis.core.runtimeservices.sitemap.SitemapServiceDefault;
import org.apache.isis.core.runtimeservices.transaction.TransactionServiceSpring;
import org.apache.isis.core.runtimeservices.urlencoding.UrlEncodingServiceWithCompression;
import org.apache.isis.core.runtimeservices.user.ImpersonateMenuAdvisorDefault;
import org.apache.isis.core.runtimeservices.user.ImpersonatedUserHolderDefault;
import org.apache.isis.core.runtimeservices.userreg.EmailNotificationServiceDefault;
import org.apache.isis.core.runtimeservices.wrapper.WrapperFactoryDefault;
import org.apache.isis.core.runtimeservices.xml.XmlServiceDefault;
import org.apache.isis.core.runtimeservices.xmlsnapshot.XmlSnapshotServiceDefault;

@Configuration
@Import({
        // modules
        IsisModuleCoreRuntime.class,
        IsisModuleCoreCodegenByteBuddy.class,

        // @Service's
        BookmarkServiceDefault.class,
        EntityChangesPublisherDefault.class,
        EntityPropertyChangePublisherDefault.class,
        CommandDtoFactoryDefault.class,
        CommandExecutorServiceDefault.class,
        CommandPublisherDefault.class,
        EmailNotificationServiceDefault.class,
        EmailServiceDefault.class,
        ExceptionRecognizerServiceDefault.class,
        ExecutionPublisherDefault.class,
        EventBusServiceSpring.class,
        FactoryServiceDefault.class,
        HomePageResolverServiceDefault.class,
        ImpersonatedUserHolderDefault.class,
        ImpersonateMenuAdvisorDefault.class,
        InteractionDtoFactoryDefault.class,
        InteractionServiceDefault.class,
        JaxbServiceDefault.class,
        LocaleChoiceProviderDefault.class,
        LanguageProviderDefault.class,
        ObjectMementoServiceDefault.class,
        TranslationServicePo.class,
        MemberExecutorServiceDefault.class,
        MenuBarsLoaderServiceDefault.class,
        MenuBarsServiceBS3.class,
        MessageServiceDefault.class,
        ObjectIconServiceDefault.class,
        ScratchpadDefault.class,
        SitemapServiceDefault.class,
        TransactionServiceSpring.class,
        UrlEncodingServiceWithCompression.class,
        WrapperFactoryDefault.class,
        XmlServiceDefault.class,
        XmlSnapshotServiceDefault.class,
        ObjectLifecyclePublisherDefault.class,
        SchemaValueMarshallerDefault.class,

        // @Controller
        RoutingServiceDefault.class,

        // @Repository's
        RepositoryServiceDefault.class,

        // @DomainService's
        TranslationServicePoMenu.class,

        // Exception Recognizers
        ExceptionRecognizerForDataAccessException.class,

})
public class IsisModuleCoreRuntimeServices {

    @Bean @Singleton // also used by _Spring utility
    public OrderComparator orderComparator() {
        return new AnnotationAwareOrderComparator();
    }

}
