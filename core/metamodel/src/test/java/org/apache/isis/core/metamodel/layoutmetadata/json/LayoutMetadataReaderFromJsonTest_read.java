/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.layoutmetadata.json;

import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Objects;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class LayoutMetadataReaderFromJsonTest_read {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    private ServicesInjector mockServicesInjector;

    @Mock
    private GridService mockGridService;

    private LayoutMetadataReaderFromJson reader;

    @Before
    public void setUp() throws Exception {
        reader = new LayoutMetadataReaderFromJson();

        context.checking(new Expectations() {{
            allowing(mockServicesInjector).lookupService(GridService.class);
            will(returnValue(mockGridService));
        }});

        reader.setServicesInjector(mockServicesInjector);
    }
    
    @Test
    public void happyCase() throws Exception {

        context.checking(new Expectations() {{
            ignoring(mockGridService);
        }});

        final Properties properties = reader.asProperties(ExampleDomainObject.class);
        assertThat(properties, is(not(nullValue())));
        
        final Properties expectedProperties = ClassExtensions.resourceProperties(ExampleDomainObject.class, ".layout.properties");
        
        final Set<Object> expectedSet = expectedProperties.keySet();
        final Set<Object> actualSet = properties.keySet();
        final SortedSet<Object> keys = new TreeSet<>();
        keys.addAll(expectedSet);
        keys.addAll(actualSet);
        
        final StringBuilder buf = new StringBuilder("\n");
        boolean fail = false;
        for (Object keyObj : keys) {
            final String key = (String) keyObj;
            final String expectedValue = expectedProperties.getProperty(key);
            final String actualValue = properties.getProperty(key);
            
            final boolean expectedKey = expectedSet.contains(keyObj);
            final boolean matches = Objects.equal(trimmed(expectedValue), trimmed(actualValue)) || !expectedKey;
            final String result = matches? (expectedKey?"OK":"N/A"):"FAIL";
            if(!result.equals("OK")) {
                buf.append(String.format("%1$-40s %2$-5s %3$-40s %4$-40s\n", key, result, expectedValue, actualValue));
            }
            fail = fail || !matches;
        }
        if(fail) {
            Assert.fail(buf.toString());
        }
    }

    private String trimmed(String expectedValue) {
        return expectedValue != null? expectedValue.trim(): expectedValue;
    }
}
