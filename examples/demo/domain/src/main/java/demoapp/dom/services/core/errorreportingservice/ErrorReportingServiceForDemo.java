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
package demoapp.dom.services.core.errorreportingservice;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.error.ErrorDetails;
import org.apache.causeway.applib.services.error.ErrorReportingService;
import org.apache.causeway.applib.services.error.Ticket;
import org.apache.causeway.applib.services.error.Ticket.StackTracePolicy;

import lombok.val;

//tag::class[]
@Service
@Named("demo.ErrorReportingServiceDemoImplementation")
@Qualifier("demo")
public class ErrorReportingServiceForDemo implements ErrorReportingService {

    @Override
    public Ticket reportError(final ErrorDetails errorDetails) {

        val reference = "#0";                               // <.>
        val userMessage = errorDetails.getMainMessage();
        val details = "Apologies!";

        val mailTo = EmailTicket.MailTo.builder()
                .receiver("support@hello.world")
                .subject("[Demo-App] Unexpected Error (" + reference + ")")
                .body(EmailTicket.MailTo.mailBodyOf(errorDetails))
                .build();

        return new EmailTicket(                             // <.>
                mailTo,
                reference,
                userMessage,
                details,
                StackTracePolicy.SHOW,
                "http://www.randomkittengenerator.com/cats/rotator.php");
    }
}
//end::class[]
