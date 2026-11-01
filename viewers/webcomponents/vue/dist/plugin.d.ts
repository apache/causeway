import { Ref } from 'vue';
import { CausewayViewerOptions, CausewayViewerRuntime } from './contracts';
import { ShellLandmarks } from './boundary';
export declare function createCausewayVueViewer(options: CausewayViewerOptions): CausewayViewerRuntime;
export declare function useCausewayViewer(): CausewayViewerRuntime;
export declare function useCausewayShell(shellReference: Ref<HTMLElement | null>): Ref<HTMLElement | null>;
export declare function bindCausewayShell(runtime: CausewayViewerRuntime, shell: HTMLElement): {
    landmarks: ShellLandmarks;
    dispose: () => void;
};
//# sourceMappingURL=plugin.d.ts.map