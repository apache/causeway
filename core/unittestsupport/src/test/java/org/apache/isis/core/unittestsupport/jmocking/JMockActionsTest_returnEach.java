package org.apache.isis.core.unittestsupport.jmocking;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class JMockActionsTest_returnEach {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private CollaboratorForReturnEach collaborator;

    @Test
    public void poke() {
        context.checking(new Expectations() {
            {
                exactly(3).of(collaborator).readValue();
                will(JMockActions.returnEach(1,2,3));
            }
        });
        assertThat(new ClassUnderTestForReturnEach(collaborator).prependAndRead("foo"), is("foo 1"));
        assertThat(new ClassUnderTestForReturnEach(collaborator).prependAndRead("bar"), is("bar 2"));
        assertThat(new ClassUnderTestForReturnEach(collaborator).prependAndRead("baz"), is("baz 3"));
    }
    
    public interface CollaboratorForReturnEach {
        public int readValue();
    }

    public static class ClassUnderTestForReturnEach {
        private final CollaboratorForReturnEach collaborator;

        private ClassUnderTestForReturnEach(final CollaboratorForReturnEach collaborator) {
            this.collaborator = collaborator;
        }

        public String prependAndRead(String prepend) {
            return prepend + " " + collaborator.readValue();
        }
    }


}
