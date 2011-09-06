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

package org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;

/**
 * This test class is used to test that a class can contain a collection of itself.
 * 
 * Caveat: Every class instance can only be contained by 1 parent collection, and will only be contained once.
 * 
 * @author Kevin
 * 
 * @version $Rev$ $Date$
 */

public class PolySelfRefClass extends AbstractDomainObject {

    public String title() {
        return string;
    }

    // {{ String type
    private String string;

    public String getString() {
        return string;
    }

    public void setString(final String string) {
        this.string = string;
    }

    // }}

    // {{ PolyTestClass collection
    private List<PolySelfRefClass> polySelfRefClasses = new ArrayList<PolySelfRefClass>();

    public List<PolySelfRefClass> getPolySelfRefClasses() {
        return polySelfRefClasses;
    }

    public void setPolySelfRefClasses(final List<PolySelfRefClass> polySelfRefClasses) {
        this.polySelfRefClasses = polySelfRefClasses;
    }

    public void addToPolySelfRefClasses(final PolySelfRefClass polySelfRefClass) {
        // check for no-op
        if (polySelfRefClass == null || getPolySelfRefClasses().contains(polySelfRefClass)) {
            return;
        }
        // associate new
        getPolySelfRefClasses().add(polySelfRefClass);
    }

    public void removeFromPolySelfRefClasses(final PolySelfRefClass polySelfRefClass) {
        // check for no-op
        if (polySelfRefClass == null || !getPolySelfRefClasses().contains(polySelfRefClass)) {
            return;
        }
        // dissociate existing
        getPolySelfRefClasses().remove(polySelfRefClass);
    }
    // }}

}
