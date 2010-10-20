package org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.perform;

import org.apache.isis.extensions.bdd.common.StoryCell;

import fit.Parse;

public class StoryCellForFitNesse implements StoryCell {
	
	private Parse source;

	public StoryCellForFitNesse(Parse source) {
		this.source = source;
	}

	public String getText() {
		return source.text();
	}

	/**
	 * The implementation-specific representation of this text.
	 * 
	 * <p>
	 * Holds a Fit {@link Parse} object.
	 */
	public Object getSource() {
		return source;
	}

	public void setText(String str) {
		source.body = str;
	}
	
}
