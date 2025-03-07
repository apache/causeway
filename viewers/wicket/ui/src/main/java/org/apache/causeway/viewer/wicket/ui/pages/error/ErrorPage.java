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
package org.apache.causeway.viewer.wicket.ui.pages.error;

import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;

import org.apache.causeway.applib.services.error.ErrorDetails;
import org.apache.causeway.applib.services.error.ErrorReportingService;
import org.apache.causeway.applib.services.error.Ticket;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.causeway.viewer.wicket.ui.errors.ExceptionStackTracePanel;
import org.apache.causeway.viewer.wicket.ui.errors.StackTraceDetail;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;

/**
 * Web page representing the home page (showing a welcome message).
 */
@AuthorizeInstantiation(UserMemento.AUTHORIZED_USER_ROLE)
public class ErrorPage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_EXCEPTION_STACK_TRACE = "exceptionStackTrace";

    public ErrorPage(final ExceptionModel exceptionModel) {
        super(PageParameterUtils.newPageParameters(), null);

        addBookmarkedPages(themeDiv);

        var errorReportingService = super.getMetaModelContext().getServiceRegistry()
                .lookupService(ErrorReportingService.class).orElse(null);

        if(errorReportingService != null) {

            final String mainMessage = exceptionModel.getMainMessage();
            final boolean recognized = exceptionModel.isRecognized();
            final boolean authorizationException = exceptionModel.isAuthorizationException();

            final List<StackTraceDetail> stackTrace = exceptionModel.getStackTrace();
            final List<String> stackDetailList = transform(stackTrace);

            final List<List<StackTraceDetail>> stackTraces = exceptionModel.getStackTraces();
            final List<List<String>> stackDetailLists = _Lists.newArrayList();
            for (List<StackTraceDetail> trace : stackTraces) {
                stackDetailLists.add(transform(trace));
            }

            final ErrorDetails errorDetails =
                    new ErrorDetails(mainMessage, recognized, authorizationException, stackDetailList, stackDetailLists);

            final Ticket ticket = errorReportingService.reportError(errorDetails);

            if (ticket != null) {
                exceptionModel.setTicket(ticket);
            }

        }

        var pageClassRegistry = super.getServiceRegistry().lookupServiceElseFail(PageClassRegistry.class);

        themeDiv.add(new ExceptionStackTracePanel(pageClassRegistry, ID_EXCEPTION_STACK_TRACE, exceptionModel));

    }

    protected List<String> transform(final List<StackTraceDetail> stackTrace) {
        return _Lists.map(stackTrace, StackTraceDetail::line);
    }

}
