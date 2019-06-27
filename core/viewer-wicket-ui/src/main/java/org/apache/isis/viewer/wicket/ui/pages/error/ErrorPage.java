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

package org.apache.isis.viewer.wicket.ui.pages.error;

import java.util.List;

import org.apache.isis.applib.services.error.ErrorDetails;
import org.apache.isis.applib.services.error.ErrorReportingService;
import org.apache.isis.applib.services.error.Ticket;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionStackTracePanel;
import org.apache.isis.viewer.wicket.ui.errors.StackTraceDetail;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;

/**
 * Web page representing the home page (showing a welcome message).
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class ErrorPage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_EXCEPTION_STACK_TRACE = "exceptionStackTrace";


    public ErrorPage(ExceptionModel exceptionModel) {
        super(PageParametersUtils.newPageParameters(), null);

        addBookmarkedPages(themeDiv);

        final ErrorReportingService errorReportingService = IsisContext.getServiceRegistry()
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

        themeDiv.add(new ExceptionStackTracePanel(ID_EXCEPTION_STACK_TRACE, exceptionModel));

    }

    protected List<String> transform(final List<StackTraceDetail> stackTrace) {
        return _Lists.map(stackTrace, (final StackTraceDetail stackTraceDetail) -> stackTraceDetail.getLine());
    }

}
