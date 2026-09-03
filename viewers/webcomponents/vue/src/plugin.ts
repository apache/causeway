/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import {inject, onBeforeUnmount, onMounted, type Plugin, type Ref} from 'vue';
import {CAUSEWAY_VIEWER_KEY, type CausewayViewerOptions, type CausewayViewerRuntime} from './contracts';
import {validateShellBoundary, type ShellLandmarks} from './boundary';
import {installSemanticBridge} from './policy';
import {normalizeBasePath} from './route-codec';
import {normalizePageRegistry} from './registry';

export function createCausewayVueViewer(options: CausewayViewerOptions): CausewayViewerRuntime {
  if (!options?.router) throw new Error('The Vue viewer requires the application router.');
  const endpoint = String(options.endpoint ?? '').trim();
  if (!endpoint) throw new Error('The Vue viewer GraphQL endpoint is required.');
  const state = {shell: null as HTMLElement | null, routeGeneration: 0};
  let runtime!: CausewayViewerRuntime;
  const plugin: Plugin = {
    install(app) {
      app.provide(CAUSEWAY_VIEWER_KEY, runtime);
    }
  };
  runtime = Object.freeze({
    plugin,
    router: options.router,
    endpoint,
    basePath: normalizeBasePath(options.basePath ?? (options.router.options.history.base || '/')),
    pages: normalizePageRegistry(options.pages),
    policies: Object.freeze({...options.policies}),
    developmentDiagnostics: options.developmentDiagnostics ?? true,
    state
  });
  return runtime;
}

export function useCausewayViewer(): CausewayViewerRuntime {
  const runtime = inject(CAUSEWAY_VIEWER_KEY);
  if (!runtime) throw new Error('The Causeway Vue viewer plugin is not installed.');
  return runtime;
}

export function useCausewayShell(shellReference: Ref<HTMLElement | null>): Ref<HTMLElement | null> {
  const runtime = useCausewayViewer();
  let cleanup: (() => void) | null = null;
  onMounted(() => {
    const shell = shellReference.value;
    if (!shell) throw new Error('The authored Vue application shell is missing.');
    validateShellBoundary(shell);
    runtime.state.shell = shell;
    cleanup = installSemanticBridge(runtime, shell);
  });
  onBeforeUnmount(() => {
    cleanup?.();
    cleanup = null;
    if (runtime.state.shell === shellReference.value) runtime.state.shell = null;
  });
  return shellReference;
}

export function bindCausewayShell(runtime: CausewayViewerRuntime, shell: HTMLElement): {landmarks: ShellLandmarks; dispose: () => void} {
  const landmarks = validateShellBoundary(shell);
  runtime.state.shell = shell;
  const cleanup = installSemanticBridge(runtime, shell);
  return {
    landmarks,
    dispose() {
      cleanup();
      if (runtime.state.shell === shell) runtime.state.shell = null;
    }
  };
}
