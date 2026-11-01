import { CausewayLocalResourceTarget } from './contracts';
export declare class LocalResourceNavigationError extends Error {
    readonly code: string;
    constructor(code: string, message: string);
}
interface LocationTarget {
    readonly href: string;
    assign(url: string): void;
}
interface NavigateOptions {
    readonly location?: LocationTarget;
    readonly applicationBase?: string;
    readonly open?: (url?: string | URL, target?: string, features?: string) => Window | null;
}
export declare function resolveLocalResourceTarget(value: CausewayLocalResourceTarget | undefined, options?: Pick<NavigateOptions, 'location' | 'applicationBase'>): Readonly<{
    url: URL;
    openUrlStrategy: 'SAME_WINDOW' | 'NEW_WINDOW';
}>;
export declare function navigateLocalResource(value: CausewayLocalResourceTarget | undefined, options?: NavigateOptions): void;
export {};
//# sourceMappingURL=local-resource.d.ts.map