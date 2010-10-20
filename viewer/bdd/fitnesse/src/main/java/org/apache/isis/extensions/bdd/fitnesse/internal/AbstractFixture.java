package org.apache.isis.extensions.bdd.fitnesse.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryCell;
import org.apache.isis.extensions.bdd.common.fixtures.AbstractFixturePeer;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.perform.StoryCellForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.util.FitnesseUtil;

import fit.Fixture;
import fit.Parse;

public abstract class AbstractFixture<T extends AbstractFixturePeer> extends Fixture {

	private final T peer;

    private Parse currentRow;
    private Parse table;

    protected AbstractFixture(final T fixturePeer) {
    	this.peer = fixturePeer;
    }
    
    protected T getPeer() {
    	return peer;
    }
    
    protected Parse getCurrentRow() {
        return currentRow;
    }
    protected void setCurrentRow(final Parse currentRow) {
        this.currentRow = currentRow;
    }
    
    @Override
    public void doTable(final Parse table) {
        this.table = table;
        super.doTable(table);
    }

    @Override
    public void doRows(final Parse headRow) {
        for (final CellBinding binding : getPeer().getCellBindings()) {
            ((CellBindingForFitNesse)binding).find(headRow.parts);
        }
        for (final CellBinding binding : getPeer().getCellBindings()) {
            if (!binding.isOptional() && !binding.isFound()) {
                FitnesseUtil.exception(this, table.parts.parts, "require '"
                        + binding.getName() + "' column");
            }
        }
        doRowsWithBindings(headRow);
        super.doRows(headRow.more);
    }

    /**
     * Hook method for logic to be done after finding bindings, and before
     * continuing processing.
     */
    protected void doRowsWithBindings(final Parse headRow) {}

    @Override
    public void doRow(final Parse row) {
        this.setCurrentRow(row);
        super.doRow(row);
    }

    @Override
    public void doCell(final Parse cell, final int column) {
        for (final CellBinding binding : getPeer().getCellBindings()) {
            binding.captureCurrent(new StoryCellForFitNesse(cell), column);
        }
    }

    protected void reportError(final Parse row, final Exception e) {
        final Parse errorCell = makeMessageCell(e);
        insertRowAfter(row, new Parse("tr", null, errorCell, null));
    }

    protected Parse makeMessageCell(final Exception e) {
        final Parse errorCell = new Parse("td", "", null, null);
        final StringWriter buffer = new StringWriter();

        e.printStackTrace(new PrintWriter(buffer));
        errorCell.addToTag(" colspan=\"" + getPeer().getCellBindings().size() + "\"");
        errorCell.addToBody("<i>" + e.getMessage() + "</i>");
        errorCell.addToBody("<pre>" + (buffer.toString()) + "</pre>");
        wrong(errorCell);

        return errorCell;
    }

    protected Parse makeMessageCell(final String msg) {
        final Parse cell = new Parse("td", "", null, null);
        cell.addToBody("<pre>" + msg + "</pre>");
        return cell;
    }

    protected void insertRowAfter(final Parse currentRow, final Parse rowToAdd) {
        final Parse nextRow = currentRow.more;
        currentRow.more = rowToAdd;
        rowToAdd.more = nextRow;
    }

    protected void right(StoryCell storyValue) {
    	right(FitnesseUtil.asParse(storyValue));
	}

}
