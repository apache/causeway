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
package org.apache.isis.testdomain.model.bad;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;

public class InvalidLogicalTypeNameClash {

    // logical type name clash: should fail validation
    @DomainObject(nature = Nature.VIEW_MODEL,
            logicalTypeName = "isis.testdomain.InvalidLogicalTypeNameClash")
    public static class VariantA {

        @Action
        public boolean anAction() {
            return false;
        }

    }

    // logical type name clash: should fail validation
    @DomainObject(nature = Nature.VIEW_MODEL,
            logicalTypeName = "isis.testdomain.InvalidLogicalTypeNameClash")
    public static  class VariantB {

        @Action
        public boolean anAction() {
            return false;
        }

    }

}
