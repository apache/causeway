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

package org.apache.isis.viewer.wicket.ui.pages.register;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosure;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionStackTracePanel;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing the about page.
 */
public class RegisterPage extends WebPage {

    private static final long serialVersionUID = 1L;

    private static final String ID_PAGE_TITLE = "pageTitle";
    private static final String ID_APPLICATION_NAME = "applicationName";
    private static final String REGISTER_FORM = "registerForm";

    private static final String ID_EXCEPTION_STACK_TRACE = "exceptionStackTrace";

    /**
     * {@link com.google.inject.Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationName")
    private String applicationName;

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationCss")
    private String applicationCss;

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationJs")
    private String applicationJs;


    /**
     * If set by {@link org.apache.isis.viewer.wicket.ui.pages.PageAbstract}.
     */
    private static ExceptionModel getAndClearExceptionModelIfAny() {
        ExceptionModel exceptionModel = PageAbstract.EXCEPTION.get();
        PageAbstract.EXCEPTION.remove();
        return exceptionModel;
    }


    public RegisterPage() {
        this(null);
    }

    public RegisterPage(final PageParameters parameters) {
        this(parameters, getAndClearExceptionModelIfAny());
    }

    public RegisterPage(final PageParameters parameters, ExceptionModel exceptionModel) {

        // TODO: the idea here is that the page would be called with some sort of encrypted parameter that within it
        // would include the username ... perhaps the email address IS the username?
        final StringValue userNameValue = parameters.get("username");

        // TODO: need some sort of validation in case there is no value available
        setUsername(userNameValue.toString(""));

        addPageTitle();
        addApplicationName();
        add(new RegisterForm(REGISTER_FORM));

        if(exceptionModel != null) {
            add(new ExceptionStackTracePanel(ID_EXCEPTION_STACK_TRACE, exceptionModel));
        } else {
            add(new WebMarkupContainer(ID_EXCEPTION_STACK_TRACE).setVisible(false));
        }

    }


    private MarkupContainer addPageTitle() {
        return add(new Label(ID_PAGE_TITLE, applicationName));
    }

    private void addApplicationName() {
        add(new Label(ID_APPLICATION_NAME, applicationName));
    }


    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference())));

        if(applicationCss != null) {
            response.render(CssReferenceHeaderItem.forUrl(applicationCss));
        }
        if(applicationJs != null) {
            response.render(JavaScriptReferenceHeaderItem.forUrl(applicationJs));
        }
    }


    /**
     * Sign in form.
     */
    public final class RegisterForm extends StatelessForm<RegisterPage>
    {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param id
         *            id of the form component
         */
        public RegisterForm(final String id)
        {
            super(id);

            setModel(new CompoundPropertyModel<>(RegisterPage.this));

            // TODO: this needs tidying up substantially in the UI
            add(new Label("username", Model.of(getUsername())));
            add(new PasswordTextField("password"));

        }

        /**
         * @see org.apache.wicket.markup.html.form.Form#onSubmit()
         */
        @Override
        public final void onSubmit()
        {
            IsisContext.openSession(new InitialisationSession());

            // TODO: need to add a link on the login page to register.
            // TODO: however, if there is no UserRegistrationService domain service available, then should suppress the link to get to the registration page.
            final UserRegistrationService userRegistrationService = IsisContext.getPersistenceSession().getServicesInjector().lookupService(UserRegistrationService.class);

            IsisContext.getTransactionManager().executeWithinTransaction(new TransactionalClosure() {
                @Override
                public void preExecute() {
                }

                @Override
                public void execute() {
                    userRegistrationService.registerUser(username, password);
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure() {
                }
            });

            IsisContext.closeSession();

            // TODO: more error handling here... eg what if the username is already in use.
            signIn(getUsername(), getPassword());
            setResponsePage(getApplication().getHomePage());
        }
        private boolean signIn(String username, String password)
        {
            return AuthenticatedWebSession.get().signIn(username, password);
        }
    }


    private String username;
    public String getPassword()
    {
        return password;
    }
    public void setPassword(final String password)
    {
        this.password = password;
    }

    private String password;
    public String getUsername()
    {
        return username;
    }
    public void setUsername(final String username)
    {
        this.username = username;
    }



    // ///////////////////////////////////////////////////
    // System components
    // ///////////////////////////////////////////////////

    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

}
