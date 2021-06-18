package org.apache.isis.extensions.secman.integration.spiimpl;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.tablecol.TableColumnVisibilityService;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.applib.user.menu.MeService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.ext.secman.TableColumnVisibilityServiceForSecman")
@javax.annotation.Priority(OrderPrecedence.LATE - 10)
@Qualifier("Secman")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TableColumnVisibilityServiceForSecman implements TableColumnVisibilityService {

    final MeService meService;
    final SpecificationLoader specificationLoader;

    @Override
    public boolean hides(Class<?> elementType, String memberId) {
        val me = meService.me();
        val permissionSet = me.getPermissionSet();

        final boolean granted = specificationLoader.specForType(elementType)
            .map(ObjectSpecification::getLogicalTypeName)
            .map(logicalTypeName->{
                val featureId = ApplicationFeatureId.newMember(logicalTypeName, memberId);
                return permissionSet.evaluate(featureId, ApplicationPermissionMode.VIEWING).isGranted();
            })
            .orElse(false); // do not grant if elementType has no logicalTypeName

        return !granted;

    }

}
