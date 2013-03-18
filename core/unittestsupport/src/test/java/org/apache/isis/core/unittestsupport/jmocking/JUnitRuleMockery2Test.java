package org.apache.isis.core.unittestsupport.jmocking;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class JUnitRuleMockery2Test {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private Collaborator collaborator;

    @Test
    public void poke() {
        context.checking(new Expectations() {
            {
                one(collaborator).doOtherStuff();
            }
        });
        new CollaboratingUsingConstructorInjection(collaborator).collaborateWithCollaborator();
    }
}
