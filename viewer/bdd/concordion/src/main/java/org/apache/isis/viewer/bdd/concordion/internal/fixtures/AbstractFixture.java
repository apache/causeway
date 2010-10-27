package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

public abstract class AbstractFixture<T>  {

	private final T peer;

    protected AbstractFixture(final T fixturePeer) {
    	this.peer = fixturePeer;
    }
    
    public T getPeer() {
		return peer;
	}
    
}
