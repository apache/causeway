export declare const INVALID_ROUTE_MESSAGE = "The requested application route is invalid.";
export interface ObjectRouteIdentity {
    readonly logicalTypeName: string;
    readonly objectId: string;
}
export declare function encodeRouteSegment(value: unknown): string;
export declare function decodeRouteSegment(encoded: string): string;
export declare function normalizeBasePath(basePath: unknown): string;
export declare function canonicalObjectPath(basePath: unknown, target: {
    readonly logicalTypeName?: unknown;
    readonly id?: unknown;
    readonly objectId?: unknown;
}): string;
export declare function canonicalRouterObjectPath(target: {
    readonly logicalTypeName?: unknown;
    readonly id?: unknown;
    readonly objectId?: unknown;
}): string;
export declare function parseCanonicalObjectPath(pathname: unknown, basePath?: unknown): Readonly<ObjectRouteIdentity>;
export declare function canonicalRouteKey(identity: ObjectRouteIdentity): string;
//# sourceMappingURL=route-codec.d.ts.map