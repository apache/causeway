package org.apache.isis.persistence.jdo.datanucleus.bootfailureanalyzer;

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
        // Found Meta-Data for class org.apache.isis.extensions.audittrail.jdo.integtests.model.Counter but this class is either not enhanced or you have multiple copies of the persistence API jar in your CLASSPATH!! Make sure all persistable classes are enhanced before running DataNucleus and/or the CLASSPATH is correct.
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
