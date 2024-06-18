package org.apache.causeway.applib.services.exceprecog;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ExceptionRecognizerForBookmarkNotFoundExceptionTest {
    static class SomeRandomException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    private Exception ex;

    private ExceptionRecognizerForBookmarkNotFoundException excepRecognizer;

    @BeforeEach
    public void setUp() throws Exception {
        excepRecognizer = new ExceptionRecognizerForBookmarkNotFoundException();
    }

    @Test
    public void whenSomeRandomException_is_not_recognized() throws Exception {
        ex = new SomeRandomException();
        assertThat(excepRecognizer.recognize(ex), is(Optional.empty()));
    }
}