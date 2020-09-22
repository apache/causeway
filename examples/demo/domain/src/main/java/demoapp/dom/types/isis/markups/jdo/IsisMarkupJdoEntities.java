package demoapp.dom.types.isis.markups.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Markup;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class IsisMarkupJdoEntities {

    final RepositoryService repositoryService;

    public Optional<IsisMarkupJdo> find(final Markup readOnlyProperty) {
        return repositoryService.firstMatch(IsisMarkupJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisMarkupJdo> all() {
        return repositoryService.allInstances(IsisMarkupJdo.class);
    }


}
