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
package org.apache.causeway.core.webapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;

import org.apache.causeway.core.interaction.session.MessageBrokerImpl;
import org.apache.causeway.core.metamodel.services.message.MessageBroker;
import org.apache.causeway.core.runtime.CausewayModuleCoreRuntime;
import org.apache.causeway.core.webapp.confmenu.ConfigurationViewServiceDefault;
import org.apache.causeway.core.webapp.health.HealthIndicatorUsingHealthCheckService;
import org.apache.causeway.core.webapp.keyvaluestore.KeyValueStoreUsingHttpSession;
import org.apache.causeway.core.webapp.modules.logonlog.WebModuleLogOnExceptionLogger;
import org.apache.causeway.core.webapp.modules.templresources.WebModuleTemplateResources;
import org.apache.causeway.core.webapp.webappctx.CausewayWebAppContextInitializer;

@Configuration
@Import({
        // Modules
        CausewayModuleCoreRuntime.class,

        // @Service's
        ConfigurationViewServiceDefault.class,
        WebModuleLogOnExceptionLogger.class,
        WebModuleTemplateResources.class,

        // @Component's

        HealthIndicatorUsingHealthCheckService.class,
        KeyValueStoreUsingHttpSession.class,

        // (not annotated)
        CausewayWebAppContextInitializer.class,

})
public class CausewayModuleCoreWebapp {

    @Bean
    @Scope(
            value = WebApplicationContext.SCOPE_SESSION,
            proxyMode = ScopedProxyMode.TARGET_CLASS)
    public MessageBroker sessionScopedMessageBroker() {
        return new MessageBrokerImpl();
    }

    /**
     * for implementation of {@link KeyValueStoreUsingHttpSession}, using {@link org.springframework.web.context.request.RequestContextHolder}.
     *
     * @see org.springframework.web.context.request.RequestContextHolder
     * @see <a href="https://stackoverflow.com/a/44830684/56880">https://stackoverflow.com/a/44830684/56880</a>
     * @see <a href="https://stackoverflow.com/a/61431621/56880">https://stackoverflow.com/a/61431621/56880</a>
     */
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

}
