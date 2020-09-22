package demoapp.dom.types.isis.localresourcepaths.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.LocalResourcePath;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class IsisLocalResourcePathJdoEntities {

    final RepositoryService repositoryService;

    public Optional<IsisLocalResourcePathJdo> find(final LocalResourcePath readOnlyProperty) {
        return repositoryService.firstMatch(IsisLocalResourcePathJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisLocalResourcePathJdo> all() {
        return repositoryService.allInstances(IsisLocalResourcePathJdo.class);
    }


}
