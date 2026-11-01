/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {appendActionContent, renderActionContent} from './action-presentation.mjs';
import {escapeHtml} from './rendering.mjs';

export const CAUSEWAY_ACTION_CONTROL = 'cw-action-control';
export const CAUSEWAY_ACTION_WIDGET_POLICY_EVENT = 'causeway-action-widget-policy';
const DEFAULT_MODULE_URL = new URL('./vaadin-actions/vaadin-actions.js', import.meta.url).href;
let configuration = Object.freeze({enabled: true, moduleUrl: DEFAULT_MODULE_URL});
let modulePromise = null;
let failed = false;
let configurationRevision = 0;

const documentPolicy = globalThis.document?.documentElement?.dataset;
if (documentPolicy && Object.hasOwn(documentPolicy, 'causewayActionButtons')) {
  configureCausewayActionWidgets({enabled: documentPolicy.causewayActionButtons === 'vaadin'});
}

export function configureCausewayActionWidgets(options = {}) {
  configuration = Object.freeze({
    enabled: options.enabled !== false,
    moduleUrl: options.moduleUrl ? safeModuleUrl(options.moduleUrl) : DEFAULT_MODULE_URL
  });
  modulePromise = null;
  failed = false;
  configurationRevision += 1;
  announcePolicyChange({reason: 'configuration'});
  return configuration;
}

export function causewayActionWidgetConfiguration() {
  return configuration;
}

export function useCausewayActionWidget() {
  return configuration.enabled && !failed;
}

export function renderCausewayActionWidget({label, describedBy = '', disabled = false, testId = '', icon = null} = {}) {
  const attributes = [
    `data-label="${escapeHtml(label ?? '')}"`,
    'data-causeway-action-control'
  ];
  if (describedBy) attributes.push(`data-describedby="${escapeHtml(describedBy)}"`);
  if (testId) attributes.push(`data-control-testid="${escapeHtml(testId)}"`);
  if (disabled) attributes.push('disabled');
  if (icon) {
    attributes.push(`data-icon-classes="${escapeHtml(icon.classes.join(' '))}"`);
    attributes.push(`data-icon-position="${escapeHtml(icon.position)}"`);
  }
  const native = nativeButtonMarkup(label, describedBy, disabled, testId, icon);
  return `<${CAUSEWAY_ACTION_CONTROL} ${attributes.join(' ')}>${native}</${CAUSEWAY_ACTION_CONTROL}>`;
}

export function renderNativeCausewayActionButton({label, describedBy = '', disabled = false, testId = '', icon = null} = {}) {
  return nativeButtonMarkup(label, describedBy, disabled, testId, icon);
}

export class CausewayActionControlElement extends HTMLElement {
  constructor() {
    super();
    this._control = null;
    this._generation = 0;
    this._focusRequested = false;
  }

  connectedCallback() {
    if (!configuration.enabled || failed) {
      this.dataset.widgetState = 'native';
      return;
    }
    const generation = ++this._generation;
    const revision = configurationRevision;
    this.dataset.widgetState = 'loading';
    void this.#upgrade(generation, revision);
  }

  disconnectedCallback() {
    this._generation += 1;
    this._control = null;
  }

  focus(options) {
    this._focusRequested = true;
    (this._control ?? this.querySelector?.('button'))?.focus?.(options);
  }

  async #upgrade(generation, revision) {
    try {
      modulePromise ??= import(configuration.moduleUrl);
      await modulePromise;
      if (!this.isConnected || generation !== this._generation || revision !== configurationRevision) return;
      await globalThis.customElements.whenDefined('vaadin-button');
      if (!this.isConnected || generation !== this._generation || revision !== configurationRevision) return;
      const control = document.createElement('vaadin-button');
      const iconClasses = String(this.dataset.iconClasses ?? '').split(/\s+/).filter(Boolean);
      const icon = iconClasses.length > 0
        ? Object.freeze({classes: Object.freeze(iconClasses), position: this.dataset.iconPosition === 'RIGHT' ? 'RIGHT' : 'LEFT'})
        : null;
      appendActionContent(control, this.dataset.label ?? '', icon);
      control.setAttribute('theme', 'primary');
      control.disabled = this.hasAttribute('disabled');
      if (this.dataset.describedby) control.setAttribute('aria-describedby', this.dataset.describedby);
      if (this.dataset.controlTestid) control.setAttribute('data-testid', this.dataset.controlTestid);
      this._control = control;
      this.replaceChildren(control);
      await control.updateComplete;
      if (!this.isConnected || generation !== this._generation || revision !== configurationRevision) return;
      this.dataset.widgetState = 'ready';
      if (this._focusRequested) queueMicrotask(() => control.focus());
    } catch (error) {
      if (!this.isConnected || generation !== this._generation || revision !== configurationRevision) return;
      failed = true;
      modulePromise = null;
      this.dataset.widgetState = 'fallback';
      this.dataset.widgetError = 'The configured action control could not be loaded.';
      this.dispatchEvent(new CustomEvent('causeway-action-load-failed', {
        bubbles: true,
        composed: true,
        detail: Object.freeze({message: this.dataset.widgetError})
      }));
      announcePolicyChange({reason: 'failure', message: this.dataset.widgetError});
    }
  }
}

function nativeButtonMarkup(label, describedBy, disabled, testId, icon) {
  return `<button type="button" data-causeway-action-control${disabled ? ' disabled aria-disabled="true"' : ''}${describedBy ? ` aria-describedby="${escapeHtml(describedBy)}"` : ''}${testId ? ` data-testid="${escapeHtml(testId)}"` : ''}>${renderActionContent(label, icon)}</button>`;
}

function announcePolicyChange(detail) {
  globalThis.document?.dispatchEvent?.(new CustomEvent(CAUSEWAY_ACTION_WIDGET_POLICY_EVENT, {
    detail: Object.freeze(detail)
  }));
}

function safeModuleUrl(value) {
  const url = new URL(value, globalThis.document?.baseURI ?? import.meta.url);
  if (!['http:', 'https:', 'file:'].includes(url.protocol)) throw new Error('Action module URL must use an approved module protocol.');
  return url.href;
}
