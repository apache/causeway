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
package org.apache.isis.core.runtime.logging;

@Deprecated //cannot subclass SmtpAppender from Log4j v2
public class SmtpExtendedAppender { //extends SmtpAppender {
//
//    public SmtpExtendedAppender() {
//        super();
//    }
//
//    public SmtpExtendedAppender(final TriggeringEventEvaluator evaluator) {
//        super(evaluator);
//    }
//
//    @Override
//    public void append(final LoggingEvent event) {
//        if (evaluator.isTriggeringEvent(event)) {
//            try {
//                final String subject = limitToFirstLine(String.valueOf(event.getMessage()));
//                final String encodedSubject = MimeUtility.encodeText(subject, "UTF-8", null);
//                msg.setSubject(encodedSubject);
//            } catch (final UnsupportedEncodingException e) {
//                // ???
//            } catch (final MessagingException e) {
//                // ???
//            }
//        }
//        super.append(event);
//    }
//
//    private String limitToFirstLine(String subject) {
//        final int newline = subject.indexOf('\n');
//        final int carriageReturn = subject.indexOf('\r');
//        if (newline != -1 || carriageReturn != -1) {
//            final int pos = Math.max(newline, carriageReturn);
//            subject = subject.substring(0, pos);
//        }
//        return subject;
//    }

}
