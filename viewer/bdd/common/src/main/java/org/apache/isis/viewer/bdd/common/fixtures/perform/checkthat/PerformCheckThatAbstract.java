package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.Constants;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.Perform;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformAbstract;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;

public abstract class PerformCheckThatAbstract extends PerformAbstract {

	protected static enum OnMemberColumn {
		REQUIRED, NOT_REQUIRED
	}

	private final Map<String, ThatSubcommand> subcommandByKey = new HashMap<String, ThatSubcommand>();
	private ObjectAdapter result;
	private final boolean requiresMember;

	public PerformCheckThatAbstract(final String key,
			final OnMemberColumn onMemberColumn, final Perform.Mode mode,
			final ThatSubcommand... thatSubcommands) {
		super(key, mode);
		requiresMember = onMemberColumn == OnMemberColumn.REQUIRED;
		for (final ThatSubcommand thatSubcommand : thatSubcommands) {
			for (final String subKey : thatSubcommand.getSubkeys()) {
				subcommandByKey.put(subKey, thatSubcommand);
			}
		}
	}

	public void perform(final PerformContext performContext)
			throws StoryBoundValueException {
		CellBinding thatItBinding = performContext.getPeer().getThatItBinding();
		if (!thatItBinding.isFound()) {
			CellBinding performBinding = performContext.getPeer()
					.getPerformBinding();
			throw StoryBoundValueException.current(performBinding, 
					"(require " + Constants.THAT_IT_NAME + "' column)");
		}
		StoryCell thatItCell = thatItBinding.getCurrentCell();
		final String thatIt = thatItCell.getText();
		final ThatSubcommand thatSubcommand = subcommandByKey.get(thatIt);
		if (thatSubcommand == null) {
			throw StoryBoundValueException.current(thatItBinding, 
					"(unknown '" + Constants.THAT_IT_NAME + "' verb)");
		}
		result = thatSubcommand.that(performContext);
	}

	public ObjectAdapter getResult() {
		return result;
	}

	public boolean requiresMember() {
		return requiresMember;
	}
}
