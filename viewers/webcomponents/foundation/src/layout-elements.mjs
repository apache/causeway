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

import {CausewaySemanticEvent} from './component-contracts.mjs';
import {CausewayContextConsumerElement} from './context-consumer-element.mjs';
import {createSemanticEvent, requestObjectMemberAllocation} from './context-events.mjs';
import {
  extractCausewayLayoutFieldSet,
  parseCausewayGridXml,
  renderObjectLayoutMembers
} from './object-layout.mjs';
import {escapeHtml} from './rendering.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};
const OWNED_ATTRIBUTE = 'data-causeway-layout-owned';
let layoutSequence = 0;

export class CausewayLayoutContainerElement extends HTMLElementBase {
  constructor() {
    super();
    this.layoutObserver = null;
    this.layoutSyncScheduled = false;
    this.layoutDiagnosticKeys = new Set();
  }

  connectedCallback() {
    this.layoutObserver = new MutationObserver(() => this.scheduleLayoutSync());
    this.layoutObserver.observe(this, {childList: true});
    this.scheduleLayoutSync();
  }

  disconnectedCallback() {
    this.layoutObserver?.disconnect();
    this.layoutObserver = null;
    this.layoutSyncScheduled = false;
  }

  scheduleLayoutSync() {
    if (!this.isConnected || this.layoutSyncScheduled) return;
    this.layoutSyncScheduled = true;
    queueMicrotask(() => {
      this.layoutSyncScheduled = false;
      if (this.isConnected) this.synchronizeLayout();
    });
  }

  synchronizeLayout() {}

  validDirectChildren(allowedNames) {
    const valid = [];
    for (const child of [...(this.children ?? [])]) {
      if (child.hasAttribute?.(OWNED_ATTRIBUTE)) continue;
      if (allowedNames.includes(child.localName)) {
        if (child.hasAttribute?.('data-causeway-layout-invalid')) {
          child.removeAttribute('data-causeway-layout-invalid');
          child.hidden = false;
        }
        valid.push(child);
      } else {
        child.setAttribute?.('data-causeway-layout-invalid', '');
        child.hidden = true;
        this.publishLayoutDiagnostic(
          'UNSUPPORTED_LAYOUT_CHILD',
          `<${this.localName}> does not allow direct <${child.localName || 'unknown'}> children.`,
          child.localName || null
        );
      }
    }
    return valid;
  }

  publishLayoutDiagnostic(code, message, child = null) {
    const key = `${code}:${message}:${child ?? ''}`;
    if (this.layoutDiagnosticKeys.has(key)) return;
    this.layoutDiagnosticKeys.add(key);
    this.dispatchEvent(createSemanticEvent(
      CausewaySemanticEvent.LAYOUT_COMPONENT_DIAGNOSTIC,
      Object.freeze({element: this, diagnostic: Object.freeze({code, message, child, severity: 'warning'})}),
      {bubbles: true, composed: true}
    ));
  }
}

export class CausewayFieldsetElement extends CausewayLayoutContainerElement {
  static get observedAttributes() {
    return ['name'];
  }

  constructor() {
    super();
    this.heading = null;
    this.layoutId = `causeway-fieldset-${++layoutSequence}`;
  }

  get name() {
    return boundedName(this.getAttribute('name'), 'Properties');
  }

  set name(value) {
    this.setAttribute('name', String(value ?? ''));
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === 'name' && oldValue !== newValue) this.scheduleLayoutSync();
  }

  synchronizeLayout() {
    if (!this.heading) {
      this.heading = document.createElement('h2');
      this.heading.id = `${this.layoutId}-heading`;
      this.heading.setAttribute(OWNED_ATTRIBUTE, '');
      this.heading.setAttribute('class', 'causeway-layout-fieldset-heading');
      this.insertBefore(this.heading, this.firstChild);
    }
    this.setAttribute('class', mergeClass(this.getAttribute('class'), 'causeway-layout-fieldset'));
    this.setAttribute('role', 'group');
    this.setAttribute('aria-labelledby', this.heading.id);
    this.heading.textContent = this.name;
    this.validDirectChildren(['cw-property']);
  }
}

export class CausewayRowElement extends CausewayLayoutContainerElement {
  synchronizeLayout() {
    this.setAttribute('class', mergeClass(this.getAttribute('class'), 'causeway-layout-row'));
    this.validDirectChildren(['cw-column']);
  }
}

