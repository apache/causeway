import { ObjectRouteIdentity } from './route-codec';
export declare const ROUTE_CONTEXT_SELECTOR = "cw-object-context[data-causeway-route-context]";
export declare const ROUTE_INTERACTIONS_SELECTOR = "cw-interaction-controller[data-causeway-route-interactions]";
export interface BoundaryValidation {
    readonly valid: boolean;
    readonly classification?: 'missing-context' | 'duplicate-context' | 'identity' | 'interactions';
    readonly context?: HTMLElement;
}
export declare function validateRouteBoundary(root: ParentNode, identity: ObjectRouteIdentity): BoundaryValidation;
export interface ShellLandmarks {
    readonly shell: HTMLElement;
    readonly client: HTMLElement;
    readonly route: HTMLElement;
    readonly loading: HTMLElement;
    readonly announcement: HTMLElement;
    readonly result: HTMLElement;
}
export declare function validateShellBoundary(shell: HTMLElement): ShellLandmarks;
//# sourceMappingURL=boundary.d.ts.map