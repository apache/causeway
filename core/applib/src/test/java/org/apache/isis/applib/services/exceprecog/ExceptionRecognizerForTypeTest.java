package org.apache.isis.applib.services.exceprecog;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.base.Function;

public class ExceptionRecognizerForTypeTest {

    private ExceptionRecognizerForType ersForType;

    static class FooException extends Exception {
        private static final long serialVersionUID = 1L;
        public FooException() {
            super("foo");
        }
        
    }
    static class BarException extends Exception {
        private static final long serialVersionUID = 1L;
        public BarException() {
            super("bar");
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
        ersForType = new ExceptionRecognizerForType(FooException.class);
        assertThat(ersForType.recognize(new FooException()), is("foo"));
    }

    @Test
    public void whenDoesNotRecognize() {
        ersForType = new ExceptionRecognizerForType(FooException.class);
        assertThat(ersForType.recognize(new BarException()), is(nullValue()));
    }

    @Test
    public void whenRecognizedWithMessageParser() {
        ersForType = new ExceptionRecognizerForType(FooException.class, prepend);
        assertThat(ersForType.recognize(new FooException()), is("pre: foo"));
    }

}