export class CausewayColumnElement extends CausewayLayoutContainerElement {
  static get observedAttributes() {
    return ['span'];
  }

  get span() {
    const value = this.getAttribute('span');
    if (value == null || value === '') return 12;
    const parsed = Number(value);
    return Number.isInteger(parsed) && parsed >= 1 && parsed <= 12 ? parsed : 12;
  }

  set span(value) {
    this.setAttribute('span', String(value ?? ''));
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === 'span' && oldValue !== newValue) this.scheduleLayoutSync();
  }

  synchronizeLayout() {
    this.setAttribute('class', mergeClass(this.getAttribute('class'), 'causeway-layout-column'));
    this.setAttribute('data-causeway-layout-span', String(this.span));
    const authored = this.getAttribute('span');
    if (authored != null && authored !== '' && String(this.span) !== authored) {
      this.publishLayoutDiagnostic(
        'INVALID_COLUMN_SPAN',
        `Column span '${boundedName(authored, '')}' is invalid; span 12 is used.`
      );
    }
    this.validDirectChildren([
      'cw-fieldset',
      'cw-collection',
      'cw-metadata',
      'cw-tabgroup',
      'cw-unreferenced-properties',
      'cw-unreferenced-collections',
      'cw-unreferenced-actions'
    ]);
  }
}

export class CausewayTabElement extends CausewayLayoutContainerElement {
  static get observedAttributes() {
    return ['name', 'selected', 'disabled'];
  }

  get name() {
    return boundedName(this.getAttribute('name'), 'Tab');
  }

  set name(value) {
    this.setAttribute('name', String(value ?? ''));
  }

  connectedCallback() {
    super.connectedCallback();
    this.parentNode?.scheduleLayoutSync?.();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.parentNode?.scheduleLayoutSync?.();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue === newValue) return;
    if (name === 'selected' && newValue != null) {
      this.parentNode?.selectTab?.(this, {focus: false});
    } else {
      this.parentNode?.scheduleLayoutSync?.();
    }
    this.scheduleLayoutSync();
  }

  synchronizeLayout() {
    this.setAttribute('class', mergeClass(this.getAttribute('class'), 'causeway-layout-tab-panel'));
    this.validDirectChildren(['cw-row']);
  }
}

export class CausewayTabgroupElement extends CausewayLayoutContainerElement {
  static get observedAttributes() {
    return ['name'];
  }

  constructor() {
    super();
    this.layoutId = `causeway-tabgroup-${++layoutSequence}`;
    this.tablist = null;
    this.selectedTab = null;
    this.tabButtons = new Map();
    this.addEventListener(CausewaySemanticEvent.UNREFERENCED_MEMBER_STATE, event => {
      const candidate = event.detail?.element;
      if (candidate?.parentNode === this && candidate.localName === 'cw-unreferenced-properties') {
        this.scheduleLayoutSync();
      }
    });
  }

  get name() {
    return boundedName(this.getAttribute('name'), 'Sections');
  }

