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

import type {
  CausewayActionRequest,
  CausewayEventClaim,
  CausewayLocalResourceTarget,
  CausewayObjectTarget,
  CausewayPolicyContext,
  CausewaySemanticResult,
  CausewayViewerRuntime
} from './contracts';
import {isFrameworkLogoutAction, removeFrameworkLogoutMenuActions} from './host-operation';
import {navigateLocalResource} from './local-resource';
import {canonicalRouterObjectPath} from './route-codec';

export const NAVIGATION_REQUEST_EVENT = 'causeway-navigation-request';
export const ACTION_REQUEST_EVENT = 'causeway-action-request';
export const ACTION_RESULT_EVENT = 'causeway-action-result';
export const OBJECT_CONTEXT_STATE_EVENT = 'causeway-object-context-state-change';
export const MENU_BARS_STATE_EVENT = 'causeway-menu-bars-state-change';

interface ActionResultDetail {
  readonly actionId?: string;
  readonly context?: object;
  readonly result?: CausewaySemanticResult;
  readonly resultPresentation?: Record<string, unknown>;
}

function createClaim(): CausewayEventClaim {
  let claimed = false;
  return {
    get claimed() { return claimed; },
    claim() {
      if (claimed) return false;
      claimed = true;
      return true;
    }
  };
}

function context(runtime: CausewayViewerRuntime): CausewayPolicyContext {
  return Object.freeze({
    router: runtime.router,
    basePath: runtime.basePath,
    shell: runtime.state.shell,
    routeGeneration: runtime.state.routeGeneration
  });
}

function objectIdentity(result: CausewaySemanticResult | undefined): CausewayObjectTarget | null {
  if (result?.kind !== 'object' || !result.value || typeof result.value !== 'object') return null;
  const metadata = (result.value as {_meta?: Record<string, unknown>})._meta;
  const logicalTypeName = metadata?.logicalTypeName;
  const id = metadata?.id;
  if (typeof logicalTypeName !== 'string' || typeof id !== 'string' || !logicalTypeName || !id) return null;
  return Object.freeze({logicalTypeName, id, title: String(metadata?.title ?? id)});
}

function eligiblePageOutlets(shell: HTMLElement): HTMLElement[] {
  const route = shell.querySelector<HTMLElement>('[data-causeway-router-view]');
  return [...(route?.querySelectorAll<HTMLElement>('cw-action-results[data-causeway-page-result]') ?? [])]
    .filter(outlet => outlet.isConnected);
}

function shellOutlet(shell: HTMLElement): HTMLElement | null {
  const outlets = shell.querySelectorAll<HTMLElement>('cw-action-results[data-causeway-shell-result]');
  return outlets.length === 1 ? outlets[0] : null;
}

export function resolveResultOutlet(shell: HTMLElement): HTMLElement {
  const page = eligiblePageOutlets(shell);
  if (page.length > 1) throw new Error('The active Vue route has duplicate action-result outlets.');
  if (page.length === 1) return page[0];
  const fallback = shellOutlet(shell);
  if (!fallback?.isConnected) throw new Error('The authored Vue shell result outlet is unavailable.');
  return fallback;
}

function replacePresentation(outlet: HTMLElement, ...nodes: Node[]): void {
  const enhanced = outlet as HTMLElement & {replacePresentation?: (...nodes: Node[]) => void};
  if (enhanced.replacePresentation) enhanced.replacePresentation(...nodes);
  else outlet.replaceChildren(...nodes);
  outlet.hidden = nodes.length === 0;
}

export function presentSemanticResult(outlet: HTMLElement, detail: ActionResultDetail): void {
  const result = detail.result;
  const heading = document.createElement('h2');
  heading.textContent = detail.actionId ? `${detail.actionId} result` : 'Action result';
  if (result?.kind === 'collection') {
    const collection = document.createElement('cw-standalone-collection') as HTMLElement & {result?: CausewaySemanticResult};
    collection.setAttribute('data-testid', 'causeway-standalone-action-result');
    collection.setAttribute('named', String(detail.resultPresentation?.named ?? heading.textContent));
    collection.result = result;
    replacePresentation(outlet, collection);
    return;
  }
  const output = document.createElement('output');
  output.textContent = result?.kind === 'scalar' ? String(result.value ?? '') :
    result?.kind === 'void' ? 'Completed' : 'This result cannot be presented.';
  replacePresentation(outlet, heading, output);
}

async function refreshAfterVoid(runtime: CausewayViewerRuntime, detail: ActionResultDetail): Promise<void> {
  const shell = runtime.state.shell;
  const routeRegion = shell?.querySelector<HTMLElement>('[data-causeway-router-view]');
  const contextElement = routeRegion?.querySelector<HTMLElement>('cw-object-context[data-causeway-route-context]') as
    (HTMLElement & {context?: {refresh?: () => void}}) | null;
  const objectContext = contextElement?.context;
  if (!contextElement || !objectContext?.refresh) return;
  const generation = runtime.state.routeGeneration;
  const onState = (rawEvent: Event) => {
    if (generation !== runtime.state.routeGeneration) return cleanup();
    const state = (rawEvent as CustomEvent).detail?.state;
    if (state?.status !== 'terminal-error') {
      if (state?.status === 'ready' || state?.status === 'partial-error') cleanup();
      return;
    }
    cleanup();
    const code = state.errors?.[0]?.extensions?.classification
      ?? state.errors?.[0]?.extensions?.code
      ?? state.error?.code;
    if (code === 'NOT_FOUND') void runtime.router.replace('/');
  };
  const cleanup = () => contextElement.removeEventListener(OBJECT_CONTEXT_STATE_EVENT, onState);
  contextElement.addEventListener(OBJECT_CONTEXT_STATE_EVENT, onState);
  objectContext.refresh();
  presentSemanticResult(resolveResultOutlet(shell!), detail);
}

