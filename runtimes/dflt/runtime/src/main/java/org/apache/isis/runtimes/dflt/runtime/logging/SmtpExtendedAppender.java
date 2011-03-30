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
package org.apache.isis.runtimes.dflt.runtime.logging;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

public class SmtpExtendedAppender extends SMTPAppender {
    
    public SmtpExtendedAppender() {
        super();
    }

    public SmtpExtendedAppender(TriggeringEventEvaluator evaluator) {
        super(evaluator);
    }

    public void append(LoggingEvent event) {
        if(evaluator.isTriggeringEvent(event)) {
            try {
                String subject = limitToFirstLine( String.valueOf(event.getMessage()));
                String encodedSubject = MimeUtility.encodeText(subject, "UTF-8", null);
                msg.setSubject(encodedSubject);
            } catch (UnsupportedEncodingException e) {
                // ???
            } catch (MessagingException e) {
                // ???
            }
        }
        super.append(event);
    }

    private String limitToFirstLine(String subject) {
        int newline = subject.indexOf('\n');
        int carriageReturn= subject.indexOf('\r');
        if (newline != -1 || carriageReturn != -1) {
            int pos = Math.max(newline, carriageReturn);
            subject = subject.substring(0, pos);
        }
        return subject;
    }

}

