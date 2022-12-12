package org.apache.causeway.applib.services.user;

import java.util.Arrays;
import java.util.List;

import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.core.internaltestsupport.contract.ValueTypeContractTestAbstract;

class RoleMemento_value_Test extends ValueTypeContractTestAbstract<RoleMemento> {

    @Override
    protected List<RoleMemento> getObjectsWithSameValue() {
        return Arrays.asList(SudoService.ACCESS_ALL_ROLE,
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.getName(), SudoService.ACCESS_ALL_ROLE.getDescription()),
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.getName(), SudoService.ACCESS_ALL_ROLE.getDescription()),
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.getName(), ""),
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.getName())
        );
    }

    @Override
    protected List<RoleMemento> getObjectsWithDifferentValue() {
        return Arrays.asList(
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.getName() + "x"),
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.getName() + "y")
        );
    }
}
