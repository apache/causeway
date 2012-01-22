package org.apache.isis.core.testsupport.jmock;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;

public class JUnit4RuleMockery2Test {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    public static class Collaborator {
        public void doOtherStuff() {
        }
    }

    public static class ClassUnderTest {
        private final Collaborator collaborator;

        private ClassUnderTest(final Collaborator collaborator) {
            this.collaborator = collaborator;
        }

        public void doStuff() {
            collaborator.doOtherStuff();
        }
    }

    @Mock
    private Collaborator collaborator;

    @Test
    public void poke() {
        context.checking(new Expectations() {
            {
                one(collaborator).doOtherStuff();
            }
        });
        new ClassUnderTest(collaborator).doStuff();
    }
}
