package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.skylark.value.TextField.CursorPosition;
import org.nakedobjects.viewer.skylark.value.TextField.Selection;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Category;


class TextFieldContent {
	private static final Category LOG = Category.getInstance(TextFieldContent.class);

	private TextField forField;
	private Vector blocks;

	public TextFieldContent(TextField field) {
		this.forField = field;
		this.blocks = new Vector();
	}
	
	public void addBlock(String text) {
		blocks.addElement(new TextFieldBlock(forField, text));
		
	}

	public int noLines() {
		int lineCount = 0;
		Enumeration e = blocks.elements();
		while (e.hasMoreElements()) {
			lineCount += ((TextFieldBlock) e.nextElement()).noLines();
		}
		return lineCount;
	}

	private BlockReference getBlockFor(int line) {
		if(line < 0) {throw new IllegalArgumentException("Line must be greater than zero - " + line); }
		for (int i = 0; i < blocks.size(); i++) {
			TextFieldBlock block = (TextFieldBlock) blocks.elementAt(i);
			int noLines = block.noLines();
			if(line < noLines) {
				LOG.debug("Block " + i + ", line " + line);
				return new BlockReference(i, (TextFieldBlock)blocks.elementAt(i), line);
			}
			line -= noLines;
		}
		throw new IllegalArgumentException("line number not valid " + line);
		
	}
	
	class BlockReference {
		int blockIndex;
		int line;
		TextFieldBlock block;
		
		public BlockReference(int blockIndex, TextFieldBlock block, int line) {
			this.blockIndex = blockIndex;
			this.block = block;
			this.line = line;
		}
	}

	/**
	 * returns the entire text of the content, with a newline between each block (but not after the final block.
	 */
	public String getText() {
		StringBuffer content = new StringBuffer();
		Enumeration e = blocks.elements();
		while (e.hasMoreElements()) {
			TextFieldBlock block = (TextFieldBlock) e.nextElement();
			if(content.length() > 0) {
				content.append("\n");
			}
			content.append(block.getText());
		}
		return content.toString();
	}

	/**
	 * returns the text on the specified line
	 */
	public String getText(int forLine) {
		BlockReference block = getBlockFor(forLine);
		return block.block.getLine(block.line);		
	}

	/**
	 * returns only the text that is selected
	 */
	public String getText(Selection selection) {
		CursorPosition from = selection.from();
		CursorPosition to = selection.to();
		
		if(from.getLine() == to.getLine()) {
			BlockReference block = getBlockFor(from.getLine());
			return block.block.getText().substring(from.getCharacter(), to.getCharacter());
			
		} else {
			throw new NotImplementedException();
		}
	}


	public void deleteLeft(CursorPosition cursorAt) {
		BlockReference block = getBlockFor(cursorAt.getLine());
		block.block.deleteLeft(block.line, cursorAt.getCharacter());
	}

	public void deleteRight(CursorPosition cursorAt) {
		BlockReference block = getBlockFor(cursorAt.getLine());
		block.block.deleteRight(block.line, cursorAt.getCharacter());
	}

	/**
	 * deletes the selected text
	 */
	public void delete(Selection selection) {
		CursorPosition from = selection.from();
		CursorPosition to = selection.to();
		
		if(from.getLine() == to.getLine()) {
			BlockReference block = getBlockFor(from.getLine());
			block.block.delete(block.line, from.getCharacter(), to.getCharacter());
			
		} else {
			throw new NotImplementedException();
		}
	}

	public void insert(CursorPosition cursorAt, String characters) {
		BlockReference block = getBlockFor(cursorAt.getLine());
		block.block.insert(block.line, cursorAt.getCharacter(), characters);
	}

	public void breakBlock(CursorPosition cursorAt) {
		 BlockReference block = getBlockFor(cursorAt.getLine());
		 TextFieldBlock newBlock = block.block.breakBlock(block.line, cursorAt.getCharacter());
		 blocks.insertElementAt(newBlock, block.blockIndex + 1);
	}
	
	public void setText(String text) {
		blocks.removeAllElements();
		
		if (text.equals("")) {
			addBlock("");
		} else {
			StringTokenizer st = new StringTokenizer(text, "\n");

			while (st.hasMoreTokens()) {
				addBlock(st.nextToken());
			}
		}
	}	
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/

