package org.apache.isis.extensions.bdd.concordion.internal.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.Constants;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.StoryCell;
import org.apache.isis.extensions.bdd.common.fixtures.UsingNakedObjectsViewerPeer;
import org.apache.isis.extensions.bdd.common.fixtures.perform.Perform;
import org.apache.isis.extensions.bdd.concordion.internal.fixtures.bindings.CellBindingForConcordion;
import org.apache.isis.extensions.bdd.concordion.internal.fixtures.perform.StoryCellForConcordion;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.feature.ObjectMember;

import com.google.common.collect.Lists;

public class UsingNakedObjectsViewerForConcordion extends
		AbstractFixture<UsingNakedObjectsViewerPeer> {

	private final List<String> argumentCells = new ArrayList<String>();

	public UsingNakedObjectsViewerForConcordion(
			final AliasRegistry aliasesRegistry,
			final Perform.Mode mode) {
		this(aliasesRegistry, mode, 
				CellBindingForConcordion.builder(
						Constants.ON_OBJECT_NAME, Constants.ON_OBJECT_HEAD_SET)
						.ditto().build(),
				CellBindingForConcordion.builder(
						Constants.ALIAS_RESULT_NAME, Constants.ALIAS_RESULT_HEAD_SET)
						.optional().build(), 
				CellBindingForConcordion.builder(
						Constants.PERFORM_NAME, Constants.PERFORM_HEAD_SET)
						.ditto().build(), 
				CellBindingForConcordion.builder(
						Constants.ON_MEMBER_NAME, Constants.ON_MEMBER_HEAD_SET)
						.optional().build(),
				CellBindingForConcordion.builder(
						Constants.THAT_IT_NAME, Constants.THAT_IT_HEAD_SET)
						.ditto().optional().build(), 
				CellBindingForConcordion.builder(
						Constants.WITH_ARGUMENTS_NAME, Constants.WITH_ARGUMENTS_HEAD_SET)
						.optional().build());
	}

	private UsingNakedObjectsViewerForConcordion(
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

	public String executeHeader(String onObject, String aliasResultAs,
			String perform, String usingMember, String thatIt, String arg0, String... remainingArgs) {

		return setupHeader(onObject, aliasResultAs, perform, usingMember,
				thatIt, arg0);
	}

	private String setupHeader(String onObject, String aliasResultAs,
			String perform, String usingMember, String thatIt, String arg0) {
		int colNum = 0;
		getPeer().getOnObjectBinding().setHeadColumn(colNum++);
		getPeer().getAliasResultAsBinding().setHeadColumn(colNum++);
		getPeer().getPerformBinding().setHeadColumn(colNum++);
		getPeer().getOnMemberBinding().setHeadColumn(colNum++);
		if (thatIt != null) {
			getPeer().getThatItBinding().setHeadColumn(colNum++);
		}
		if (arg0 != null) {
			getPeer().getArg0Binding().setHeadColumn(colNum++);
		}
		
		return ""; // ok
	}

	public String executeRow(String onObject, String aliasResultAs,
			String perform, String usingMember, String thatIt, String arg0, String... remainingArgs) {

		setupHeader(onObject, aliasResultAs, perform, usingMember,
				thatIt, arg0);

		// capture current
		getPeer().getOnObjectBinding().captureCurrent(new StoryCellForConcordion(onObject));
		getPeer().getAliasResultAsBinding().captureCurrent(new StoryCellForConcordion(aliasResultAs));
		getPeer().getPerformBinding().captureCurrent(new StoryCellForConcordion(perform));
		getPeer().getOnMemberBinding().captureCurrent(new StoryCellForConcordion(usingMember));
		if (getPeer().getThatItBinding().isFound()) {
			getPeer().getThatItBinding().captureCurrent(new StoryCellForConcordion(thatIt));
		}
		if (getPeer().getArg0Binding().isFound()) {
			getPeer().getArg0Binding().captureCurrent(new StoryCellForConcordion(arg0));
			argumentCells.add(arg0);
		}
		for (String arg: remainingArgs) {
			argumentCells.add(arg);
		}
		
		// execute
		try {
			execute();
		} catch (StoryBoundValueException ex) {
			return ex.getMessage();
		}
		
		return "ok";
	}

	private void execute() throws StoryBoundValueException {

		ObjectAdapter onAdapter = getPeer().validateOnObject();
		String aliasAs = getPeer().validateAliasAs();
		Perform performCommand = getPeer().validatePerform();
		
		ObjectMember nakedObjectMember = null;
		if (performCommand.requiresMember()) {
			nakedObjectMember = getPeer().validateOnMember(onAdapter);
		}

		getPeer().performCommand(performCommand, onAdapter, nakedObjectMember, asValues(argumentCells), aliasAs);
	}

	private static List<StoryCell> asValues(List<String> argumentCells) {
		List<StoryCell> storyValues = Lists.newArrayList();
		for (String arg : argumentCells) {
			storyValues.add(new StoryCellForConcordion(arg));
		}
		return storyValues;
	}
}