async function applyNavigation(runtime: CausewayViewerRuntime, target: CausewayObjectTarget): Promise<void> {
  const claim = createClaim();
  const handled = await runtime.policies.navigate?.(target, claim, context(runtime));
  if (handled === true) claim.claim();
  if (!claim.claimed) await runtime.router.push(canonicalRouterObjectPath(target));
}

export function installSemanticBridge(runtime: CausewayViewerRuntime, shell: HTMLElement): () => void {
  const destinations = new WeakMap<object, HTMLElement>();
  const resumedActionEvents = new WeakSet<Event>();
  let unscopedDestination: HTMLElement | null = null;
  let active = true;
  const announceUnavailableLogout = () => {
    const announcement = shell.querySelector<HTMLElement>('[data-causeway-route-announcement]');
    if (announcement) announcement.textContent = 'Logout requires a host authentication integration.';
    shell.dataset.causewayLogoutUnavailable = 'true';
  };
  const resumeAction = (event: CustomEvent<CausewayActionRequest>, generation: number) => {
    if (!active || generation !== runtime.state.routeGeneration || !(event.target instanceof EventTarget)) return;
    const replay = new CustomEvent<CausewayActionRequest>(ACTION_REQUEST_EVENT, {
      bubbles: true,
      composed: true,
      cancelable: true,
      detail: event.detail
    });
    resumedActionEvents.add(replay);
    event.target.dispatchEvent(replay);
  };
  const onActionRequest = (rawEvent: Event) => {
    const event = rawEvent as CustomEvent<CausewayActionRequest>;
    const detail = event.detail;
    try {
      const outlet = resolveResultOutlet(shell);
      if (detail?.context && typeof detail.context === 'object') destinations.set(detail.context, outlet);
      else unscopedDestination = outlet;
    } catch (error) {
      runtime.policies.error?.(error, context(runtime));
    }
    if (resumedActionEvents.has(event)) return;
    const frameworkLogout = isFrameworkLogoutAction(detail);
    const actionPolicy = runtime.policies.action;
    if (!actionPolicy) {
      if (frameworkLogout) {
        event.preventDefault();
        announceUnavailableLogout();
      }
      return;
    }
    const claim = createClaim();
    const generation = runtime.state.routeGeneration;
    try {
      const handled = actionPolicy(detail, claim, context(runtime));
      if (handled && typeof (handled as Promise<boolean | void>).then === 'function') {
        event.preventDefault();
        void Promise.resolve(handled).then(value => {
          if (value === true) claim.claim();
          if (!claim.claimed && !frameworkLogout) resumeAction(event, generation);
          else if (!claim.claimed) announceUnavailableLogout();
        }).catch(error => runtime.policies.error?.(error, context(runtime)));
        return;
      }
      if (handled === true) claim.claim();
      if (claim.claimed || frameworkLogout) event.preventDefault();
      if (frameworkLogout && !claim.claimed) announceUnavailableLogout();
    } catch (error) {
      event.preventDefault();
      runtime.policies.error?.(error, context(runtime));
    }
  };
  const onNavigation = (rawEvent: Event) => {
    const event = rawEvent as CustomEvent;
    const target = event.detail?.target as CausewayObjectTarget | undefined;
    if (!target?.logicalTypeName || !(target.id ?? target.objectId)) return;
    event.preventDefault();
    void applyNavigation(runtime, target).catch(error => runtime.policies.error?.(error, context(runtime)));
  };
  const onResult = (rawEvent: Event) => {
    const event = rawEvent as CustomEvent<ActionResultDetail>;
    const detail = event.detail ?? {};
    void (async () => {
      const claim = createClaim();
      const handled = await runtime.policies.result?.(detail, claim, context(runtime));
      if (handled === true) claim.claim();
      if (claim.claimed) return;
      if (detail.result?.kind === 'local-resource') {
        navigateLocalResource(detail.result.value as CausewayLocalResourceTarget, {applicationBase: runtime.applicationResourceBase});
        (event.target as {dismissResult?: () => void} | null)?.dismissResult?.();
        return;
      }
      const identity = objectIdentity(detail.result);
      if (identity) return applyNavigation(runtime, identity);
      const destination = detail.context && destinations.get(detail.context) || unscopedDestination || resolveResultOutlet(shell);
      if (detail.result?.kind === 'void') await refreshAfterVoid(runtime, detail);
      else presentSemanticResult(destination, detail);
      (event.target as {dismissResult?: () => void} | null)?.dismissResult?.();
    })().catch(error => runtime.policies.error?.(error, context(runtime)));
  };
  const onMenuState = () => queueMicrotask(() => {
    if (active) removeFrameworkLogoutMenuActions(shell);
  });
  removeFrameworkLogoutMenuActions(shell);
  shell.addEventListener(ACTION_REQUEST_EVENT, onActionRequest, {capture: true});
  shell.addEventListener(NAVIGATION_REQUEST_EVENT, onNavigation);
  shell.addEventListener(ACTION_RESULT_EVENT, onResult);
  shell.addEventListener(MENU_BARS_STATE_EVENT, onMenuState);
  return () => {
    active = false;
    shell.removeEventListener(ACTION_REQUEST_EVENT, onActionRequest, {capture: true});
    shell.removeEventListener(NAVIGATION_REQUEST_EVENT, onNavigation);
    shell.removeEventListener(ACTION_RESULT_EVENT, onResult);
    shell.removeEventListener(MENU_BARS_STATE_EVENT, onMenuState);
  };
}
