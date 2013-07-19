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

package org.apache.isis.example.metamodel.namefile.facets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NameFileParserParsingTest {

    private NameFileParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new NameFileParser();
    }

    @After
    public void tearDown() throws Exception {
        parser = null;
    }

    @Test
    public void canFindResourceWhenExists() throws Exception {
        parser.parse();
    }

    @Test
    public void getName() throws Exception {
        parser.parse();
        assertThat(parser.getName(DomainObjectWithNameFileEntry.class), is("Customer"));
    }

    @Test
    public void getPropertyName() throws Exception {
        parser.parse();
        assertThat(parser.getMemberName(DomainObjectWithNameFileEntry.class, "lastName"), is("surname"));
    }

}
