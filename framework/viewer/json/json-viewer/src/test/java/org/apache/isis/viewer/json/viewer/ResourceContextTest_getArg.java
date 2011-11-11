/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.json.viewer;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulRequest.RequestParameter;
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

    private RequestParameter<?> queryParameter;


    @Before
    public void setUp() throws Exception {
        httpServletRequest = context.mock(HttpServletRequest.class);
        queryParameter = context.mock(RequestParameter.class);
        
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
