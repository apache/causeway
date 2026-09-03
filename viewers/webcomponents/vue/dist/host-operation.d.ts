import { CausewayActionRequest } from './contracts';
export declare const FRAMEWORK_LOGOUT_ACTION: Readonly<{
    serviceLogicalTypeName: "causeway.security.LogoutMenu";
    actionId: "logout";
}>;
export declare function isFrameworkLogoutAction(value: Partial<CausewayActionRequest> | undefined): boolean;
export declare function removeFrameworkLogoutMenuActions(root: ParentNode): number;
//# sourceMappingURL=host-operation.d.ts.map