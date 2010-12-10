package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
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

	@Override
    public void perform(final PerformContext performContext)
			throws ScenarioBoundValueException {
		CellBinding thatItBinding = performContext.getPeer().getThatItBinding();
		if (!thatItBinding.isFound()) {
			CellBinding performBinding = performContext.getPeer()
					.getPerformBinding();
			throw ScenarioBoundValueException.current(performBinding, 
					"(require " + IsisViewerConstants.THAT_IT_NAME + "' column)");
		}
		ScenarioCell thatItCell = thatItBinding.getCurrentCell();
		final String thatIt = thatItCell.getText();
		final ThatSubcommand thatSubcommand = subcommandByKey.get(thatIt);
		if (thatSubcommand == null) {
			throw ScenarioBoundValueException.current(thatItBinding, 
					"(unknown '" + IsisViewerConstants.THAT_IT_NAME + "' verb)");
		}
		result = thatSubcommand.that(performContext);
	}

	@Override
    public ObjectAdapter getResult() {
		return result;
	}

	@Override
    public boolean requiresMember() {
		return requiresMember;
	}
}
