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
package domainapp.modules.simple.specglue;

import java.util.List;
import java.util.UUID;

import org.apache.isis.core.specsupport.specs.CukeGlueAbstract;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import domainapp.modules.simple.dom.impl.SimpleObject;
import domainapp.modules.simple.dom.impl.SimpleObjectMenu;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SimpleObjectMenuGlue extends CukeGlueAbstract {

    @Given("^there are.* (\\d+) simple objects$")
    public void there_are_N_simple_objects(int n) throws Throwable {
        try {
            final List<SimpleObject> list = simpleObjectMenu().listAll();
            assertThat(list.size(), is(n));
            putVar("java.util.List", "simpleObjects", list);
        } finally {
            assertMocksSatisfied();
        }
    }
    
    @When("^.*create a .*simple object$")
    public void create_a_simple_object() throws Throwable {
        simpleObjectMenu().create(UUID.randomUUID().toString());
    }

    private SimpleObjectMenu simpleObjectMenu() {
        return service(SimpleObjectMenu.class);
    }

}
