package org.apache.isis.viewer.bdd.common;

import java.io.CharArrayWriter;
import java.io.PrintWriter;


public class ScenarioBoundValueException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final CellBinding cellBinding;
	private final ScenarioCell storyCell;

	public static ScenarioBoundValueException current(CellBinding cellBinding, String message) {
		return new ScenarioBoundValueException(cellBinding, cellBinding.getCurrentCell(), message);
	}

	public static ScenarioBoundValueException arg(CellBinding cellBinding, ScenarioCell storyCell, String message) {
		return new ScenarioBoundValueException(cellBinding, storyCell, message);
	}

	private ScenarioBoundValueException(CellBinding cellBinding, ScenarioCell storyCell, String message) {
		super(message);
		this.cellBinding = cellBinding;
		this.storyCell = storyCell;
	}
	
	public CellBinding getCellBinding() {
		return cellBinding;
	}
	
	public ScenarioCell getStoryCell() {
		return storyCell;
	}

    public String asString() {
    	CharArrayWriter caw = new CharArrayWriter();
		this.printStackTrace(new PrintWriter(caw));
		return caw.toString();
    }


}
