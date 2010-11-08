package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.metamodel.runtimecontext.spec.feature.ObjectActionSet;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.AddToCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckAction;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckAddToCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckClearProperty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckObject;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckProperty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckRemoveFromCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckSetProperty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.ClearProperty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetActionParameterChoices;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetActionParameterDefault;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetProperty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetPropertyChoices;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetPropertyDefault;
import org.apache.isis.viewer.bdd.common.fixtures.perform.InvokeAction;
import org.apache.isis.viewer.bdd.common.fixtures.perform.Perform;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.RemoveFromCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.SaveObject;
import org.apache.isis.viewer.bdd.common.fixtures.perform.SetProperty;
import org.apache.isis.viewer.bdd.common.util.Strings;

public class UsingNakedObjectsViewerPeer extends AbstractFixturePeer {

	private static List<Perform> performCommands(final Perform.Mode mode) {
		ArrayList<Perform> commands = new ArrayList<Perform>();

		commands.add(new CheckProperty(mode));
		commands.add(new CheckSetProperty(mode));
		commands.add(new CheckClearProperty(mode));
		commands.add(new GetProperty(mode));
		commands.add(new SetProperty(mode));
		commands.add(new ClearProperty(mode));
		commands.add(new GetPropertyDefault(mode));
		commands.add(new GetPropertyChoices(mode));

		commands.add(new CheckCollection(mode));
		commands.add(new CheckAddToCollection(mode));
		commands.add(new CheckRemoveFromCollection(mode));
		commands.add(new AddToCollection(mode));
		commands.add(new RemoveFromCollection(mode));
		commands.add(new GetCollection(mode));

		commands.add(new CheckAction(mode));
		commands.add(new InvokeAction(mode));
		commands.add(new GetActionParameterDefault(mode));
		commands.add(new GetActionParameterChoices(mode));

		commands.add(new CheckObject(mode));
		commands.add(new SaveObject(mode));

		return commands;
	}

	private final CellBinding onObjectBinding;
	private final CellBinding aliasResultAsBinding;
	private final CellBinding performBinding;
	private final CellBinding onMemberBinding;
	private final CellBinding thatItBinding;
	private final CellBinding arg0Binding;

	private final Map<String, Perform> commandByKey = new HashMap<String, Perform>();

	public UsingNakedObjectsViewerPeer(final AliasRegistry aliasesRegistry,
			final Perform.Mode mode, final CellBinding onObjectBinding,
			final CellBinding aliasResultAsBinding,
			final CellBinding performBinding,
			final CellBinding onMemberBinding, final CellBinding thatItBinding,
			final CellBinding arg0Binding) {
		super(aliasesRegistry, onObjectBinding, aliasResultAsBinding,
				performBinding, onMemberBinding, thatItBinding, arg0Binding);

		this.onObjectBinding = onObjectBinding;
		this.aliasResultAsBinding = aliasResultAsBinding;
		this.performBinding = performBinding;
		this.onMemberBinding = onMemberBinding;
		this.thatItBinding = thatItBinding;
		this.arg0Binding = arg0Binding;

		final List<Perform> performCommands = performCommands(mode);
		for (final Perform command : performCommands) {
			commandByKey.put(command.getKey(), command);
		}
	}

	public CellBinding getOnObjectBinding() {
		return onObjectBinding;
	}

	public CellBinding getAliasResultAsBinding() {
		return aliasResultAsBinding;
	}

	public CellBinding getOnMemberBinding() {
		return onMemberBinding;
	}

	public CellBinding getPerformBinding() {
		return performBinding;
	}

	public CellBinding getThatItBinding() {
		return thatItBinding;
	}

	public CellBinding getArg0Binding() {
		return arg0Binding;
	}

	public boolean isArg0BindingLast() {
		return !bindingAfterArg0();

	}

