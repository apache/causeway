/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.viewer.lightweight.view;

import org.apache.log4j.Category;

class TextFieldBlock {
	private static final Category LOG = Category.getInstance(TextFieldBlock.class);
	private final TextField forField;
	private String text;
	private int[] breaks;
	private boolean formatted;
	private int noBreaks;

	TextFieldBlock(TextField forField) {
		this.forField = forField;
	}

	TextFieldBlock(TextField forField, String text) {
		this.forField = forField;
		this.text = text;
		formatted = false;
	}

	public String getLine(int line) {
		if (line < 0 || line > noBreaks) {
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
			formatted = false;
		}
	}

	public void deleteRight(int line, int character) {
		int pos = pos(line, character);
		if(pos < text.length()) {
			text = text.substring(0, pos) + text.substring(pos + 1);
			formatted = false;
		}
	}

	public void delete(int line, int fromCharacter, int toCharacter) {
		int from = pos(line, fromCharacter);
		int to = pos(line, toCharacter);
		text = text.substring(0, from) + text.substring(to);
		formatted = false;
	}

	public int noLines() {
		format();

		return noBreaks + 1;
	}

	private void breakAt(int breakAt) {
		// deal with growing array
		breaks[noBreaks] = breakAt;
		noBreaks++;
	}

	private void format() {
		if (!formatted) {
			breaks = new int[100];
			noBreaks = 0;

			int length = text.length();

			int lineWidth = 0;
			//            int start = 0;
			int breakAt = -1;

			for (int pos = 0; pos < length; pos++) {
				char ch = text.charAt(pos);

				if (ch == '\n') {
					throw new IllegalStateException("Block must not contain newline characters");
				}

				lineWidth += forField.charWidth(ch);

				if (lineWidth > forField.getMaxWidth()) {
					breakAt = (breakAt == -1) ? pos - 1 : breakAt;
					// ensures that a string without spaces doesn't loop forever
					breakAt(breakAt);

					// include the remaining chars in the starting width.
					lineWidth = forField.stringWidth(text.substring(breakAt - 1, pos + 1));

					// reset for next line
					//start = breakAt;
					breakAt = -1;

					continue;
				}

				if (ch == ' ') {
					breakAt = pos + 1; // break at the character after the space
				}
			}

			formatted = true;
		}
	}

	public void insert(int line, int character, String characters) {
		if (characters.indexOf('\n') >= 0) {
			throw new IllegalArgumentException("Insert characters cannot contain newline");
		}
		int pos = pos(line, character);
		text = text.substring(0, pos) + characters + text.substring(pos);
		formatted = false;
	}

	private int pos(int line, int character) {
		int pos = lineStart(line);
		pos += character;
		LOG.debug("position " + pos);
		return pos;
	}

	private int lineStart(int line) {
		int pos = line == 0 ? 0 : breaks[line - 1];
		LOG.debug("line " + line + " starts at " + pos);
		return pos;
	}

	private int lineEnd(int line) {
		int pos = line >= noBreaks ? text.length() : breaks[line];
		LOG.debug("line " + line + " ends at " + pos);
		return pos;
	}

	/**
	 * breaks a block at the cursor position by truncating this block and creating a new block and adding the removed text.
	 * @return
	 */
	public TextFieldBlock breakBlock(int line, int character) {
		int pos = pos(line, character);
		TextFieldBlock newBlock = new TextFieldBlock(forField, text.substring(pos));
		text = text.substring(0, pos);
		formatted = false;
		return newBlock;
	}
}
