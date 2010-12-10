package net.sf.isiscontrib.bdd.fitnesse.internal.util;

import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

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

    public static void exception(Fixture fixture, ScenarioBoundValueException ex) {
        exception(fixture, ex.getStoryCell(), ex.getMessage());
    }

    public static void exception(final Fixture fixture, final ScenarioCell storyCell, final String message) {
        exception(fixture, asParse(storyCell), message);
    }

    public static Parse asParse(final ScenarioCell storyValue) {
        return (Parse) storyValue.getSource();
    }

    public static void exception(final Fixture fixture, final Parse cell, final String message) {
        fixture.exception(cell, new FitFailureException(message));
    }

    public static void setText(ScenarioCell storyCell, String str) {
        storyCell.setText(str);
    }

    public static void setBody(Parse parse, String str) {
        parse.body = str;
    }

}
