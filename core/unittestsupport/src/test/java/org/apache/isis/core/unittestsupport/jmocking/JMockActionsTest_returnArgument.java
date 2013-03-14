package org.apache.isis.core.unittestsupport.jmocking;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class JMockActionsTest_returnArgument {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private CollaboratorForReturnArgument collaborator;

    @Test
    public void poke() {
        context.checking(new Expectations() {
            {
                one(collaborator).selectOneOf(with(any(Integer.class)), with(any(Integer.class)), with(any(Integer.class)));
                will(JMockActions.returnArgument(1)); // ie the 2nd argument, which is '20'
            }
        });
        assertThat(new ClassUnderTestForReturnArgument(collaborator).addTo(4), is(24)); // adding 4 to the second argument
    }

    public interface CollaboratorForReturnArgument {
        public int selectOneOf(int x, int y, int z);
    }

    public static class ClassUnderTestForReturnArgument {
        private final CollaboratorForReturnArgument collaborator;

        private ClassUnderTestForReturnArgument(final CollaboratorForReturnArgument collaborator) {
            this.collaborator = collaborator;
        }

        public int addTo(int x) {
            return x + collaborator.selectOneOf(10, 20, 30);
        }
    }


}
