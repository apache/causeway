package org.apache.isis.viewer.bdd.concordion.internal.fixtures.perform;

import org.apache.isis.viewer.bdd.common.StoryCell;

/**
 * Simply holds the text.
 *
 */
public class StoryCellForConcordion implements StoryCell {
	
// for FitNesse, the source is a fit.Parse, which is a mutable table cell.
//	private Parse source;
	
	private String source;

	public StoryCellForConcordion(String source) {
		this.source = source;
	}

	public String getText() {
		return source;
	}

	public void setText(String str) {
		this.source = str;
	}

	/**
	 * The implementation-specific representation of this text.
	 */
	public Object getSource() {
		return source;
	}

	
}
