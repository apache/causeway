package org.apache.isis.extensions.security.manager.shiro;

import org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionMode;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.shiro.authz.Permission;

/**
 * As created by {@link org.apache.isis.extensions.security.manager.shiro.PermissionResolverForIsisShiroAuthorizor}, interprets the
 * permission strings formatted by <code>IsisShiroAuthorizor</code>.
 */
class PermissionForMember implements org.apache.shiro.authz.Permission {

    private final ApplicationFeatureId featureId;
    private final ApplicationPermissionMode mode;

    /**
     * Expects in format <code>package:className:methodName:r|w</code>
     */
    public PermissionForMember(String permissionString) {
        final String[] split = permissionString.split("\\:");
        if(split.length == 4) {
            String packageName = split[0];
            String className = split[1];
            String memberName = split[2];
            this.featureId = ApplicationFeatureId.newMember(packageName + "." + className, memberName);

            ApplicationPermissionMode mode = modeFrom(split[3]);
            if(mode != null) {
                this.mode = mode;
                return;
            }
        }
        throw new IllegalArgumentException("Invalid format for permission: " + permissionString + "; expected 'packageName:className:methodName:r|w");
    }

    private static ApplicationPermissionMode modeFrom(String s) {
        if("r".equals(s)) {
            return ApplicationPermissionMode.VIEWING;
        }
        if("w".equals(s)) {
            return ApplicationPermissionMode.CHANGING;
        }
        return null;
    }

    /**
     */
    @Override
    public boolean implies(Permission p) {
        return false;
    }

    ApplicationFeatureId getFeatureId() {
        return featureId;
    }

    ApplicationPermissionMode getMode() {
        return mode;
    }
}
