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

package org.apache.isis.runtimes.dflt.testsupport.domain;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.util.TitleBuffer;

@ObjectType("TPJ")
public class TestPojo {
    
    private static int nextId;
    
    private final int id = nextId++;
    private final String state = "pojo" + id;


    // {{ title
    public String title() {
        return propertyUsedForTitle;
    }
    // }}
    
    // {{ PropertyUsedForTitle (property)
    private String propertyUsedForTitle;

    @MemberOrder(sequence = "1")
    public String getPropertyUsedForTitle() {
        return propertyUsedForTitle;
    }

    public void setPropertyUsedForTitle(final String propertyUsedForTitle) {
        this.propertyUsedForTitle = propertyUsedForTitle;
    }
    // }}


    
    @Override
    public String toString() {
        return "Pojo#" + id;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (other.getClass() == getClass()) {
            final TestPojo otherTestPojo = (TestPojo) other;
            return otherTestPojo.state.equals(state);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }


}
