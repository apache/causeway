package org.apache.isis.extensions.bdd.common;

public interface StoryCell {

	/**
	 * The text represented by the source.
	 */
	public String getText();

	/**
	 * Sets the text held by the source.
	 */
	public void setText(String str);
	
	/**
	 * The implementation-specific representation of this text.
	 */
	public Object getSource();

	
}
