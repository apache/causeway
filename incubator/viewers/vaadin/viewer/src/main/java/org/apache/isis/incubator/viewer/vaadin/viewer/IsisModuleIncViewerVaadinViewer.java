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

package org.apache.isis.incubator.viewer.vaadin.viewer;

import java.util.HashMap;

import javax.inject.Inject;

import com.vaadin.flow.server.InitParameters;
import com.vaadin.flow.spring.RootMappedCondition;
import com.vaadin.flow.spring.SpringBootAutoConfiguration;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.VaadinConfigurationProperties;
import com.vaadin.flow.spring.VaadinServletContextInitializer;
import com.vaadin.flow.spring.VaadinWebsocketEndpointExporter;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.incubator.viewer.vaadin.ui.IsisModuleIncViewerVaadinUi;

import lombok.val;

/**
 *
 * @since 2.0
 */
@Configuration
@Import({
        // modules
        IsisModuleIncViewerVaadinUi.class,

        VaadinConfigurationProperties.class

        // @Service's


        // @Mixin's


})
//disable standard vaadin spring boot bootstrapping
@EnableAutoConfiguration(exclude = { SpringBootAutoConfiguration.class })
public class IsisModuleIncViewerVaadinViewer {


    @Inject private WebApplicationContext context;
    @Inject private VaadinConfigurationProperties configurationProperties;
    @Inject private InteractionFactory isisInteractionFactory;

    /**
     * Creates a {@link ServletContextInitializer} instance.
     *
     * @return a custom ServletContextInitializer instance
     */
    @Bean
    public ServletContextInitializer contextInitializer() {
        return new VaadinServletContextInitializer(context);
    }

    /**
     * Creates a {@link ServletRegistrationBean} instance with Spring aware Vaadin servlet.
     *
     * @return a custom ServletRegistrationBean instance
     */
    @Bean
    public ServletRegistrationBean<SpringServlet> servletRegistrationBean() {
        String urlMapping = configurationProperties.getUrlMapping();
        val initParameters = new HashMap<String, String>();
        val isRootMapping = RootMappedCondition.isRootMapping(urlMapping);
        if (isRootMapping) {
            urlMapping = "/vaadinServlet/*";
            initParameters.put(InitParameters.SERVLET_PARAMETER_PUSH_URL,
                    makeContextRelative(urlMapping.replace("*", "")));
        }
        val registration = new ServletRegistrationBean<SpringServlet>(
                new IsisServletForVaadin(isisInteractionFactory, context, isRootMapping),
                urlMapping);
        registration.setInitParameters(initParameters);
        registration.setAsyncSupported(configurationProperties.isAsyncSupported());
        registration.setName(ClassUtils.getShortNameAsProperty(SpringServlet.class));
        return registration;
    }

    /**
     * Deploys JSR-356 websocket endpoints when Atmosphere is available.
     *
     * @return the server endpoint exporter which does the actual work.
     */
    @Bean
    public ServerEndpointExporter websocketEndpointDeployer() {
        return new VaadinWebsocketEndpointExporter();
    }

    // -- HELPER

    private static String makeContextRelative(String url) {
        // / -> context://
        // foo -> context://foo
        // /foo -> context://foo
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        return "context://" + url;
    }

}
