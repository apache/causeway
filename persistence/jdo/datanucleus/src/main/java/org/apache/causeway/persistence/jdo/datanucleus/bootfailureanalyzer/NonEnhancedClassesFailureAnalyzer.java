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
 *
 */

package org.apache.causeway.persistence.jdo.datanucleus.bootfailureanalyzer;

import org.datanucleus.exceptions.NucleusUserException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import lombok.val;

public class NonEnhancedClassesFailureAnalyzer extends AbstractFailureAnalyzer<org.datanucleus.exceptions.NucleusUserException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, NucleusUserException cause) {
        val msg = nonEnhancedMessage(cause);
        if (msg != null) {
            return new FailureAnalysis(descriptionOf(cause), action(cause), null);
        } else {
            return new FailureAnalysis(cause.getLocalizedMessage(), null, cause);
        }
    }

    private String descriptionOf(NucleusUserException cause) {
        // Found Meta-Data for class org.apache.causeway.extensions.audittrail.jdo.integtests.model.Counter but this class is either not enhanced or you have multiple copies of the persistence API jar in your CLASSPATH!! Make sure all persistable classes are enhanced before running DataNucleus and/or the CLASSPATH is correct.
        String buf = nonEnhancedMessage(cause);
        if (buf != null) return buf;
        return cause.getLocalizedMessage();
    }

    private String nonEnhancedMessage(NucleusUserException cause) {
        if (cause.getMessage() == null || !cause.getMessage().contains("See the nested exceptions for details")) {
            return null;
        }

        val buf = new StringBuilder();
        buf.append("Non-enhanced classes:\n\n");
        for (Throwable nestedException : cause.getNestedExceptions()) {
            String message = nestedException.getMessage();
            String prefix = "Found Meta-Data for class ";
            if (message.startsWith(prefix)) {
                String classNamePlusBlurb = message.substring(prefix.length());
                int spaceAfterClassName = classNamePlusBlurb.indexOf(" ");
                buf.append("  * ").append(classNamePlusBlurb, 0, spaceAfterClassName).append("\n") ;
            }
        }
        return buf.toString();
    }

    private String action(NucleusUserException cause) {
        return "Did the Enhancer run correctly?";
    }

}
