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

import {CausewayContextConsumerElement} from './context-consumer-element.mjs';
import {
  COMPONENT_STATE_EVENT,
  createSemanticEvent,
  OBJECT_LAYOUT_DIAGNOSTIC_EVENT,
  OBJECT_LAYOUT_STATE_EVENT
} from './context-events.mjs';
import {
  CausewayGridError,
  createFallbackLayoutPlan,
  parseCausewayGridXml,
  renderObjectLayoutPlan
} from './object-layout.mjs';
import {escapeHtml} from './rendering.mjs';

let objectSequence = 0;

export class CausewayObjectElement extends CausewayContextConsumerElement {
  static get observedAttributes() {
    return ['editable', 'layout-mode'];
  }

  constructor() {
    super();
    this.layoutId = `causeway-object-${++objectSequence}`;
    this.layoutRevision = 0;
    this.layoutAbortController = null;
    this.currentPlan = null;
    this.currentGridPath = null;
    this.forceLayoutRefresh = false;
    this.lastPublishedState = null;
    this.addEventListener('click', event => this.#handleTabClick(event));
    this.addEventListener('keydown', event => this.#handleTabKeydown(event));
    this.addEventListener(COMPONENT_STATE_EVENT, event => {
      if (event.detail?.element !== this) {
        queueMicrotask(() => this.#updateEmptyRegions());
      }
    });
  }

  get editable() {
    return this.hasAttribute('editable');
  }

  set editable(value) {
    if (value) {
      this.setAttribute('editable', '');
    } else {
      this.removeAttribute('editable');
    }
  }

  get layoutMode() {
    return this.getAttribute('layout-mode') || 'auto';
  }

  set layoutMode(value) {
    this.setAttribute('layout-mode', value);
  }

  connectedCallback() {
    super.connectedCallback();
  }

  disconnectedCallback() {
    this.layoutAbortController?.abort();
    this.layoutAbortController = null;
    super.disconnectedCallback();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue === newValue || !this.isConnected) {
      return;
    }
    if (name === 'layout-mode') {
      this.refreshLayout();
    } else if (name === 'editable' && this.currentPlan) {
      this.#renderPlan(this.currentPlan, this.getAttribute('data-layout-state') || this.currentPlan.source);
    }
  }

  createRequirement() {
    return {kind: 'layout'};
  }

  refreshLayout() {
    this.forceLayoutRefresh = true;
    this.currentGridPath = null;
    this.layoutAbortController?.abort();
    if (this.componentState) {
      void this.#prepareLayout(this.componentState);
    }
  }

  renderComponentState(state) {
    if (!state) {
      return;
    }
    if (['idle', 'schema-loading', 'object-loading'].includes(state.status)) {
      if (!this.currentPlan) {
        this.#renderStatus('loading', 'Loading object layout…');
      }
      return;
    }
    if (state.status === 'terminal-error') {
      this.#renderError(this._resolvedContext
        ? state.errors?.[0]?.message || 'No usable Causeway object context is available.'
        : 'No Causeway object context is available. Place <cw-object> beneath <cw-object-context>.');
      return;
    }
    void this.#prepareLayout(state);
  }

  async #prepareLayout(state) {
    const context = this._resolvedContext;
    if (!context) {
      this.#renderError('No Causeway object context is available. Place <cw-object> beneath <cw-object-context>.');
      return;
    }
    if (!['auto', 'fallback'].includes(this.layoutMode)) {
      this.#renderError(`Unsupported layout-mode '${this.layoutMode}'. Use 'auto' or 'fallback'.`);
      return;
    }
    let description;
    try {
      description = await context.describeObject();
    } catch {
      this.#renderError('The object schema description is unavailable.');
      return;
    }
    const members = description.members;
    if (this.layoutMode === 'fallback') {
      if (!this.currentPlan || this.currentPlan.source !== 'fallback' || this.forceLayoutRefresh) {
        this.forceLayoutRefresh = false;
        this.currentGridPath = null;
        this.#applyPlan(createFallbackLayoutPlan(members), 'fallback', null);
      }
      return;
    }
    if (state.status === 'unsupported' || state.status === 'partial-error') {
      const diagnostic = layoutDiagnostic(
        'GRID_METADATA_UNAVAILABLE',
        'Effective grid metadata is unavailable; the canonical fallback layout was used.'
      );
      this.#applyPlan(createFallbackLayoutPlan(members, [diagnostic]), 'fallback', null);
      return;
    }
    const gridPath = state.data?.grid;
    if (!gridPath) {
      const diagnostic = layoutDiagnostic(
        'GRID_REFERENCE_ABSENT',
        'No effective grid resource is available; the canonical fallback layout was used.'
      );
      this.#applyPlan(createFallbackLayoutPlan(members, [diagnostic]), 'fallback', null);
      return;
    }
    if (this.currentPlan && this.currentGridPath === gridPath && !this.forceLayoutRefresh) {
      return;
    }
    this.forceLayoutRefresh = false;
    const revision = ++this.layoutRevision;
    this.layoutAbortController?.abort();
    const abortController = new AbortController();
    this.layoutAbortController = abortController;
    if (!this.currentPlan) {
      this.#renderStatus('layout-loading', 'Loading effective object layout…');
    } else {
      this.#setLayoutState('layout-loading');
      this.#publishState('layout-loading', this.currentPlan.source, this.currentPlan.diagnostics, gridPath);
    }
    try {
      const resource = await context.loadStructuralResource(gridPath, {
        accept: 'application/xml, text/xml',
        signal: abortController.signal
      });
      if (revision !== this.layoutRevision || !this.isConnected) {
        return;
      }
      const parsed = parseCausewayGridXml(resource.text, {members});
      if (parsed.usable) {
        this.currentGridPath = gridPath;
        this.#applyPlan({...parsed.plan, diagnostics: parsed.diagnostics}, 'ready', gridPath);
      } else {
        const diagnostic = layoutDiagnostic(
          'GRID_PLAN_UNUSABLE',
          'The effective grid contains no usable semantic placement; the canonical fallback layout was used.'
        );
        this.currentGridPath = gridPath;
        this.#applyPlan(
          createFallbackLayoutPlan(members, [...parsed.diagnostics, diagnostic]),
          'fallback',
          gridPath
        );
      }
    } catch (error) {
      if (error?.name === 'AbortError' || revision !== this.layoutRevision || !this.isConnected) {
        return;
      }
      const code = error instanceof CausewayGridError
        ? error.code
        : error?.code || 'GRID_RESOURCE_UNAVAILABLE';
      const diagnostic = layoutDiagnostic(
        code,
        'The effective grid could not be used; the canonical fallback layout was used.'
      );
      this.currentGridPath = gridPath;
      this.#applyPlan(createFallbackLayoutPlan(members, [diagnostic]), 'fallback', gridPath);
    } finally {
      if (this.layoutAbortController === abortController) {
        this.layoutAbortController = null;
      }
    }
  }

  #applyPlan(plan, layoutState, gridPath) {
    this.currentPlan = plan;
    this.currentGridPath = gridPath;
    this.#renderPlan(plan, layoutState);
    this.#publishState(layoutState, plan.source, plan.diagnostics, gridPath);
    for (const diagnostic of plan.diagnostics ?? []) {
      this.dispatchEvent(createSemanticEvent(
        OBJECT_LAYOUT_DIAGNOSTIC_EVENT,
        Object.freeze({element: this, diagnostic}),
        {bubbles: true, composed: true}
      ));
    }
  }

  #renderPlan(plan, layoutState) {
    this.#setLayoutState(layoutState);
    const status = layoutState === 'fallback'
      ? 'Canonical fallback object layout is active.'
      : 'Effective object layout is ready.';
    this.innerHTML = `<div class="causeway-object" data-causeway-region="object" data-layout-source="${escapeHtml(plan.source)}">
  <p class="causeway-object-layout-status causeway-visually-hidden" role="status">${escapeHtml(status)}</p>
  ${renderObjectLayoutPlan(plan, {idPrefix: this.layoutId, editable: this.editable})}
</div>`;
    queueMicrotask(() => this.#updateEmptyRegions());
  }

  #renderStatus(state, message) {
    this.#setLayoutState(state);
    this.innerHTML = `<section class="causeway-object causeway-object-layout-status" aria-busy="true"><span role="status">${escapeHtml(message)}</span></section>`;
    this.#publishState(state, null, [], null);
  }

  #renderError(message) {
    this.currentPlan = null;
    this.currentGridPath = null;
    this.#setLayoutState('error');
    this.innerHTML = `<section class="causeway-object causeway-error" role="alert">${escapeHtml(message)}</section>`;
    this.#publishState('error', null, [], null);
  }

  #setLayoutState(state) {
    this.setAttribute('data-layout-state', state);
  }

  #publishState(status, source, diagnostics = [], resource = null) {
    const signature = JSON.stringify({status, source, resource, diagnostics});
    if (signature === this.lastPublishedState) {
      return;
    }
    this.lastPublishedState = signature;
    this.dispatchEvent(createSemanticEvent(
      OBJECT_LAYOUT_STATE_EVENT,
      Object.freeze({
        element: this,
        status,
        source,
        resource,
        diagnostics: Object.freeze([...(diagnostics ?? [])])
      }),
      {bubbles: true, composed: true}
    ));
  }

  #handleTabClick(event) {
    const tab = event.target?.closest?.('[role="tab"][data-causeway-tab]');
    if (tab) {
      this.#activateTab(tab, {focus: false});
    }
  }

  #handleTabKeydown(event) {
    const tab = event.target?.closest?.('[role="tab"][data-causeway-tab]');
    if (!tab) {
      return;
    }
    const tabList = tab.parentElement;
    const tabs = [...(tabList?.querySelectorAll?.('[role="tab"][data-causeway-tab]') ?? [])]
      .filter(candidate => !candidate.hidden);
    const current = tabs.indexOf(tab);
    if (current < 0 || tabs.length === 0) {
      return;
    }
    let next = current;
    if (['ArrowRight', 'ArrowDown'].includes(event.key)) {
      next = (current + 1) % tabs.length;
    } else if (['ArrowLeft', 'ArrowUp'].includes(event.key)) {
      next = (current - 1 + tabs.length) % tabs.length;
    } else if (event.key === 'Home') {
      next = 0;
    } else if (event.key === 'End') {
      next = tabs.length - 1;
    } else if (!['Enter', ' '].includes(event.key)) {
      return;
    }
    event.preventDefault();
    this.#activateTab(tabs[next], {focus: true});
  }

  #activateTab(tab, {focus = false} = {}) {
    const group = tab.closest?.('[data-causeway-tab-group]');
    const tabList = group?.querySelector?.(':scope > [role="tablist"]');
    const tabs = [...(tabList?.querySelectorAll?.('[role="tab"][data-causeway-tab]') ?? [])];
    const panels = [...(group?.children ?? [])]
      .filter(child => child.matches?.('[role="tabpanel"][data-causeway-tab-panel]'));
    const index = Number(tab.getAttribute('data-causeway-tab'));
    for (const candidate of tabs) {
      const selected = candidate === tab;
      candidate.setAttribute('aria-selected', String(selected));
      candidate.setAttribute('tabindex', selected ? '0' : '-1');
    }
    for (const panel of panels) {
      panel.hidden = Number(panel.getAttribute('data-causeway-tab-panel')) !== index;
    }
    if (focus) {
      tab.focus?.();
    }
  }

  #updateEmptyRegions() {
    if (!this.querySelectorAll) {
      return;
    }
    const semanticSelector = [
      'cw-object-header:not([hidden])',
      'cw-property:not([hidden])',
      'cw-action:not([hidden])',
      'cw-collection:not([hidden])'
    ].join(',');
    for (const region of this.querySelectorAll('[data-causeway-region="group"], [role="tabpanel"]')) {
      region.setAttribute('data-empty', String(!region.querySelector(semanticSelector)));
    }
    for (const group of this.querySelectorAll('[data-causeway-tab-group]')) {
      const tabList = group.querySelector(':scope > [role="tablist"]');
      const tabs = [...(tabList?.querySelectorAll('[role="tab"][data-causeway-tab]') ?? [])];
      const panels = [...group.children]
        .filter(child => child.matches?.('[role="tabpanel"][data-causeway-tab-panel]'));
      for (let index = 0; index < tabs.length; index += 1) {
        tabs[index].hidden = panels[index]?.getAttribute('data-empty') === 'true';
      }
      const selected = tabs.find(tab => tab.getAttribute('aria-selected') === 'true' && !tab.hidden);
      const replacement = selected ?? tabs.find(tab => !tab.hidden);
      if (replacement) {
        this.#activateTab(replacement);
      }
      group.setAttribute('data-empty', String(!replacement));
    }
    const structuralRegions = [...this.querySelectorAll(
      '[data-causeway-region="column"], [data-causeway-region="row"]'
    )].reverse();
    for (const region of structuralRegions) {
      const hasVisibleSemanticChild = Boolean(region.querySelector(semanticSelector));
      const hasVisibleStructuralChild = [...region.children].some(child =>
        ['row', 'column', 'group', 'tabs'].includes(child.getAttribute?.('data-causeway-region'))
          && child.getAttribute('data-empty') !== 'true');
      region.setAttribute('data-empty', String(!hasVisibleSemanticChild && !hasVisibleStructuralChild));
    }
  }
}

function layoutDiagnostic(code, message) {
  return Object.freeze({code, message, region: null, severity: 'warning'});
}
