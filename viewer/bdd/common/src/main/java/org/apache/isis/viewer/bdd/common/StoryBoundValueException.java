package org.apache.isis.viewer.bdd.common;

import java.io.CharArrayWriter;
import java.io.PrintWriter;


public class StoryBoundValueException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final CellBinding cellBinding;
	private final StoryCell storyCell;

	public static StoryBoundValueException current(CellBinding cellBinding, String message) {
		return new StoryBoundValueException(cellBinding, cellBinding.getCurrentCell(), message);
	}

	public static StoryBoundValueException arg(CellBinding cellBinding, StoryCell storyCell, String message) {
		return new StoryBoundValueException(cellBinding, storyCell, message);
	}

	private StoryBoundValueException(CellBinding cellBinding, StoryCell storyCell, String message) {
		super(message);
		this.cellBinding = cellBinding;
		this.storyCell = storyCell;
	}
	
	public CellBinding getCellBinding() {
		return cellBinding;
	}
	
	public StoryCell getStoryCell() {
		return storyCell;
	}

    public String asString() {
    	CharArrayWriter caw = new CharArrayWriter();
		this.printStackTrace(new PrintWriter(caw));
		return caw.toString();
    }


}
