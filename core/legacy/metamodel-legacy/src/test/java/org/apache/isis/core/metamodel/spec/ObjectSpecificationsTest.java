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

package org.apache.isis.core.metamodel.spec;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ObjectSpecificationsTest {
    
    @Parameters
    public static Collection<Object[][]> data() {
        return Arrays.asList(new Object[][][]{
                {
                    {"X","Y","Z"}, {"X","Y","Z"}, {"X","Y","Z"},
                },
                {
                    {"X","Y","Z"}, {"A","B","C"}, {"X","Y","Z"}
                },
                {
                    {"X","Y","Z"}, {"Z","B","C"}, {"Z","X","Y"}
                },
                {
                    {"X","Y","Z"}, {"Z","Y","X"}, {"Z","Y","X"}
                },
                {
                    {"Dates","General"}, {"General"}, {"General","Dates"}
                },
        });
    }

    private static List<String> asListOfStrings(Object[] values) {
        final List<String> list = Lists.newArrayList();
        for(Object value: values) {
            list.add((String) value);
        }
        return list;
    }

    private final List<String> valuesToOrder;
    private final List<String> valuesInRequiredOrder;
    private final List<String> expected;
     
    public ObjectSpecificationsTest(Object[] valuesToOrder, Object[] valuesInRequiredOrder, Object[] expected) {
        this.valuesToOrder = asListOfStrings(valuesToOrder);
        this.valuesInRequiredOrder = asListOfStrings(valuesInRequiredOrder);
        this.expected = asListOfStrings(expected);
    }
    
    @Test
    public void test() throws Exception {
        assertThat(ObjectSpecifications.order(valuesToOrder, valuesInRequiredOrder), is(expected));
    }
}
