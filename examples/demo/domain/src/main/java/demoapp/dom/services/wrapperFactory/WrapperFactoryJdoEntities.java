package demoapp.dom.services.wrapperFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class WrapperFactoryJdoEntities {

    final RepositoryService repositoryService;

    public Optional<WrapperFactoryJdo> find(final String value) {
        return repositoryService.firstMatch(WrapperFactoryJdo.class, x -> Objects.equals(x.getPropertyAsync(), value));
    }

    public List<WrapperFactoryJdo> all() {
        return repositoryService.allInstances(WrapperFactoryJdo.class);
    }

    public WrapperFactoryJdo first() {
        return all().stream().findFirst().get();
    }

}
