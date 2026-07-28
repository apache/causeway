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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.stream.Stream;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

interface MemberPopulator {

	enum IntrospectionState {
	    /**
	     * At this stage, {@link LogicalType} only.
	     */
	    NOT_INTROSPECTED,
	    /**
	     * Interim stage, to avoid infinite loops while on way to being {@link #TYPE_INTROSPECTED}
	     */
	    TYPE_BEING_INTROSPECTED,
	    /**
	     * Type has been introspected (but not its members).
	     */
	    TYPE_INTROSPECTED,
	    /**
	     * Interim stage, to avoid infinite loops while on way to being {@link #FULLY_INTROSPECTED}
	     */
	    MEMBERS_BEING_INTROSPECTED,

	    //MIXED_IN_MEMBERS_ADDED,
	    /**
	     * Fully introspected... class and also its members.
	     */
	    FULLY_INTROSPECTED;

		boolean isLessThan(final IntrospectionState other) {
			return this.ordinal() < other.ordinal();
		}
	}
	
	record ComputedMembers(
			Can<ObjectAssociation> associationsInOrder,
			Can<ObjectAction> actionsInOrder
			//Map<ResolvedMethod, ObjectMember> membersByMethod,
			) {

		ComputedMembers() {
			this(Can.empty(), Can.empty());
		}

		ComputedMembers(
				final Stream<ObjectAssociation> associations,
				final Stream<ObjectAction> actions) {
			this(
					Can.ofCollection(_MemberSortingUtils.sortAssociationsIntoList(associations)),
					Can.ofCollection(_MemberSortingUtils.sortActionsIntoList(actions)));
		}

		ComputedMembers join(final ComputedMembers other) {
			return new ComputedMembers(
					Stream.concat(this.associationsInOrder.stream(), other.associationsInOrder.stream()),
					Stream.concat(this.actionsInOrder.stream(), other.actionsInOrder.stream()));
		}

	}

}