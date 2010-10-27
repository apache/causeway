package org.apache.isis.viewer.bdd.concordion.internal.concordion;

import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.CommandCall;
import org.concordion.internal.Row;
import org.concordion.internal.TableSupport;
import org.concordion.internal.command.ExecuteCommand;

public class ExecuteCommandWithHeader extends ExecuteCommand {

	public static enum Context {
		TABLE, INLINE
	}

	public static enum TableRow {
		HEADER, BODY
	}

	/**
	 * Provides a mechanism for the fixture to determine whether the command is
	 * being executed in the context of a table or inline.
	 */
	public static ThreadLocal<Context> context = new ThreadLocal<Context>() {
		@Override
		protected Context initialValue() {
			return null;
		}
	};

	/**
	 * When executing in a {@link Context#TABLE table context}, provides a
	 * mechanism for the fixture to determine whether the header of a table or
	 * the body is being processed.
	 */
	public static ThreadLocal<TableRow> tableRow = new ThreadLocal<TableRow>() {
		@Override
		protected TableRow initialValue() {
			return TableRow.BODY;
		}
	};

	@Override
	public void execute(CommandCall commandCall, Evaluator evaluator,
			ResultRecorder resultRecorder) {
		// we're gonna handle tables slightly differently from Concordion's
		// usual strategy
		if (commandCall.getElement().isNamed("table")) {
			Context contextIfAny = context.get();
			boolean setContext = contextIfAny == null; 
			if (setContext) {
				context.set(Context.TABLE);
			}
			executeTable(commandCall, evaluator, resultRecorder);
			if (setContext) {
				context.set(null);
			}
		} else {
			Context contextIfAny = context.get();
			boolean setContext = contextIfAny == null;
			if (setContext) {
				context.set(Context.INLINE);
			}
			super.execute(commandCall, evaluator, resultRecorder);
			if (setContext) {
				context.set(null);
			}
		}
	}

	private void executeTable(CommandCall commandCall, Evaluator evaluator,
			ResultRecorder resultRecorder) {
		TableSupport tableSupport = new TableSupport(commandCall);

		// also execute on the header
		tableRow.set(TableRow.HEADER);
		Row headerRow = tableSupport.getLastHeaderRow();
		commandCall.setElement(headerRow.getElement());
		commandCall.execute(evaluator, resultRecorder);

		// the rest is copied from Concordion's original implementation
		tableRow.set(TableRow.BODY);
		Row[] detailRows = tableSupport.getDetailRows();
		for (Row detailRow : detailRows) {
			if (detailRow.getCells().length != tableSupport.getColumnCount()) {
				throw new RuntimeException(
						"The <table> 'execute' command only supports rows with an equal number of columns.");
			}
			commandCall.setElement(detailRow.getElement());
			tableSupport.copyCommandCallsTo(detailRow);
			commandCall.execute(evaluator, resultRecorder);
		}
		tableRow.set(null);
	}

}
