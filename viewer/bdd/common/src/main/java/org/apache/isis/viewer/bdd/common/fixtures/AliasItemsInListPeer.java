package org.apache.isis.viewer.bdd.common.fixtures;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.StoryValueException;
import org.apache.isis.viewer.bdd.common.util.Strings;

public class AliasItemsInListPeer extends AbstractFixturePeer {

	private final String listAlias;
    private final CellBinding titleBinding;
    private final CellBinding typeBinding;
    private final CellBinding aliasBinding;
    

    public AliasItemsInListPeer(final AliasRegistry aliasesRegistry,
            final String listAlias, 
            final CellBinding titleBinding,
            final CellBinding typeBinding, final CellBinding aliasBinding) {
    	super(aliasesRegistry, titleBinding, typeBinding, aliasBinding);

    	this.titleBinding = titleBinding;
        this.typeBinding = typeBinding;
        this.aliasBinding = aliasBinding;
        this.listAlias = listAlias;
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

	public CellBinding getTitleBinding() {
		return titleBinding;
	}
	
	public CellBinding getTypeBinding() {
		return typeBinding;
	}
	
	public CellBinding getAliasBinding() {
		return aliasBinding;
	}

	public StoryCell findAndAlias() throws StoryBoundValueException {
    	ObjectAdapter foundAdapter = findAdapter();
        if (foundAdapter == null) {
        	throw StoryBoundValueException.current(titleBinding, "not found");
        }
        
        StoryCell currentCell = aliasBinding.getCurrentCell();
		String currentCellText = currentCell.getText();
		getAliasRegistry().aliasAs(currentCellText, foundAdapter);
		return currentCell;
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


}
