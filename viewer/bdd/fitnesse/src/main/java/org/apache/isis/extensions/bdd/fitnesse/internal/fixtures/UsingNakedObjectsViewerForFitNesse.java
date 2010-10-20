package org.apache.isis.extensions.bdd.fitnesse.internal.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.Constants;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.StoryCell;
import org.apache.isis.extensions.bdd.common.fixtures.UsingNakedObjectsViewerPeer;
import org.apache.isis.extensions.bdd.common.fixtures.perform.Perform;
import org.apache.isis.extensions.bdd.fitnesse.internal.AbstractFixture;
import org.apache.isis.extensions.bdd.fitnesse.internal.CellBindingForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.perform.StoryCellForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.util.FitnesseUtil;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.feature.ObjectMember;

import com.google.common.collect.Lists;

import fit.Parse;
import fit.exception.FitFailureException;

public class UsingNakedObjectsViewerForFitNesse extends
		AbstractFixture<UsingNakedObjectsViewerPeer> {

	private final List<Parse> argumentCells = new ArrayList<Parse>();

	public UsingNakedObjectsViewerForFitNesse(
			final AliasRegistry aliasesRegistry,
			final Perform.Mode mode) {
		this(aliasesRegistry, mode, 
				CellBindingForFitNesse.builder(
						Constants.ON_OBJECT_NAME, Constants.ON_OBJECT_HEAD_SET)
						.ditto().build(),
				CellBindingForFitNesse.builder(
						Constants.ALIAS_RESULT_NAME, Constants.ALIAS_RESULT_HEAD_SET)
						.optional().build(), 
				CellBindingForFitNesse.builder(
						Constants.PERFORM_NAME, Constants.PERFORM_HEAD_SET)
						.ditto().build(), 
				CellBindingForFitNesse.builder(
						Constants.ON_MEMBER_NAME, Constants.ON_MEMBER_HEAD_SET)
						.optional().build(),
				CellBindingForFitNesse.builder(
						Constants.THAT_IT_NAME, Constants.THAT_IT_HEAD_SET)
						.ditto().optional().build(), 
				CellBindingForFitNesse.builder(
						Constants.WITH_ARGUMENTS_NAME, Constants.WITH_ARGUMENTS_HEAD_SET)
						.optional().build());
	}

	private UsingNakedObjectsViewerForFitNesse(
			final AliasRegistry aliasesRegistry,
			final Perform.Mode mode,
			final CellBinding onObjectBinding,
			final CellBinding aliasResultAsBinding,
			final CellBinding performBinding,
			final CellBinding onMemberBinding, final CellBinding thatItBinding,
			final CellBinding arg0Binding) {
		super(new UsingNakedObjectsViewerPeer(aliasesRegistry, mode,
				onObjectBinding, aliasResultAsBinding, performBinding,
				onMemberBinding, thatItBinding, arg0Binding));
	}

	@Override
	public void doRows(final Parse headRow) {
		super.doRows(headRow);
		ensureArgBindingLast();
	}

	private void ensureArgBindingLast() {
		if (getPeer().isArg0BindingLast()) {
			return;
		}
		FitnesseUtil.exception(this, getPeer().getArg0Binding().getHeadCell(),
				"(must be last column)");
		throw new FitFailureException("(invalid binding order)");
	}

	@Override
	public void doRow(final Parse row) {
		argumentCells.clear();
		super.doRow(row);
		execute();
	}

	@Override
	public void doCell(final Parse cell, final int columnNum) {
		super.doCell(cell, columnNum);

		captureArgumentCellsIfAny(cell, columnNum);
	}

	private void captureArgumentCellsIfAny(final Parse cell, final int columnNum) {
		if (!getPeer().getArg0Binding().isFound()) {
			return;
		}
		if (columnNum < getPeer().getArg0Binding().getColumn()) {
			return;
		}
		argumentCells.add(cell);
	}

	private void execute() {

		ObjectAdapter onAdapter = null;
		try {
			onAdapter = getPeer().validateOnObject();
		} catch (StoryBoundValueException ex) {
			FitnesseUtil.exception(this, ex);
			return;
		}

		String aliasAs = null;
		try {
			aliasAs = getPeer().validateAliasAs();
		} catch (StoryBoundValueException ex) {
			FitnesseUtil.exception(this, ex);
			return;
		}

		Perform performCommand = null;
		try {
			performCommand = getPeer().validatePerform();
		} catch (StoryBoundValueException ex) {
			FitnesseUtil.exception(this, ex);
			return;
		}

		ObjectMember nakedObjectMember = null;
		if (performCommand.requiresMember()) {
			try {
				nakedObjectMember = getPeer().validateOnMember(onAdapter);
			} catch (StoryBoundValueException ex) {
				FitnesseUtil.exception(this, ex);
				return;
			}
		}

		try {
			List<StoryCell> argumentStoryCells = asValues(argumentCells);
			getPeer().performCommand(performCommand, onAdapter, nakedObjectMember,
					argumentStoryCells, aliasAs);
		} catch (StoryBoundValueException ex) {
			FitnesseUtil.exception(this, ex);
			return;
		}

		rightMemberElseOnObjectCell();
	}

	private static List<StoryCell> asValues(List<Parse> argCells) {
		List<StoryCell> storyValues = Lists.newArrayList();
		for (Parse parse : argCells) {
			storyValues.add(new StoryCellForFitNesse(parse));
		}
		return storyValues;
	}

	private void rightMemberElseOnObjectCell() {
		right(getPeer().getMemberElseOnObjectCell());
	}


}
