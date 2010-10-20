package org.apache.isis.extensions.bdd.fitnesse.internal.util;

import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.StoryCell;

import fit.Fixture;
import fit.Parse;
import fit.exception.FitFailureException;

public final class FitnesseUtil {

	private FitnesseUtil() {
	}

	public static Parse cell(final Parse row, final int columnNumber) {
		Parse cell = row.parts;
		if (cell == null) {
			return cell;
		}
		int i = 0;
		while (true) {
			if (columnNumber == i) {
				return cell;
			}
			if (cell.more == null) {
				return cell;
			}
			cell = cell.more;
			i++;
		}
	}

	public static void exception(Fixture fixture, StoryBoundValueException ex) {
		exception(fixture, ex.getStoryCell(), ex.getMessage());
	}

	public static void exception(final Fixture fixture,
			final StoryCell storyCell, final String message) {
		exception(fixture, asParse(storyCell), message);
	}

	public static Parse asParse(final StoryCell storyValue) {
		return (Parse) storyValue.getSource();
	}

	public static void exception(final Fixture fixture, final Parse cell,
			final String message) {
		fixture.exception(cell, new FitFailureException(message));
	}

	public static void setText(StoryCell storyCell, String str) {
		storyCell.setText(str);
	}

	public static void setBody(Parse parse, String str) {
		parse.body = str;
	}

}
