package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;

public class CheckListPeer extends AbstractListFixturePeer {

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

    private final CheckMode checkMode;

    private final CellBinding titleBinding;
    /**
     * Can be set to null, indicating that no type checking is performed.
     */
    private final CellBinding typeBinding;

    /**
     * Objects found while processing table.
     */
    private final List<ObjectAdapter> foundAdapters = new ArrayList<ObjectAdapter>();


    public CheckListPeer(final AliasRegistry aliasesRegistry,
            String listAlias, final CheckMode checkMode,
            final CellBinding titleBinding) {
        this(aliasesRegistry, listAlias, checkMode, titleBinding, null);
    }

    public CheckListPeer(final AliasRegistry aliasesRegistry,
        String listAlias, final CheckMode checkMode,
        final CellBinding titleBinding, final CellBinding typeBinding) {
    super(aliasesRegistry, listAlias, titleBinding, typeBinding);
    this.checkMode = checkMode;
    this.titleBinding = titleBinding;
    this.typeBinding = typeBinding;
}

	public boolean isCheckModeExact() {
		return getCheckMode().isExact();
	}

    public CellBinding getTitleBinding() {
        return titleBinding;
    }

    /**
     * May be <tt>null</tt> (indicating that no type checking to be performed.
     */
    public CellBinding getTypeBinding() {
        return typeBinding;
    }

	private CheckMode getCheckMode() {
		return checkMode;
	}
	
	public List<ObjectAdapter> getFoundAdapters() {
		return foundAdapters;
	}

	public List<ObjectAdapter> getNotFoundAdapters() {
		final List<ObjectAdapter> allAdapters = 
		    new ArrayList<ObjectAdapter>(collectionAdapters());
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
        for (final ObjectAdapter adapter : collectionAdapters()) {

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
        return StringUtils.nullSafeEquals(adapterTitle, requiredTitle);
    }

    private boolean typeMatches(final ObjectAdapter adapter) {
        if (typeBinding == null || !typeBinding.isFound()) {
            return true;
        }

        final ObjectSpecification spec = adapter.getSpecification();
        final String requiredTypeName = typeBinding.getCurrentCell().getText();
        final String specFullName = spec.getFullName();
        if (specFullName.equals(requiredTypeName)) {
            return true;
        }

        final String simpleSpecName = StringUtils.simpleName(specFullName);
        final String simpleRequiredType = StringUtils
                .simpleName(requiredTypeName);
        return simpleSpecName.equalsIgnoreCase(simpleRequiredType);
    }


}