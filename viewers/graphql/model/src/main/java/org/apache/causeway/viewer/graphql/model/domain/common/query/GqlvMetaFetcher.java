package org.apache.causeway.viewer.graphql.model.domain.common.query;

import lombok.val;

import java.util.Optional;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutFacet;
import org.apache.causeway.core.metamodel.object.Bookmarkable;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

/**
 * Metadata for every domain object.
 */
public class GqlvMetaFetcher {

    private final Bookmark bookmark;
    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;
    private final CausewayConfiguration causewayConfiguration;
    private final String graphqlPath;

    public GqlvMetaFetcher(
            final Bookmark bookmark,
            final BookmarkService bookmarkService,
            final ObjectManager objectManager,
            final CausewayConfiguration causewayConfiguration
    ) {
        this.bookmark = bookmark;
        this.bookmarkService = bookmarkService;
        this.objectManager = objectManager;
        this.causewayConfiguration = causewayConfiguration;
        this.graphqlPath = causewayConfiguration.valueOf("spring.graphql.path").orElse("/graphql");
    }

    public String logicalTypeName() {
        return bookmark.getLogicalTypeName();
    }

    public String id() {
        return bookmark.getIdentifier();
    }

    public String version() {
        return managedObject()
                .map(managedObject -> {
                    val domainPojo = managedObject.getPojo();
                    val entityFacet = managedObject.getSpecification().getFacet(EntityFacet.class);
                    if (entityFacet != null) {
                        val object = entityFacet.versionOf(domainPojo);
                        return object != null ? object.toString() : null;
                    } else {
                        return null;
                    }
                }).orElse(null);
    }

    public Bookmark bookmark() {
        return bookmark;
    }

    public String title() {
        return managedObject()
                .map(ManagedObject::getTitle)
                .orElse(null);
    }

    public String cssClass() {
        return managedObject()
                .map(managedObject -> {
                    val facet = managedObject.getSpecification().getFacet(CssClassFacet.class);
                    return facet != null ? facet.cssClass(managedObject) : null;
                })
                .orElse(null);
    }

    public String layout() {
        return managedObject()
                .map(managedObject -> {
                    val facet = managedObject.getSpecification().getFacet(LayoutFacet.class);
                    return facet != null ? facet.layout(managedObject) : null;
                })
                .orElse(null);
    }

    public String grid() {
        return resource("grid");
    }

    public String icon() {
        return resource("icon");
    }

    private String resource(String resource) {
        return managedObject()
                .flatMap(Bookmarkable::getBookmark
                ).map(bookmark -> String.format(
                        "//%s/object/%s:%s/%s/%s",
                        graphqlPath, bookmark.getLogicalTypeName(), bookmark.getIdentifier(), causewayConfiguration.getViewer().getGraphql().getMetaData().getFieldName(), resource))
                .orElse(null);
    }

    private Optional<ManagedObject> managedObject() {
        return bookmarkService.lookup(bookmark)
                .map(objectManager::adapt);
    }
}
