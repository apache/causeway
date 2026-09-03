import { Component } from 'vue';
export type CausewayPageLoader = () => Promise<Component | {
    default: Component;
}>;
export type CausewayPageRegistration = Component | CausewayPageLoader;
export type CausewayPageRegistryInput = ReadonlyMap<string, CausewayPageRegistration> | ReadonlyArray<readonly [string, CausewayPageRegistration]> | Readonly<Record<string, CausewayPageRegistration>>;
export declare function normalizePageRegistry(input?: CausewayPageRegistryInput): ReadonlyMap<string, CausewayPageRegistration>;
export declare function isPageLoader(registration: CausewayPageRegistration): registration is CausewayPageLoader;
//# sourceMappingURL=registry.d.ts.map