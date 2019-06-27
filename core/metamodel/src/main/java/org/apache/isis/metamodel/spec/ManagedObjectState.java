package org.apache.isis.metamodel.spec;

/**
 * @since 2.0
 */
public enum ManagedObjectState {

	not_Persistable,
	persistable_Attached,
	persistable_Detached, 
	persistable_Destroyed
	
	;

	public boolean isPersistable() {
		return this != not_Persistable;
	}
	
	public boolean isAttached() {
		return this == persistable_Attached;
	}
	
	public boolean isDetached() {
		return this == persistable_Detached;
	}
	
	public boolean isDestroyed() {
		return this == persistable_Destroyed;
	}
	
}