  set name(value) {
    this.setAttribute('name', String(value ?? ''));
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === 'name' && oldValue !== newValue) this.scheduleLayoutSync();
  }

  synchronizeLayout() {
    this.setAttribute('class', mergeClass(this.getAttribute('class'), 'causeway-layout-tabgroup'));
    if (!this.tablist) {
      this.tablist = document.createElement('div');
      this.tablist.setAttribute(OWNED_ATTRIBUTE, '');
      this.tablist.setAttribute('class', 'causeway-layout-tablist');
      this.tablist.setAttribute('role', 'tablist');
      this.insertBefore(this.tablist, this.firstChild);
    }
    this.tablist.setAttribute('aria-label', this.name);
    const focusedTab = [...this.tabButtons.entries()]
      .find(([, button]) => button === document.activeElement)?.[0] ?? null;
    const previousSelected = this.selectedTab;
    const tabs = this.layoutTabs();
    const available = tabs.filter(tab => !tab.hasAttribute('disabled'));
    const selectionRetired = previousSelected != null && !available.includes(previousSelected);
    if (!available.includes(this.selectedTab)) {
      this.selectedTab = available.find(tab => tab.hasAttribute('selected')) ?? available[0] ?? null;
    }
    this.tabButtons.clear();
    this.tablist.replaceChildren();
    tabs.forEach((tab, index) => {
      const tabId = `${this.layoutId}-tab-${index}`;
      const panelId = `${this.layoutId}-panel-${index}`;
      const selected = tab === this.selectedTab;
      const button = document.createElement('button');
      button.type = 'button';
      button.id = tabId;
      button.textContent = tab.localName === 'cw-unreferenced-properties'
        ? tab.name
        : boundedName(tab.getAttribute('name'), `Tab ${index + 1}`);
      button.setAttribute('role', 'tab');
      button.setAttribute('aria-controls', panelId);
      button.setAttribute('aria-selected', String(selected));
      button.setAttribute('tabindex', selected ? '0' : '-1');
      button.setAttribute('data-causeway-layout-tab', String(index));
      if (tab.hasAttribute('disabled')) button.setAttribute('disabled', '');
      button.addEventListener('click', () => this.selectTab(tab, {focus: true}));
      button.addEventListener('keydown', event => this.handleTabKey(event, tab));
      this.tablist.appendChild(button);
      this.tabButtons.set(tab, button);
      tab.id = panelId;
      tab.setAttribute('role', 'tabpanel');
      tab.setAttribute('aria-labelledby', tabId);
      tab.setAttribute('data-causeway-layout-tab-panel', String(index));
      tab.hidden = !selected;
    });
    if (selectionRetired) {
      this.tabButtons.get(this.selectedTab)?.focus();
    } else if (focusedTab) {
      (this.tabButtons.get(focusedTab) ?? this.tabButtons.get(this.selectedTab))?.focus();
    }
  }

  layoutTabs() {
    const candidates = this.validDirectChildren(['cw-tab', 'cw-unreferenced-properties']);
    const tabs = [];
    for (const candidate of candidates) {
      if (candidate.localName === 'cw-unreferenced-properties'
          && candidate.getAttribute('data-causeway-unreferenced-state') !== 'ready') {
        candidate.hidden = true;
        candidate.removeAttribute('role');
        candidate.removeAttribute('aria-labelledby');
        candidate.removeAttribute('data-causeway-layout-tab-panel');
        continue;
      }
      if (candidate.localName === 'cw-unreferenced-properties') {
        candidate.setAttribute('class', mergeClass(candidate.getAttribute('class'), 'causeway-layout-tab-panel'));
      }
      tabs.push(candidate);
    }
    return tabs;
  }

  selectTab(tab, {focus = false} = {}) {
    const tabs = this.layoutTabs();
    if (!tabs.includes(tab) || tab.hasAttribute('disabled')) return false;
    this.selectedTab = tab;
    this.synchronizeLayout();
    if (focus) this.tabButtons.get(tab)?.focus();
    return true;
  }

  handleTabKey(event, tab) {
    const available = this.layoutTabs().filter(candidate => !candidate.hasAttribute('disabled'));
    const current = available.indexOf(tab);
    if (current < 0) return;
    let target = null;
    if (event.key === 'Home') target = available[0];
    if (event.key === 'End') target = available.at(-1);
    if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
      const rtl = directionOf(this) === 'rtl';
      const forward = event.key === (rtl ? 'ArrowLeft' : 'ArrowRight');
      target = available[(current + (forward ? 1 : -1) + available.length) % available.length];
    }
    if (!target) return;
    event.preventDefault();
    this.selectTab(target, {focus: true});
  }
}

export class CausewayMetadataElement extends CausewayContextConsumerElement {
  constructor() {
    super();
    this.metadataRevision = 0;
    this.metadataAbortController = null;
    this.metadataSignature = null;
    this.metadataClaimSource = null;
    this.metadataMenu = null;
    this.metadataMenuTrigger = null;
    this.metadataOutsideListener = event => {
      const target = event.target;
      const activePrompt = target?.closest?.('.causeway-action-prompt, [data-causeway-inline-action-prompt]');
      if (this.metadataMenu?.hasAttribute('open') && !this.metadataMenu.contains(target) && !activePrompt) {
        this.closeMetadataMenu();
      }
    };
    this.metadataResultListener = event => {
      const actionId = event.detail?.actionId;
      const belongsToMenu = actionId && [...(this.metadataMenu?.querySelectorAll?.('cw-action') ?? [])]
        .some(action => action.id === actionId);
      if (!this.metadataMenu?.hasAttribute('open') || !belongsToMenu) return;
      const trigger = this.metadataMenuTrigger;
      this.closeMetadataMenu();
      queueMicrotask(() => {
        if (trigger?.isConnected) trigger.focus();
      });
    };
  }

