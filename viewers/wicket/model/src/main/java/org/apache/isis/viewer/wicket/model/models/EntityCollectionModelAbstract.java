package org.apache.isis.viewer.wicket.model.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class EntityCollectionModelAbstract
extends ModelAbstract<List<ManagedObject>>
implements EntityCollectionModel {

    private static final long serialVersionUID = 1L;

    @Getter(onMethod_ = {@Override}) private final @NonNull Identifier identifier;
    @Getter private final int pageSize;

    protected EntityCollectionModelAbstract(
            final @NonNull IsisAppCommonContext commonContext,
            final @NonNull Identifier identifier,
            final @NonNull ObjectSpecification typeOfSpecification,
            final @NonNull Can<FacetHolder> facetHolders) {
        super(commonContext);
        this.identifier = identifier;
        this.typeOfSpecification = Optional.of(typeOfSpecification); // as an optimization: memoize transient
        this.elementType = typeOfSpecification.getCorrespondingClass();
        this.pageSize = facetHolders.stream()
        .map(facetHolder->facetHolder.getFacet(PagedFacet.class))
        .filter(_NullSafe::isPresent)
        .findFirst()
        .map(PagedFacet::value)
        .orElse(getVariant().getPageSizeDefault());

        this.toggledMementos = _Maps.<String, ObjectMemento>newLinkedHashMap();
    }

    // -- TYPE OF (ELEMENT TYPE)

    @Getter(value = AccessLevel.PROTECTED) private final @NonNull Class<?> elementType;

    private transient Optional<ObjectSpecification> typeOfSpecification;

    @Override
    public ObjectSpecification getTypeOfSpecification() {
        if(typeOfSpecification==null) {
            typeOfSpecification = getSpecificationLoader().specForType(elementType);
        }
        return typeOfSpecification.orElse(null);
    }

    // -- LINKS PROVIDER

    /**
     * Additional links to render (if any)
     */
    private List<LinkAndLabel> linkAndLabels = _Lists.newArrayList();

    public void setLinkAndLabels(final @NonNull Iterable<LinkAndLabel> linkAndLabels) {
        this.linkAndLabels.clear();
        linkAndLabels.forEach(this.linkAndLabels::add);
    }

    @Override
    public final Can<LinkAndLabel> getLinks() {
        return Can.ofCollection(linkAndLabels);
    }

    // -- TOGGLE SUPPORT

    @Getter private LinkedHashMap<String, ObjectMemento> toggledMementos;

    @Override
    public Can<ObjectMemento> getToggleMementosList() {
        return Can.ofCollection(this.toggledMementos.values());
    }

    @Override
    public void clearToggleMementosList() {
        this.toggledMementos.clear();
    }

    @Override
    public boolean toggleSelectionOn(final ManagedObject selectedAdapter) {
        final ObjectMemento selectedAsMemento = super.getMementoService().mementoForObject(selectedAdapter);
        final String selectedKey = selectedAsMemento.asString();
        final boolean isSelected = _Maps.toggleElement(toggledMementos, selectedKey, selectedAsMemento);
        return isSelected;
    }

}
