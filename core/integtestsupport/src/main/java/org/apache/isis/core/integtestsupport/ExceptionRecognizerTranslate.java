package org.apache.isis.core.integtestsupport;

import java.util.List;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class ExceptionRecognizerTranslate implements MethodRule {

    public static ExceptionRecognizerTranslate create() {
        return new ExceptionRecognizerTranslate();
    }
    private ExceptionRecognizerTranslate(){}


    @Override
    public Statement apply(final Statement statement, final FrameworkMethod frameworkMethod, final Object o) {
        return new TranslationStatement(statement);
    }

    private class TranslationStatement extends Statement {
        private final Statement next;

        public TranslationStatement(final Statement base) {
            this.next = base;
        }

        public void evaluate() throws Throwable {
            try {
                this.next.evaluate();
            } catch (final Throwable ex) {
                recognize(ex);
                throw ex;
            }
        }
    }

    /**
     * Simply invokes {@link org.apache.isis.applib.services.exceprecog.ExceptionRecognizer#recognize(Throwable)} for all registered {@link org.apache.isis.applib.services.exceprecog.ExceptionRecognizer}s for the provided exception, so that the message will be translated.
     */
    private void recognize(final Throwable ex) {
        final List<ExceptionRecognizer> exceptionRecognizers = IsisContext.getPersistenceSession().getServicesInjector().lookupServices(ExceptionRecognizer.class);
        for (final ExceptionRecognizer exceptionRecognizer : exceptionRecognizers) {
            final String unused = exceptionRecognizer.recognize(ex);
        }
    }

}
