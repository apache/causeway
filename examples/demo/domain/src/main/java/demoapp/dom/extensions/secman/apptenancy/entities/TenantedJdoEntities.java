package demoapp.dom.extensions.secman.apptenancy.entities;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class TenantedJdoEntities {

    final RepositoryService repositoryService;

    public List<TenantedJdo> all() {
        return repositoryService.allInstances(TenantedJdo.class);
    }


}
