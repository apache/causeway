package org.apache.isis.extensions.secman.model.spiimpl;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.tablecol.TableColumnVisibilityService;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.user.menu.MeService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.ext.secman.TableColumnVisibilityServiceForSecman")
@Order(OrderPrecedence.LATE - 10)
@Qualifier("Secman")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TableColumnVisibilityServiceForSecman implements TableColumnVisibilityService {

    final MeService meService;
    final MetaModelService metaModelService;

    @Override
    public boolean hides(Class<?> elementType, String memberId) {
        val me = meService.me();
        val permissionSet = me.getPermissionSet();

        final boolean granted = metaModelService.lookupLogicalTypeByClass(elementType)
            .map(LogicalType::getLogicalTypeName)
            .map(objectType->{
                val featureId = ApplicationFeatureId.newMember(objectType, memberId);
                return permissionSet.evaluate(featureId, ApplicationPermissionMode.VIEWING).isGranted();
            })
            .orElse(false); // do not grant if elementType has no objectType

        return !granted;

    }

}
