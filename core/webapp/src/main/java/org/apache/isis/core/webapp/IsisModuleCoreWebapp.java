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
package org.apache.isis.core.webapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import org.apache.isis.core.interaction.session.MessageBroker;
import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.core.webapp.confmenu.ConfigurationViewServiceDefault;
import org.apache.isis.core.webapp.health.HealthIndicatorUsingHealthCheckService;
import org.apache.isis.core.webapp.modules.logonlog.WebModuleLogOnExceptionLogger;
import org.apache.isis.core.webapp.modules.templresources.WebModuleTemplateResources;
import org.apache.isis.core.webapp.webappctx.IsisWebAppContextInitializer;

@Configuration
@Import({
        // modules
        IsisModuleCoreRuntime.class,

        // @Service's
        ConfigurationViewServiceDefault.class,
        WebModuleLogOnExceptionLogger.class,
        WebModuleTemplateResources.class,

        // @Component's
        
        HealthIndicatorUsingHealthCheckService.class,

        // (not annotated)
        IsisWebAppContextInitializer.class,

})
public class IsisModuleCoreWebapp {

    @Bean
    @Scope(
            value = WebApplicationContext.SCOPE_SESSION, 
            proxyMode = ScopedProxyMode.TARGET_CLASS)
    public MessageBroker sessionScopedMessageBroker() {
        return new MessageBroker();
    }
    
}
