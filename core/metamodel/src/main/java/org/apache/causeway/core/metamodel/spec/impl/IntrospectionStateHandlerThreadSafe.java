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

final class IntrospectionStateHandlerThreadSafe
implements IntrospectionStateHandler {

	private final Runnable introspectTypeHierarchy;
	private final Runnable introspectMembers;
	private final Object lock = new Object();
	private IntrospectionState state;

	IntrospectionStateHandlerThreadSafe(
			final Runnable introspectTypeHierarchy,
			final Runnable introspectMembers) {
		this.introspectTypeHierarchy = introspectTypeHierarchy;
		this.introspectMembers = introspectMembers;
		this.state = IntrospectionState.NOT_INTROSPECTED;
	}

	@Override
	public boolean isFullyIntrospected() {
		// we don't care about thread synchronization here
		return state == IntrospectionState.FULLY_INTROSPECTED;
	}

    @Override
    public void introspectUpTo(final IntrospectionState upTo) {
    	if(isFullyIntrospected())
    		return; // optimization

    	// This ensures only one thread changes state at a time,
    	// but threads block while another thread holds the lock.
    	synchronized (lock) {
	        switch (state) {
	            case NOT_INTROSPECTED->{
	                if(state.isLessThan(upTo)) {
	                	transitionToTypeIntrospected();
	                }
	                if(state.isLessThan(upTo)) {
	                	transitionToFullyIntrospected();
	                }
	            }
	            case TYPE_BEING_INTROSPECTED->{} // nothing to do (interim state during introspectType)
	            case TYPE_INTROSPECTED->{
	                if(state.isLessThan(upTo)) {
	                	transitionToFullyIntrospected();
	                }
	            }
	            case MEMBERS_BEING_INTROSPECTED->{}// nothing to do (interim state during introspect fully)
	            case FULLY_INTROSPECTED->{}// nothing to do ... all done
	        }
    	}
    }

    // -- HELPER

    private void transitionToTypeIntrospected() {
        this.state = IntrospectionState.TYPE_BEING_INTROSPECTED;
        introspectTypeHierarchy.run();
        this.state = IntrospectionState.TYPE_INTROSPECTED;
    }

    private void transitionToFullyIntrospected() {
    	this.state = IntrospectionState.MEMBERS_BEING_INTROSPECTED;
        introspectMembers.run();
        this.state = IntrospectionState.FULLY_INTROSPECTED;
    }


}