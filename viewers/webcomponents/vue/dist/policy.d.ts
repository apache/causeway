import { CausewaySemanticResult, CausewayViewerRuntime } from './contracts';
export declare const NAVIGATION_REQUEST_EVENT = "causeway-navigation-request";
export declare const ACTION_REQUEST_EVENT = "causeway-action-request";
export declare const ACTION_RESULT_EVENT = "causeway-action-result";
export declare const OBJECT_CONTEXT_STATE_EVENT = "causeway-object-context-state-change";
export declare const MENU_BARS_STATE_EVENT = "causeway-menu-bars-state-change";
interface ActionResultDetail {
    readonly actionId?: string;
    readonly context?: object;
    readonly result?: CausewaySemanticResult;
    readonly resultPresentation?: Record<string, unknown>;
}
export declare function resolveResultOutlet(shell: HTMLElement): HTMLElement;
export declare function presentSemanticResult(outlet: HTMLElement, detail: ActionResultDetail): void;
export declare function installSemanticBridge(runtime: CausewayViewerRuntime, shell: HTMLElement): () => void;
export {};
//# sourceMappingURL=policy.d.ts.map