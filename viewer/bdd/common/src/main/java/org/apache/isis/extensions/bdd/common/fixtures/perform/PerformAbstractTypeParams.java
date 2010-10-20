package org.apache.isis.extensions.bdd.common.fixtures.perform;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.StoryCell;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;

public abstract class PerformAbstractTypeParams extends PerformAbstract {

	protected static enum Type {
		PROPERTY(true) {
			@Override
			String ensureMemberIsOfType(final ObjectMember m) {
				return m instanceof OneToOneAssociation ? null
						: "(not a property)";
			}
		},
		COLLECTION(true) {
			@Override
			String ensureMemberIsOfType(final ObjectMember m) {
				return m instanceof OneToManyAssociation ? null
						: "(not a collection)";
			}
		},
		ACTION(true) {
			@Override
			String ensureMemberIsOfType(final ObjectMember m) {
				return m instanceof ObjectAction ? null
						: "(not an action)";
			}
		},
		OBJECT(false) {
			@Override
			String ensureMemberIsOfType(final ObjectMember m) {
				return m != null ? "(not required)" : null;
			}
		};
//		public void ensureMemberIsOfType(
//				final NakedObjectMember nakedObjectMember,
//				final StoryCell onMemberCell) throws StoryFailureException {
//			final String msg = ensureMemberIsOfType(nakedObjectMember);
//			if (msg != null) {
//				throw new StoryFailureException(onMemberCell, msg);
//			}
//		}

		abstract String ensureMemberIsOfType(ObjectMember m);

		private final boolean representsMember;

		private Type(final boolean representsMember) {
			this.representsMember = representsMember;
		}

		public boolean representsMember() {
			return representsMember;
		}
	}

	protected static enum NumParameters {
		ZERO, ONE, UNLIMITED;
	}

	private final Type type;
	private final NumParameters numParameters;

	public PerformAbstractTypeParams(final String key, final Type type,
			final NumParameters numParameters, final Perform.Mode mode) {
		super(key, mode);
		this.type = type;
		this.numParameters = numParameters;
	}

	public boolean requiresMember() {
		return type.representsMember();
	}

	/**
	 * Can be overridden, but provides basic checking that member is of correct
	 * type and, if taking {@link NumParameters#ZERO zero} or
	 * {@link NumParameters#ONE one} parameter, that the correct number of
	 * actual arguments match.
	 */
	public void perform(final PerformContext performContext)
			throws StoryBoundValueException {

		CellBinding onMemberBinding = performContext.getPeer()
				.getOnMemberBinding();
		final StoryCell onMemberCell = onMemberBinding.getCurrentCell();

		final String reason = type.ensureMemberIsOfType(performContext
				.getNakedObjectMember());
		if (reason != null) {
			throw StoryBoundValueException.current(onMemberBinding, 
					reason);
		}

		if (type.representsMember()) {

			if (getMode() == Perform.Mode.TEST) {
				performContext.ensureVisible(onMemberBinding, onMemberCell);
				performContext.ensureUsable(onMemberBinding, onMemberCell);
			}

			// validate we have correct number of parameters if zero or one.
			final int numArgs = performContext.getArgumentCells().size();

			// don't do these two checks, because there could be additional
			// cells due to previous or subsequent rows in the table 
			// (with the author keeping this 'tidy')

			// if (numParameters == NumParameters.ZERO && length > 0) {
			// ... "(no arguments required)"
			// }
			// if (numParameters == NumParameters.ONE && length > 1) {
			// ... "(need just 1 argument)"
			// }

			// okay to do this check, though
			if (numParameters == NumParameters.ONE && numArgs < 1) {
				throw StoryBoundValueException.current(onMemberBinding, 
						"(need one argument)");
			}
		}

		doHandle(performContext);
	}

	/**
	 * Hook method that does nothing; should be overridden if
	 * {@link #perform(PerformContextForFitNesse) handle(...)} method has not
	 * been.
	 */
	protected void doHandle(final PerformContext performContext)
			throws StoryBoundValueException {
		// does nothing
	}

//	/**
//	 * Convenience; delegates to
//	 * {@link Type#ensureMemberIsOfType(NakedObjectMember)} and downcasts.
//	 */
//	protected OneToOneAssociation ensureMemberIsProperty(
//			final NakedObjectMember nakedObjectMember,
//			final StoryCell usingMemberCell) throws StoryFailureException {
//		type.ensureMemberIsOfType(nakedObjectMember, usingMemberCell);
//		return (OneToOneAssociation) nakedObjectMember;
//	}
//
//	/**
//	 * Convenience; delegates to
//	 * {@link Type#ensureMemberIsOfType(NakedObjectMember)} and downcasts.
//	 */
//	protected OneToManyAssociation ensureMemberIsCollection(
//			final NakedObjectMember nakedObjectMember,
//			final StoryCell usingMemberCell) throws StoryFailureException {
//		type.ensureMemberIsOfType(nakedObjectMember, usingMemberCell);
//		return (OneToManyAssociation) nakedObjectMember;
//	}
//
//	/**
//	 * Convenience; delegates to
//	 * {@link Type#ensureMemberIsOfType(NakedObjectMember)} and downcasts.
//	 */
//	protected NakedObjectAction ensureMemberIsAction(
//			final NakedObjectMember nakedObjectMember,
//			final StoryCell usingMemberCell) throws StoryFailureException {
//		type.ensureMemberIsOfType(nakedObjectMember, usingMemberCell);
//		return (NakedObjectAction) nakedObjectMember;
//	}

}
