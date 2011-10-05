package org.apache.isis.viewer.json.viewer;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class ResourceContextTest_getArg {

    private HttpServletRequest httpServletRequest;
    
    private ResourceContext resourceContext;
    private Mockery context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private QueryParameter<?> queryParameter;


    @Before
    public void setUp() throws Exception {
        httpServletRequest = context.mock(HttpServletRequest.class);
        queryParameter = context.mock(QueryParameter.class);
        
    }

    @Test
    public void delegatesToQueryParam() throws Exception {
        final Map<?, ?> parameterMap = context.mock(Map.class);
        context.checking(new Expectations() {
            {
                one(httpServletRequest).getParameterMap();
                will(returnValue(parameterMap));
                one(queryParameter).valueOf(parameterMap);
            }
        });
        resourceContext = new ResourceContext(null, null, null, null, httpServletRequest, null, null, null, null, null, null, null) {
            @Override
            void init(RepresentationType representationType) {
                //
            }
        };
        resourceContext.getArg(queryParameter);
    }

}