  connectedCallback() {
    this.metadataClaimSource = requestObjectMemberAllocation(this)?.registerClaimSource(
      this,
      ['property', 'action']
    ) ?? null;
    super.connectedCallback();
  }

  createRequirement() {
    return {kind: 'layout'};
  }

  disconnectedCallback() {
    this.metadataRevision += 1;
    this.metadataAbortController?.abort();
    this.metadataAbortController = null;
    this.metadataSignature = null;
    this.retireMetadataMenu();
    this.metadataClaimSource?.release();
    this.metadataClaimSource = null;
    super.disconnectedCallback();
  }

  renderComponentState(state) {
    if (!state || ['idle', 'schema-loading', 'object-loading'].includes(state.status)) {
      if (this.getAttribute('data-causeway-metadata-state') !== 'ready') {
        this.metadataClaimSource?.pending();
        this.renderMetadataStatus('loading', 'Loading metadata…');
      }
      return;
    }
    if (state.status === 'terminal-error') {
      this.retireMetadataLoad();
      this.failMetadata('METADATA_CONTEXT_UNAVAILABLE', 'No usable Causeway object context is available.');
      return;
    }
    if (state.status === 'partial-error' || state.status === 'unsupported') {
      this.retireMetadataLoad();
      this.failMetadata('METADATA_GRID_UNAVAILABLE', 'The effective metadata layout is unavailable.');
      return;
    }
    void this.prepareMetadata(state);
  }

  retireMetadataLoad() {
    this.metadataRevision += 1;
    this.metadataClaimSource?.settle();
    this.metadataAbortController?.abort();
    this.metadataAbortController = null;
    this.metadataSignature = null;
    this.retireMetadataMenu();
  }

  async prepareMetadata(state) {
    const context = this._resolvedContext;
    const gridPath = state.data?.grid;
    const signature = gridPath ?? '';
    if (signature === this.metadataSignature) return;
    this.metadataSignature = signature;
    this.metadataClaimSource?.pending();
    const revision = ++this.metadataRevision;
    this.metadataAbortController?.abort();
    const abortController = new AbortController();
    this.metadataAbortController = abortController;
    if (!context || !gridPath) {
      abortController.abort();
      this.metadataAbortController = null;
      this.failMetadata('METADATA_GRID_UNAVAILABLE', 'The effective metadata layout is unavailable.');
      return;
    }
    this.renderMetadataStatus('loading', 'Loading metadata…');
    try {
      const description = await context.describeObject();
      const resource = await context.loadStructuralResource(gridPath, {
        accept: 'application/xml, text/xml',
        signal: abortController.signal
      });
      if (revision !== this.metadataRevision || !this.isConnected) return;
      const parsed = parseCausewayGridXml(resource.text, {members: description.members});
      const metadata = extractCausewayLayoutFieldSet(parsed.plan, 'metadata');
      if (!parsed.usable || !metadata) {
        this.failMetadata('METADATA_FIELDSET_UNAVAILABLE', 'The effective layout has no usable metadata fieldset.');
        return;
      }
      this.renderMetadata(metadata);
    } catch (error) {
      if (error?.name === 'AbortError' || revision !== this.metadataRevision || !this.isConnected) return;
      this.failMetadata('METADATA_RESOURCE_UNAVAILABLE', 'The effective metadata layout could not be loaded.');
    } finally {
      if (this.metadataAbortController === abortController) this.metadataAbortController = null;
    }
  }

