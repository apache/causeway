import { Component } from 'vue';
import { RouteRecordRaw } from 'vue-router';
export declare const CAUSEWAY_ROUTE_NAMES: Readonly<{
    home: "causeway-home";
    object: "causeway-object";
    invalid: "causeway-invalid-route";
    notFound: "causeway-not-found";
}>;
export declare const CausewayInvalidRoutePage: Component;
export declare const CausewayNotFoundPage: Component;
export interface CausewayRouteRecordOptions {
    readonly homeComponent?: Component;
    readonly notFoundComponent?: Component;
}
export declare function createCausewayRouteRecords(options?: CausewayRouteRecordOptions): RouteRecordRaw[];
//# sourceMappingURL=routes.d.ts.map