	private boolean bindingAfterArg0() {
		if (!getArg0Binding().isFound()) {
			return false;
		}
		for (final CellBinding binding : getCellBindings()) {
			if (binding.getColumn() > getArg0Binding().getColumn()) {
				return true;
			}
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////
	// 
	// //////////////////////////////////////////////////////////////////

	public void makePersistent(ObjectAdapter adapter) {
		getPersistenceSession().makePersistent(adapter);
	}

	public void provideDefault(StoryCell storySource, String resultStr) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	private String previousOnObject = null;

	public ObjectAdapter validateOnObject() throws StoryBoundValueException {

		StoryCell onObjectCell = onObjectBinding.getCurrentCell();
		String onObject = onObjectCell.getText();
		if (onObject == null) {
			if (previousOnObject == null) {
				throw StoryBoundValueException.current(onObjectBinding, "(required)");
			}
			onObject = previousOnObject;
		} else {
			previousOnObject = onObject;
		}
		final ObjectAdapter onAdapter = getAliasRegistry().getAliased(onObject);
		if (onAdapter == null) {
			throw StoryBoundValueException.current(onMemberBinding, "(unknown object)");
		}
		return onAdapter;
	}

	public String validateAliasAs() throws StoryBoundValueException {
		if (getAliasResultAsBinding() == null) {
			return null;
		}
		final StoryCell aliasCell = aliasResultAsBinding
				.getCurrentCell();
		if (aliasCell == null) {
			return null;
		}

		String aliasAs = aliasCell.getText();
		if (getAliasRegistry().getAliased(aliasAs) != null) {
			throw StoryBoundValueException.current(aliasResultAsBinding, "(already used)");
		}
		return aliasAs;
	}

	public ObjectMember validateOnMember(ObjectAdapter onAdapter)
			throws StoryBoundValueException {

		final StoryCell onMemberCell = onMemberBinding.getCurrentCell();
		final String onMember = onMemberCell.getText();

		if (Strings.emptyString(onMember)) {
			throw StoryBoundValueException.current(onMemberBinding, "(required)");
		}

		if (onAdapter == null) {
			return null;
		}

		// see if property, collection or action.
		final String memberId = Strings.memberIdFor(onMember);
		final ObjectSpecification spec = onAdapter.getSpecification();
		final List<ObjectMember> objectMembers = new ArrayList<ObjectMember>();

		objectMembers.addAll(spec.getAssociationList());

		// see if action (of any type)
		objectMembers.addAll(spec
				.getObjectActionList(ObjectActionType.USER));
		objectMembers.addAll(spec
				.getObjectActionList(ObjectActionType.EXPLORATION));
		objectMembers.addAll(spec
				.getObjectActionList(ObjectActionType.DEBUG));
		for (final ObjectMember member : objectMembers) {
			if (matchesId(member, memberId)) {
				return member;
			}
			// special handling for contributed actions.
			if (member instanceof ObjectActionSet) {
				final ObjectActionSet actionSet = (ObjectActionSet) member;
				for (final ObjectAction contributedAction : actionSet
						.getActions()) {
					if (contributedAction.getId().equals(memberId)) {
						return contributedAction;
					}
				}
			}
		}
		throw StoryBoundValueException.current(onMemberBinding, "(unknown member)");
	}

	private boolean matchesId(final ObjectMember member,
			final String memberId) {
		return member.getId().equals(memberId);
	}

	public Perform validatePerform() throws StoryBoundValueException {
		final String perform = performBinding.getCurrentCell()
				.getText();
		if (perform == null) {
			throw StoryBoundValueException.current(performBinding, "(required)");
		}
		final Perform performCommand = commandByKey.get(perform);
		if (performCommand == null) {
			throw StoryBoundValueException.current(performBinding, "(unknown interaction)");
		}
		return performCommand;
	}

	private void aliasResultFromPerformCommand(Perform performCommand,
			String aliasAs) throws StoryBoundValueException {
		if (Strings.emptyString(aliasAs)) {
			return;
		}
		final ObjectAdapter resultAdapter = performCommand.getResult();
		if (resultAdapter == null) {
			throw StoryBoundValueException.current(onMemberBinding, "(no result)");
		}
		getAliasRegistry().aliasAs(aliasAs, resultAdapter);
	}

	public StoryCell getMemberElseOnObjectCell() {
		StoryCell storyCell = getOnMemberBinding().getCurrentCell();
		if (storyCell == null) {
			storyCell = getOnObjectBinding().getCurrentCell();
		}
		return storyCell;
	}

	public void performCommand(Perform performCommand, ObjectAdapter onAdapter,
			ObjectMember nakedObjectMember,
			List<StoryCell> argumentStoryCells, 
			String aliasAs)
			throws StoryBoundValueException {
		PerformContext performContext = new PerformContext(this, onAdapter,
				nakedObjectMember, argumentStoryCells);
		try {
			performCommand.perform(performContext);
		} catch (final RuntimeException ex) {
			// handler should have colored in invalid cells.
		}
		aliasResultFromPerformCommand(performCommand, aliasAs);
	}

	public ObjectAdapter getAdapter(final ObjectAdapter contextAdapter,
			final ObjectSpecification noSpec, final CellBinding contextBinding, final StoryCell paramCell)
			throws StoryBoundValueException {

		final String cellText = paramCell.getText();

		// see if can handle as parseable value
		final ParseableFacet parseableFacet = noSpec
				.getFacet(ParseableFacet.class);
		if (parseableFacet != null) {
			try {
				return parseableFacet.parseTextEntry(contextAdapter, cellText);
			} catch (final IllegalArgumentException ex) {
				throw StoryBoundValueException.arg(contextBinding, paramCell, "(cannot parse)");
			}
		}

		// otherwise, handle as reference to known object
		final ObjectAdapter adapter = getAliasRegistry().getAliased(cellText);
		if (adapter == null) {
			throw StoryBoundValueException.arg(contextBinding, paramCell, "(unknown reference)");
		}

		return adapter;
	}

	/**
	 * Ensures that there are at least enough arguments for the number of parameters required.
	 */
	public ObjectAdapter[] getAdapters(final ObjectAdapter onAdapter,
			final ObjectAction nakedObjectAction,
			CellBinding onMemberBinding, final List<StoryCell> argumentCells) throws StoryBoundValueException {
		final ObjectActionParameter[] parameters = nakedObjectAction
				.getParameters();
		
		int parameterCount = parameters.length;
		if (argumentCells.size() < parameterCount) {
			throw StoryBoundValueException.current(onMemberBinding, 
					"(action requires " + parameterCount + " arguments)");
		}
		final ObjectAdapter[] adapters = new ObjectAdapter[parameterCount];


		for (int i = 0; i < parameterCount; i++) {
			final StoryCell paramCell = argumentCells.get(i);
			final ObjectActionParameter parameter = parameters[i];
			adapters[i] = getAdapter(null, parameter.getSpecification(),
					onMemberBinding, paramCell);
		}
		return adapters;
	}

	public ObjectAdapter toAdaptedListOfPojos(final ObjectAdapter[] choiceAdapters) {
		final List<Object> choiceList = new ArrayList<Object>();
		if (choiceAdapters != null) {
			for (final ObjectAdapter adapter : choiceAdapters) {
				choiceList.add(adapter.getObject());
			}
		}
		return getAdapterManager().adapterFor(choiceList);
	}

}
