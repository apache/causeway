package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryValueException;

import com.google.common.collect.Iterables;

public class AbstractListFixturePeer extends AbstractFixturePeer {

	private final String listAlias;

   /**
     * @see #collectionAdapters()
     */
    private List<ObjectAdapter> objects;


    public AbstractListFixturePeer(final AliasRegistry aliasesRegistry,
            final String listAlias, final CellBinding... cellBindings) {
    	super(aliasesRegistry, cellBindings);

        this.listAlias = listAlias;
    }

    protected boolean isValidListAlias() {
        return getListAdapter() != null && isList();
    }

    protected ObjectAdapter getListAdapter() {
        return getAliasRegistry().getAliased(listAlias);
    }


	public void assertIsList() throws StoryValueException {
		if (!(getListAdapter() != null)) {
			throw new StoryValueException("no such alias");
		}
		if (!isList()) {
			throw new StoryValueException("not a list");
		}
	}

	public boolean isList() {
		return getCollectionFacet() != null;
	}

    /**
     * Lazily populated, and populated only once.
     */
    protected List<ObjectAdapter> collectionAdapters() {
        if(objects==null){
            objects = new ArrayList<ObjectAdapter>();
            Iterables.addAll(objects, collectionContents());
        }
        return objects;
    }

    private Iterable<ObjectAdapter> collectionContents() {
        return getCollectionFacet().iterable(getListAdapter());
    }

	private CollectionFacet getCollectionFacet() {
        return getListAdapter() != null ? getListAdapter()
                .getSpecification().getFacet(CollectionFacet.class) : null;
	}


}