  renderMetadata(metadata) {
    this.retireMetadataMenu();
    this.metadataClaimSource?.resolve([
      ...metadata.properties.map(node => ({kind: 'property', id: node.memberId})),
      ...metadata.actions.map(node => ({kind: 'action', id: node.memberId}))
    ]);
    const id = `${this.id || `causeway-metadata-${++layoutSequence}`}-legend`;
    const menuId = `${id}-actions`;
    const properties = renderObjectLayoutMembers(metadata.properties, {idPrefix: `${id}-properties`});
    const actions = renderObjectLayoutMembers(metadata.actions, {idPrefix: menuId});
    const dropdown = metadata.actions.length > 0
      ? `<details class="causeway-metadata-actions" data-causeway-metadata-actions><summary aria-label="Metadata actions" aria-controls="${escapeHtml(menuId)}" aria-expanded="false" title="Metadata actions"><span aria-hidden="true">&#8942;</span></summary><div id="${escapeHtml(menuId)}" class="causeway-metadata-actions-menu" role="menu" aria-label="Metadata actions">${actions}</div></details>`
      : '';
    this.setAttribute('data-causeway-metadata-state', 'ready');
    this.innerHTML = `<fieldset class="causeway-layout-fieldset causeway-metadata-fieldset"><legend id="${escapeHtml(id)}"><span>${escapeHtml(metadata.label || 'Metadata')}</span></legend>${properties}${dropdown}</fieldset>`;
    this.bindMetadataMenu();
  }

  bindMetadataMenu() {
    const menu = this.querySelector?.('[data-causeway-metadata-actions]') ?? null;
    const trigger = menu?.querySelector?.('summary') ?? null;
    if (!menu || !trigger) return;
    this.metadataMenu = menu;
    this.metadataMenuTrigger = trigger;
    menu.addEventListener('toggle', () => this.synchronizeMetadataMenu());
    menu.addEventListener('keydown', event => {
      if (event.key !== 'Escape' || !menu.hasAttribute('open')) return;
      event.preventDefault();
      event.stopPropagation();
      this.closeMetadataMenu({restoreFocus: true});
    });
    this.synchronizeMetadataMenu();
  }

  synchronizeMetadataMenu() {
    const open = this.metadataMenu?.hasAttribute('open') === true;
    this.metadataMenuTrigger?.setAttribute('aria-expanded', String(open));
    document.removeEventListener?.('pointerdown', this.metadataOutsideListener);
    document.removeEventListener?.(CausewaySemanticEvent.ACTION_RESULT, this.metadataResultListener);
    if (open) {
      document.addEventListener?.('pointerdown', this.metadataOutsideListener);
      document.addEventListener?.(CausewaySemanticEvent.ACTION_RESULT, this.metadataResultListener);
    }
  }

  closeMetadataMenu({restoreFocus = false} = {}) {
    const trigger = this.metadataMenuTrigger;
    this.metadataMenu?.removeAttribute('open');
    this.synchronizeMetadataMenu();
    if (restoreFocus && trigger?.isConnected) trigger.focus();
  }

  retireMetadataMenu() {
    document.removeEventListener?.('pointerdown', this.metadataOutsideListener);
    document.removeEventListener?.(CausewaySemanticEvent.ACTION_RESULT, this.metadataResultListener);
    this.metadataMenu?.removeAttribute('open');
    this.metadataMenuTrigger?.setAttribute('aria-expanded', 'false');
    this.metadataMenu = null;
    this.metadataMenuTrigger = null;
  }

  renderMetadataStatus(state, message) {
    this.retireMetadataMenu();
    this.setAttribute('data-causeway-metadata-state', state);
    this.innerHTML = `<p class="causeway-layout-status causeway-${state === 'error' ? 'error' : 'loading'}" role="status">${escapeHtml(message)}</p>`;
  }

  failMetadata(code, message) {
    this.metadataClaimSource?.settle();
    this.renderMetadataStatus('error', message);
    this.dispatchEvent(createSemanticEvent(
      CausewaySemanticEvent.LAYOUT_COMPONENT_DIAGNOSTIC,
      Object.freeze({element: this, diagnostic: Object.freeze({code, message, child: null, severity: 'warning'})}),
      {bubbles: true, composed: true}
    ));
  }
}

function boundedName(value, fallback) {
  const normalized = String(value ?? '').replace(/\s+/g, ' ').trim().slice(0, 160);
  return normalized || fallback;
}

function mergeClass(value, required) {
  return [...new Set(`${value ?? ''} ${required}`.trim().split(/\s+/).filter(Boolean))].join(' ');
}

function directionOf(element) {
  try {
    return globalThis.getComputedStyle?.(element)?.direction || element.getAttribute('dir') || 'ltr';
  } catch {
    return element.getAttribute('dir') || 'ltr';
  }
}
