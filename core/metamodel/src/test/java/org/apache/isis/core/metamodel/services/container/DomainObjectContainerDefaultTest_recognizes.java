package org.apache.isis.core.metamodel.services.container;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.RecognizedException;
import org.apache.isis.core.unittestsupport.jmock.auto.Mock;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DomainObjectContainerDefaultTest_recognizes {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private ExceptionRecognizer mockERS;

    private RecognizedException ex;
    
    private DomainObjectContainerDefault container;
    
    @Before
    public void setUp() throws Exception {
        ex = new RecognizedException("foo");
        container = new DomainObjectContainerDefault() {
            @Override
            ExceptionRecognizer getRecogService() {
                return mockERS;
            }
        };
    }
    
    @Test
    public void delegates() throws Exception {
        context.checking(new Expectations() {
            {
                one(mockERS).recognize(ex);
            }
        });
        container.recognize(ex);
    }
}
