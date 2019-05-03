package org.apache.isis.core.commons.collections;

/**
 * @since 2.0.0-M3
 */
public enum Cardinality {
	
	ZERO,
	ONE,
	MULTIPLE
	;
	
	public boolean isZero() {
		return this == ZERO;
	}
	
	public boolean isOne() {
		return this == ONE;
	}
	
	public boolean isMultiple() {
		return this == MULTIPLE;
	}
	
}
