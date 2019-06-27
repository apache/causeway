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
package org.apache.isis.metamodel.specloader.specimpl;

import java.util.function.Predicate;

import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectMember;


/**
 * Interface indicating an a contributed association or action.
 */
public interface ContributeeMember extends ObjectMember {


    boolean isContributedBy(ObjectAction serviceAction);

    /**
     * Which parameter of the original contributed action was contributed to (zero-based).
     */
    int getContributeeParamPosition();
    
    ObjectSpecification getServiceContributedBy();
    
    // -- UTILITY
    
    public static class Predicates {

        private Predicates(){}

        /**
         * Now generalized to handle {@link MixedInMember}s as well as {@link ContributeeMember}s.
         */
        public static <T extends ObjectMember> Predicate<T> regularElse(final Contributed contributed) {
            return (T input) -> regular().or(is(contributed)).test(input);
        }

        public static <T extends ObjectMember> Predicate<T> regular() {
            return (T input) -> !(input instanceof ContributeeMember) && !(input instanceof MixedInMember);
        }

        public static <T extends ObjectMember> Predicate<T> is(final Contributed contributed) {
            return (T input) -> contributed.isIncluded();
        }

    }
    
}
