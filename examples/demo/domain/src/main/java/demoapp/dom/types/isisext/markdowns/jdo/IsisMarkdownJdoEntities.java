package demoapp.dom.types.isisext.markdowns.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;

@Service
public class IsisMarkdownJdoEntities {

    public Optional<IsisMarkdownJdo> find(final Markdown readOnlyProperty) {
        return repositoryService.firstMatch(IsisMarkdownJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisMarkdownJdo> all() {
        return repositoryService.allInstances(IsisMarkdownJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
