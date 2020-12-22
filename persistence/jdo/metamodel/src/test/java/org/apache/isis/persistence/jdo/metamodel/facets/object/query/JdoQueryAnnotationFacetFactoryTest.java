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
package org.apache.isis.persistence.jdo.metamodel.facets.object.query;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class JdoQueryAnnotationFacetFactoryTest {

    public static class From_Test extends JdoQueryAnnotationFacetFactoryTest {

        @Test
        public void lower_case() throws Exception {

            String query = "SELECT from domainapp.modules.simple.dom.impl.SimpleObject WHERE name.indexOf(:name) >= 0 ";

            String name = JdoQueryAnnotationFacetFactory.from(query);

            assertThat(name, is(equalTo("domainapp.modules.simple.dom.impl.SimpleObject")));
        }

        @Test
        public void upper_case() throws Exception {

            String query = "SELECT FROM domainapp.modules.simple.dom.impl.SimpleObject WHERE name.indexOf(:name) >= 0 ";

            String name = JdoQueryAnnotationFacetFactory.from(query);

            assertThat(name, is(equalTo("domainapp.modules.simple.dom.impl.SimpleObject")));
        }

        @Test
        public void no_match() throws Exception {

            String query = "UPDATE org.isisaddons.module.sessionlogger.dom.SessionLogEntry "
                    + "   SET logoutTimestamp == :logoutTimestamp "
                    + "      ,causedBy2 == :causedBy2 "
                    + " WHERE causedBy2 == null";

            String name = JdoQueryAnnotationFacetFactory.from(query);

            assertThat(name, is(nullValue()));
        }

    }

    public static class Variables_Test extends JdoQueryAnnotationFacetFactoryTest {

        @Test
        public void lower_case() throws Exception {

            String query = "SELECT FROM mydomain.Supplier WHERE this.products.contains(prod) && prod.name == 'Beans' variables mydomain.Product prod ";

            String variables = JdoQueryAnnotationFacetFactory.variables(query);

            assertThat(variables, is(equalTo("mydomain.Product")));
        }

        @Test
        public void upper_case() throws Exception {

            String query = "SELECT FROM mydomain.Supplier WHERE this.products.contains(prod) && prod.name == 'Beans' VARIABLES mydomain.Product prod ";

            String variables = JdoQueryAnnotationFacetFactory.variables(query);

            assertThat(variables, is(equalTo("mydomain.Product")));
        }

        @Test
        public void no_match() throws Exception {

            String query = "SELECT FROM domainapp.modules.simple.dom.impl.SimpleObject WHERE name.indexOf(:name) >= 0 ";

            String variables = JdoQueryAnnotationFacetFactory.variables(query);

            assertThat(variables, is(nullValue()));
        }

    }

}