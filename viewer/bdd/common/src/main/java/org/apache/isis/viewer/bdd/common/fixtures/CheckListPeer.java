package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryValueException;
import org.apache.isis.viewer.bdd.common.util.Strings;

public class CheckListPeer extends AbstractFixturePeer {

    public static enum CheckMode {
        EXACT {
			@Override
			public boolean isExact() {
				return true;
			}
		}, 
        NOT_EXACT {
			@Override
			public boolean isExact() {
				return false;
			}
		};
        public abstract boolean isExact();
    }

    private final String listAlias;

    private final CellBinding titleBinding;
    private final CellBinding typeBinding;
    private final CheckMode checkMode;

    /**
     * Objects found while processing table.
     */
    private final List<ObjectAdapter> foundAdapters = new ArrayList<ObjectAdapter>();


    public CheckListPeer(final AliasRegistry aliasesRegistry,
            String listAlias, final CheckMode checkMode,
            final CellBinding titleBinding, final CellBinding typeBinding) {
        super(aliasesRegistry, titleBinding, typeBinding);
        this.titleBinding = titleBinding;
        this.typeBinding = typeBinding;
        this.listAlias = listAlias;
        this.checkMode = checkMode;
    }

	public void assertIsList() throws StoryValueException {
		if (!isAliasedAdapter()) {
			throw new StoryValueException("no such alias");
		}
		if (!isList()) {
			throw new StoryValueException("not a list");
		}
	}

	public boolean isList() {
		return getCollectionFacet() != null;
	}

	private boolean isAliasedAdapter() {
		return getListAdapter() != null;
	}

    private ObjectAdapter getListAdapter() {
		return getAliasRegistry().getAliased(listAlias);
	}

	private CollectionFacet getCollectionFacet() {
        return getListAdapter() != null ? getListAdapter()
                .getSpecification().getFacet(CollectionFacet.class) : null;
	}

	public boolean isCheckModeExact() {
		return getCheckMode().isExact();
	}

	private CheckMode getCheckMode() {
		return checkMode;
	}
	
	public List<ObjectAdapter> getFoundAdapters() {
		return foundAdapters;
	}

	public List<ObjectAdapter> getNotFoundAdapters() {
		final List<ObjectAdapter> allAdapters = new ArrayList<ObjectAdapter>();
		
		for (final ObjectAdapter adapter : getCollectionFacet().iterable(getListAdapter())) {
			allAdapters.add(adapter);
		}
		
		allAdapters.removeAll(foundAdapters);
		return allAdapters;
	}
	
	public boolean findAndAddObject() {
		ObjectAdapter foundAdapter = findAdapter();
        if (foundAdapter == null) {
        	return false;
        } 
        foundAdapters.add(foundAdapter);
		return true;
	}

	private ObjectAdapter findAdapter() {
        for (final ObjectAdapter adapter : getCollectionFacet().iterable(getListAdapter())) {

            if (!titleMatches(adapter)) {
                continue; // keep looking
            }
            if (!typeMatches(adapter)) {
                continue; // keep looking
            }

            return adapter;
        }
		return null;
	}

    private boolean titleMatches(final ObjectAdapter adapter) {
        final String adapterTitle = adapter.titleString();
        final String requiredTitle = titleBinding.getCurrentCell().getText();
        return Strings.nullSafeEquals(adapterTitle, requiredTitle);
    }

    private boolean typeMatches(final ObjectAdapter adapter) {
        if (!typeBinding.isFound()) {
            return true;
        }

        final ObjectSpecification spec = adapter.getSpecification();
        final String requiredTypeName = typeBinding.getCurrentCell().getText();
        final String specFullName = spec.getFullName();
        if (specFullName.equals(requiredTypeName)) {
            return true;
        }

        final String simpleSpecName = Strings.simpleName(specFullName);
        final String simpleRequiredType = Strings
                .simpleName(requiredTypeName);
        return simpleSpecName.equalsIgnoreCase(simpleRequiredType);
    }

	public CellBinding getTitleBinding() {
		return titleBinding;
	}

}
