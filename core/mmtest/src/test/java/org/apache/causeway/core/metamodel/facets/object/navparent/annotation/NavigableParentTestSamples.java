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
package org.apache.causeway.core.metamodel.facets.object.navparent.annotation;

import java.math.BigInteger;

import org.apache.causeway.applib.annotation.Navigable;
import org.apache.causeway.applib.annotation.PropertyLayout;

class NavigableParentTestSamples {

    // has no navigable parent
    protected static class DomainObjectRoot {
        @Override public String toString() { return "Root"; }
    }

    // has navigable parent 'Root' specified via Annotation
    protected static class DomainObjectProperAnnot {
        private static final DomainObjectRoot myParent = new DomainObjectRoot();
        @Override public String toString() { return "properAnnot"; }

        @PropertyLayout(navigable=Navigable.PARENT)
        public DomainObjectRoot root() { return myParent; }
    }

//    // has navigable parent 'A' specified via method
//    protected static class DomainObjectProperMethod {
//        private static final DomainObjectRoot myParent = new DomainObjectRoot();
//        @Override public String toString() { return "properMethod"; }
//        public DomainObjectRoot parent() { return myParent; }
//    }

    // has invalid (value-type) navigable parent specified via Annotation
    protected static class DomainObjectInvalidParentAnnot {
        @Override public String toString() { return "invalidAnnot"; }

        @PropertyLayout(navigable=Navigable.PARENT)
        public BigInteger root() { return BigInteger.ONE; }

    }

//    // has invalid (value-type) navigable parent specified via method
//    protected static class DomainObjectInvalidParentMethod {
//        @Override public String toString() { return "invalidMethod"; }
//        public BigInteger parent() { return BigInteger.ONE; }
//    }
}
