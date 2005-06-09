package org.nakedobjects.viewer.skylark.text;

import org.apache.log4j.Logger;

class TextBlock {
	private static final Logger LOG = Logger.getLogger(TextBlock.class);
	private final TextBlockTarget forField;
	private String text;
	private int[] lineBreaks;
	private boolean isFormatted;
	private int lineCount;
    private boolean canWrap;

	TextBlock(TextBlockTarget forField, String text, boolean canWrap) {
		this.forField = forField;
		this.text = text;
		isFormatted = false;
		this.canWrap = canWrap;
	}

	public String getLine(int line) {
		if (line < 0 || line > lineCount) {
			throw new IllegalArgumentException("line outside of block " + line);
		}

		format();

		int from = lineStart(line);
		int to = lineEnd(line);

		return text.substring(from, to);
	}

	public String getText() {
		return text;
	}

	public void deleteLeft(int line, int character) {
		int pos = pos(line, character);
		if(pos > 0) {
			text = text.substring(0, pos - 1) + text.substring(pos);
			isFormatted = false;
		}
	}

	public void deleteRight(int line, int character) {
		int pos = pos(line, character);
		if(pos < text.length()) {
			text = text.substring(0, pos) + text.substring(pos + 1);
			isFormatted = false;
		}
	}

	public void delete(int line, int fromCharacter, int toCharacter) {
		int from = pos(line, fromCharacter);
		int to = pos(line, toCharacter);
		text = text.substring(0, from) + text.substring(to);
		isFormatted = false;
	}

	public int noLines() {
		format();
		return lineCount + 1;
	}

	private void breakAt(int breakAt) {
		// TODO deal with growing array
		lineBreaks[lineCount] = breakAt;
		lineCount++;
	}

	private void format() {
		if (canWrap && !isFormatted) {
			lineBreaks = new int[100];
			lineCount = 0;

			int length = text.length();

			int lineWidth = 0;
			int breakAt = -1;

			for (int pos = 0; pos < length; pos++) {
				char ch = text.charAt(pos);

				if (ch == '\n') {
					throw new IllegalStateException("Block must not contain newline characters");
				}

				lineWidth += forField.getText().charWidth(ch);

				if (lineWidth > forField.getMaxWidth()) {
					breakAt = (breakAt == -1) ? pos - 1 : breakAt;
					// ensures that a string without spaces doesn't loop forever
					breakAt(breakAt);

					// include the remaining chars in the starting width.
					lineWidth = forField.getText().stringWidth(text.substring(breakAt - 1, pos + 1));

					// reset for next line
					//start = breakAt;
					breakAt = -1;

					continue;
				}

				if (ch == ' ') {
					breakAt = pos + 1; // break at the character after the space
				}
			}

			isFormatted = true;
		}
	}

	public void insert(int line, int character, String characters) {
		if (characters.indexOf('\n') >= 0) {
			throw new IllegalArgumentException("Insert characters cannot contain newline");
		}
		int pos = pos(line, character);
		text = text.substring(0, pos) + characters + text.substring(pos);
		isFormatted = false;
	}

	private int pos(int line, int character) {
		int pos = lineStart(line);
		pos += character;
		LOG.debug("position " + pos);
		return pos;
	}

	private int lineStart(int line) {
		int pos = line == 0 ? 0 : lineBreaks[line - 1];
		LOG.debug("line " + line + " starts at " + pos);
		return pos;
	}

	private int lineEnd(int line) {
		int pos = line >= lineCount ? text.length() : lineBreaks[line];
		LOG.debug("line " + line + " ends at " + pos);
		return pos;
	}

	/**
	 * breaks a block at the cursor position by truncating this block and creating a new block and adding the removed text.
	 */
	public TextBlock breakBlock(int line, int character) {
	    format();
		int pos = pos(line, character);
		TextBlock newBlock = new TextBlock(forField, text.substring(pos), canWrap);
		text = text.substring(0, pos);
		isFormatted = false;
		return newBlock;
	}
	
	public void setCanWrap(boolean canWrap) {
        this.canWrap = canWrap;
    }
	
	public String toString() {
	    StringBuffer content = new StringBuffer();
	    content.append("TextBlock [");
	    content.append("formatted=");
	    content.append(isFormatted);
	    content.append(",lines=");
	    content.append(lineCount);
	    content.append(",text=");
	    content.append(text);
	    content.append(",breaks=");
	    if(lineBreaks == null) {
	        content.append("none");
	    } else {
		    for (int i = 0; i < lineBreaks.length; i++) {
	            content.append(i == 0 ? "" : ",");
	            content.append(lineBreaks[i]);
	        }
	    }
	    content.append("]");
        return content.toString();
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

