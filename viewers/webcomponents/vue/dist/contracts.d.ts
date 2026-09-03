import { InjectionKey, Plugin } from 'vue';
import { Router } from 'vue-router';
import { CausewayPageRegistration, CausewayPageRegistryInput } from './registry';
export interface CausewayObjectTarget {
    readonly logicalTypeName?: string;
    readonly id?: string;
    readonly objectId?: string;
    readonly title?: string;
}
export interface CausewaySemanticResult {
    readonly kind: 'object' | 'scalar' | 'collection' | 'void' | string;
    readonly value?: unknown;
}
export interface CausewayEventClaim {
    readonly claimed: boolean;
    claim(): boolean;
}
export interface CausewayPolicyContext {
    readonly router: Router;
    readonly basePath: string;
    readonly shell: HTMLElement | null;
    readonly routeGeneration: number;
}
export interface CausewayViewerPolicies {
    readonly navigate?: (target: CausewayObjectTarget, claim: CausewayEventClaim, context: CausewayPolicyContext) => boolean | void | Promise<boolean | void>;
    readonly home?: (entry: unknown, claim: CausewayEventClaim, context: CausewayPolicyContext) => boolean | void | Promise<boolean | void>;
    readonly result?: (detail: unknown, claim: CausewayEventClaim, context: CausewayPolicyContext) => boolean | void | Promise<boolean | void>;
    readonly error?: (error: unknown, context: CausewayPolicyContext) => void;
}
export interface CausewayViewerOptions {
    readonly router: Router;
    readonly endpoint: string;
    readonly basePath?: string;
    readonly pages?: CausewayPageRegistryInput;
    readonly policies?: CausewayViewerPolicies;
    readonly developmentDiagnostics?: boolean;
}
export interface CausewayViewerRuntime {
    readonly plugin: Plugin;
    readonly router: Router;
    readonly endpoint: string;
    readonly basePath: string;
    readonly pages: ReadonlyMap<string, CausewayPageRegistration>;
    readonly policies: CausewayViewerPolicies;
    readonly developmentDiagnostics: boolean;
    readonly state: {
        shell: HTMLElement | null;
        routeGeneration: number;
    };
}
export declare const CAUSEWAY_VIEWER_KEY: InjectionKey<CausewayViewerRuntime>;
export interface CausewayRoutePageProps {
    readonly logicalTypeName: string;
    readonly objectId: string;
    readonly routeKey: string;
}
//# sourceMappingURL=contracts.d.ts.map