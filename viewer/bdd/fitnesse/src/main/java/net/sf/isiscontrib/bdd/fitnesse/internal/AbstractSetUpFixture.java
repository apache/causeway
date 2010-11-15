package net.sf.isiscontrib.bdd.fitnesse.internal;

import fitlibrary.SetUpFixture;

public abstract class AbstractSetUpFixture<T> extends SetUpFixture {

	private final T peer;

	public AbstractSetUpFixture(
			T peer) {
		this.peer = peer;
	}
	
	public T getPeer() {
		return peer;
	}

}
