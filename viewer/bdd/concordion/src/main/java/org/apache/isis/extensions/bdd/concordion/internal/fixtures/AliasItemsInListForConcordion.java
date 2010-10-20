package org.apache.isis.extensions.bdd.concordion.internal.fixtures;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.Constants;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.StoryValueException;
import org.apache.isis.extensions.bdd.common.fixtures.AliasItemsInListPeer;
import org.apache.isis.extensions.bdd.concordion.internal.fixtures.bindings.CellBindingForConcordion;
import org.apache.isis.extensions.bdd.concordion.internal.fixtures.perform.StoryCellForConcordion;

public class AliasItemsInListForConcordion extends
		AbstractFixture<AliasItemsInListPeer> {

	public AliasItemsInListForConcordion(final AliasRegistry aliasRegistry,
			final String listAlias) {
		this(aliasRegistry, listAlias, CellBindingForConcordion.builder(
				Constants.TITLE_NAME, Constants.TITLE_HEAD).build(),
				CellBindingForConcordion.builder(Constants.TYPE_NAME,
						Constants.TYPE_HEAD).optional().build(),
				CellBindingForConcordion.builder(Constants.ALIAS_RESULT_NAME,
						Constants.ALIAS_RESULT_HEAD_SET).autoCreate().build());
	}

	private AliasItemsInListForConcordion(final AliasRegistry aliasRegistry,
			final String listAlias, final CellBinding titleBinding,
			final CellBinding typeBinding, final CellBinding aliasBinding) {
		super(new AliasItemsInListPeer(aliasRegistry, listAlias,
				titleBinding, typeBinding, aliasBinding));
	}

	public String execute(String aliasAs, String title, String type) {
		String header = executeHeader(aliasAs, title, type);
		if (header != null) {
			return header;
		}
		
		String row = executeRow(aliasAs, title, type);
		if (row != null) {
			return row;
		}
		
		return "ok"; // ok
	}

	private String executeHeader(String alias, String title, String type) {
		try {
			getPeer().assertIsList();
		} catch (StoryValueException e) {
			return e.getMessage();
		}

		// create bindings
		getPeer().getTitleBinding().setHeadColumn(0);
		getPeer().getAliasBinding().setHeadColumn(1);

		if (type != null) {
			getPeer().getTypeBinding().foundHeadColumn(2,
					new StoryCellForConcordion(type));
		}
		
		return null;
	}

	private String executeRow(String aliasAs, String title, String type) {
		if (!getPeer().isList()) {
			return null; // skip
		}
		
		captureCurrent(aliasAs, title, type);
		
		try {
			getPeer().findAndAlias();
		} catch (StoryBoundValueException e) {
			return e.getMessage();
		}
		
		return null;
	}

	private void captureCurrent(String aliasAs, String title, String type) {
		getPeer().getAliasBinding().captureCurrent(new StoryCellForConcordion(aliasAs));
		getPeer().getTitleBinding().captureCurrent(new StoryCellForConcordion(title));
		if (type != null) {
			getPeer().getTitleBinding().captureCurrent(new StoryCellForConcordion(type));
		}
	}

}
