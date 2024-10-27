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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
class _MemberIdClashReporting {

    /**
     * Guard against members and mixed-in members that share the same member-id.
     * <ul>
     *      <li>member-ids for actions within the same type must be unique (including mixed-in ones)</li>
     *      <li>member-ids for associations within the same type must be unique (including mixed-in ones)</li>
     * </ul>
     * Notes:
     * <p>
     * Either call with associations only or actions only, don't mix.
     * <p>
     * Needs to be called before members have been sorted or duplicates have been discarded.
     * <p>
     * {@link ObjectSpecificationAbstract} has built-in logic to stream members in a unique way,
     * that is, member streams have no member-id duplicates,
     * which in its own right helps with handling of method overriding (Java language terminology).
     */
    void flagAnyMemberIdClashes(
            final ObjectSpecification declaringType,
            final Iterable<? extends ObjectMember> regularMembers,
            final Iterable<? extends ObjectMember> mixedInMembers) {

        if(declaringType.isAbstract()) return; // skip abstract types

        var memberIdCollector = new MemberIdCollector();

        // prime member-ids from regular members, without flagging (honor method override)
        regularMembers.forEach(memberIdCollector::collect);

        mixedInMembers.forEach(objectMember->
            memberIdCollector.collect(objectMember)
            .ifPresent(previous->
                ValidationFailureUtils.raiseMemberIdClash(declaringType, previous, objectMember)));
    }

    // -- HELPER

    private static class MemberIdCollector {
        private Map<String, ObjectMember> actionIds;
        private Map<String, ObjectMember> associationIds;
        /** Optionally returns a member with the same member-id, based on whether previously collected. */
        public Optional<ObjectMember> collect(final ObjectMember objectMember) {
            if(objectMember.isAction()) {
                if(actionIds==null) this.actionIds = new HashMap<>();
                return Optional.ofNullable(actionIds.put(objectMember.getId(), objectMember));
            }
            if(objectMember.isPropertyOrCollection()) {
                if(associationIds==null) this.associationIds = new HashMap<>();
                return Optional.ofNullable(associationIds.put(objectMember.getId(), objectMember));
            }
            throw _Exceptions.unmatchedCase(String.format("framework bug: unmatched feature %s", objectMember));
        }
    }

}
