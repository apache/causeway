package org.apache.isis.core.unittestsupport.jmocking;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.ClassUnderTest;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class JUnitRuleMockery2Test_autoWiring_happyCase {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private Collaborator collaborator;

    @ClassUnderTest
	private Collaborating collaborating;

    @Before
	public void setUp() throws Exception {
    	collaborating = (Collaborating) context.getClassUnderTest();
	}
    
    @Test
    public void wiring() {
    	assertThat(collaborating.collaborator, is(not(nullValue())));
    }


}
