package org.apache.isis.applib.services.exceprecog;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ExceptionRecognizerCompositeTest {

    private ExceptionRecognizerComposite composite;

    static class FakeERS implements ExceptionRecognizer {
        private String message;
        public FakeERS(String message) {
            this.message = message;
        }
        @Override
        public String recognize(Throwable ex) {
            return message;
        }
    }
    
    @Before
    public void setUp() throws Exception {
        composite = new ExceptionRecognizerComposite();
    }
    
    @Test
    public void whenEmpty() {
        assertThat(composite.recognize(new RuntimeException()), is(nullValue()));
    }

    @Test
    public void whenOne() {
        composite.add(new FakeERS("one"));
        assertThat(composite.recognize(new RuntimeException()), is("one"));
    }

    @Test
    public void whenNullThenOne() {
        composite.add(new FakeERS(null));
        composite.add(new FakeERS("one"));
        assertThat(composite.recognize(new RuntimeException()), is("one"));
    }

    @Test
    public void whenOneThenTwo() {
        composite.add(new FakeERS("one"));
        composite.add(new FakeERS("two"));
        assertThat(composite.recognize(new RuntimeException()), is("one"));
    }
}
