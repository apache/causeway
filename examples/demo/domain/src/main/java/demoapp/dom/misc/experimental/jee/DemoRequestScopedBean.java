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
package demoapp.dom.misc.experimental.jee;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.annotation.RequestScope;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.interaction.session.InteractionFactory;

import lombok.extern.log4j.Log4j2;

@Service
@Named("demoapp.DemoRequestScopedBean")
@RequestScope
@Scope(
        value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("demo")
@Log4j2
public class DemoRequestScopedBean {

    @Inject private InteractionFactory isisInteractionFactory;

    public DemoRequestScopedBean() {
        log.info("init " + this.hashCode());
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct");
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy");
    }

    public void debug() {
        log.info("fact " + isisInteractionFactory);
    }


}
