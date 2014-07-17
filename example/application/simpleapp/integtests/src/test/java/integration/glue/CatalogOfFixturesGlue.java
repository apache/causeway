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
package integration.glue;

import cucumber.api.java.Before;
import dom.simple.SimpleObject;
import fixture.simple.SimpleObjectsFixture;

import org.apache.isis.core.specsupport.scenarios.InMemoryDB;
import org.apache.isis.core.specsupport.specs.CukeGlueAbstract;

public class CatalogOfFixturesGlue extends CukeGlueAbstract {

    
    @Before(value={"@unit", "@SimpleObjectsFixture"}, order=20000)
    public void unitFixtures() throws Throwable {
        final InMemoryDB inMemoryDB = new InMemoryDBForSimpleApp(this.scenarioExecution());
        inMemoryDB.getElseCreate(SimpleObject.class, "Foo");
        inMemoryDB.getElseCreate(SimpleObject.class, "Bar");
        inMemoryDB.getElseCreate(SimpleObject.class, "Baz");
        putVar("isis", "in-memory-db", inMemoryDB);
    }

    // //////////////////////////////////////

    @Before(value={"@integration", "@SimpleObjectsFixture"}, order=20000)
    public void integrationFixtures() throws Throwable {
        scenarioExecution().install(new SimpleObjectsFixture());
    }
    

}
