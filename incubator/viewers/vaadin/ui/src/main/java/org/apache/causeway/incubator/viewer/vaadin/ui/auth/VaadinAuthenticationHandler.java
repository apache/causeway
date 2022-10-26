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
package org.apache.causeway.incubator.viewer.vaadin.ui.auth;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.incubator.viewer.vaadin.ui.pages.login.VaadinLoginView;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Hooks into Vaadin's routing, such that unauthorized access is redirected to the login view.
 * @since Mar 9, 2020
 *
 */
@Component
@PWA(name = "Example Project", shortName = "Example Project")
//@Theme(themeClass = Material.class, variant = Material.DARK)
@Theme(themeClass = Lumo.class, variant = Lumo.LIGHT)
@Log4j2
public class VaadinAuthenticationHandler
implements
    AppShellConfigurator,
    VaadinServiceInitListener {

    private static final long serialVersionUID = 1L;

    @Inject private transient InteractionService interactionService;
    @Inject private transient MetaModelContext metaModelContext;

    @Override
    public void serviceInit(ServiceInitEvent event) {

        log.debug("service init event {}", event.getSource());

        event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().addBeforeEnterListener(this::beforeEnter);
            uiEvent.getUI().addBeforeLeaveListener(this::beforeLeave);
        });
    }

    /**
     * @param authenticationRequest
     * @return whether login was successful
     */
    public boolean loginToSession(AuthenticationRequest authenticationRequest) {
        val authentication = metaModelContext.getAuthenticationManager()
                .authenticate(authenticationRequest);

        if(authentication!=null) {
            log.debug("logging in {}", authentication.getUser().getName());
            AuthSessionStoreUtil.put(authentication);
            return true;
        }
        return false;
    }

    /**
     * Executes a piece of code in a new (possibly nested) CausewayInteraction, using the
     * current Authentication, as, at this point, should be stored in the
     * current VaadinSession.
     *
     * @param callable - the piece of code to run
     *
     */
    public <R> R callAuthenticated(Callable<R> callable) {
        return AuthSessionStoreUtil.get()
                .map(authentication->interactionService.call(authentication, callable))
                .orElse(null); // TODO redirect to login
    }

    /**
     * Variant of {@link #callAuthenticated(Callable)} that takes a runnable.
     * @param runnable
     */
    public void runAuthenticated(ThrowingRunnable runnable) {
        final Callable<Void> callable = ()->{runnable.run(); return null;};
        callAuthenticated(callable);
    }


    // -- HELPER

    private void beforeEnter(BeforeEnterEvent event) {
        val targetView = event.getNavigationTarget();
        log.debug("detected a routing event to {}", targetView);

        val authentication = AuthSessionStoreUtil.get().orElse(null);
        if(authentication!=null) {
            interactionService.openInteraction(authentication);
            return; // access granted
        }
        // otherwise redirect to login page
        if(!VaadinLoginView.class.equals(targetView)) {
            event.rerouteTo(VaadinLoginView.class);
        }
    }

    private void beforeLeave(BeforeLeaveEvent event) {
        //causewayInteractionFactory.closeSessionStack();
    }





}
