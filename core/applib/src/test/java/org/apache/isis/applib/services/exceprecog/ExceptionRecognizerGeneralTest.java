package org.apache.isis.applib.services.exceprecog;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicates;

public class ExceptionRecognizerGeneralTest {

    private ExceptionRecognizerGeneral ersGeneral;

    static class FooException extends Exception {
        private static final long serialVersionUID = 1L;
        public FooException() {
            super("foo");
        }
    }
    
    private Function<String,String> prepend = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return "pre: " + input;
        }
    };
    
    
    @Test
    public void whenRecognized() {
        ersGeneral = new ExceptionRecognizerGeneral(Predicates.<Throwable>alwaysTrue());
        assertThat(ersGeneral.recognize(new FooException()), is("foo"));
    }

    @Test
    public void whenDoesNotRecognize() {
        ersGeneral = new ExceptionRecognizerGeneral(Predicates.<Throwable>alwaysFalse());
        assertThat(ersGeneral.recognize(new FooException()), is(nullValue()));
    }

    @Test
    public void whenRecognizedWithMessageParser() {
        ersGeneral = new ExceptionRecognizerGeneral(Predicates.<Throwable>alwaysTrue(), prepend);
        assertThat(ersGeneral.recognize(new FooException()), is("pre: foo"));
    }

}
