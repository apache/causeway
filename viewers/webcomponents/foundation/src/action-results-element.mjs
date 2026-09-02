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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {CausewayAttribute, CausewayHostClass} from './component-contracts.mjs';
import {ACTION_RESULTS_DISMISS_REQUEST_EVENT, createSemanticEvent} from './context-events.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};

export const ActionResultPresentationStyle = Object.freeze({
  INLINE: 'INLINE',
  DIALOG: 'DIALOG',
  SIDEBAR: 'SIDEBAR'
});

const PRESENTATION_STYLES = new Set(Object.values(ActionResultPresentationStyle));
const FOCUSABLE_SELECTOR = [
  'button:not([disabled])',
  '[href]',
  'input:not([disabled])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  '[tabindex]:not([tabindex="-1"])'
].join(',');

export function normalizeActionResultPresentationStyle(value) {
  const candidate = String(value ?? '').trim().toUpperCase();
  return PRESENTATION_STYLES.has(candidate) ? candidate : ActionResultPresentationStyle.INLINE;
}

export class CausewayActionResultsElement extends HTMLElementBase {
  static get observedAttributes() {
    return ['aria-label', CausewayAttribute.PRESENTATION_STYLE];
  }

  constructor() {
    super();
    this._presentationNodes = [];
    this._presentationOrigin = null;
    this._surface = null;
    this._rendering = false;
    this._observer = typeof globalThis.MutationObserver === 'function'
      ? new MutationObserver(() => this.#handleExternalMutation())
      : null;
    this.onSurfaceKeydown = event => this.#handleSurfaceKeydown(event);
  }

  get presentationStyle() {
    return normalizeActionResultPresentationStyle(this.getAttribute(CausewayAttribute.PRESENTATION_STYLE));
  }

  set presentationStyle(value) {
    this.setAttribute(CausewayAttribute.PRESENTATION_STYLE, normalizeActionResultPresentationStyle(value));
  }

  get presentationNodes() {
    return Object.freeze([...this._presentationNodes]);
  }

  get presentationContext() {
    return Object.freeze({origin: this._presentationOrigin});
  }

  connectedCallback() {
    this.classList?.add?.(CausewayHostClass.ACTION_RESULTS);
    this.setAttribute('role', 'region');
    if (this._presentationNodes.length === 0 && !this._surface) {
      this._presentationNodes = [...(this.children ?? this.childNodes ?? [])];
    }
    this._observer?.observe(this, {childList: true});
    this.addEventListener('keydown', this.onSurfaceKeydown);
    this.#renderPresentation();
  }

  disconnectedCallback() {
    this._observer?.disconnect();
    this.removeEventListener('keydown', this.onSurfaceKeydown);
    this.#closeSurface();
    this._presentationNodes = [];
    this._presentationOrigin = null;
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (!this.isConnected || oldValue === newValue) return;
    if (name === CausewayAttribute.PRESENTATION_STYLE) this.#renderPresentation();
    else this.#synchronizeAccessibility();
  }

  setPresentationContext({origin = null} = {}) {
    this._presentationOrigin = eligibleFocusOrigin(origin) ? origin : null;
  }

  replacePresentation(...nodes) {
    this._presentationNodes = nodes.filter(Boolean);
    if (this._presentationNodes.length === 0) this._presentationOrigin = null;
    this.#renderPresentation();
  }

  clear({restoreFocus = true} = {}) {
    const origin = restoreFocus ? this._presentationOrigin : null;
    this._presentationNodes = [];
    this._presentationOrigin = null;
    this.#renderPresentation();
    if (eligibleFocusOrigin(origin)) {
      schedule(() => {
        if (eligibleFocusOrigin(origin)) origin.focus();
      });
    }
  }

  dismiss() {
    this.clear({restoreFocus: true});
  }

  requestDismiss(reason = 'explicit') {
    const event = createSemanticEvent(ACTION_RESULTS_DISMISS_REQUEST_EVENT, Object.freeze({
      outlet: this,
      reason: String(reason ?? 'explicit')
    }), {cancelable: true});
    if (this.dispatchEvent(event)) this.dismiss();
  }

  #renderPresentation() {
    if (!this.isConnected) return;
    this._rendering = true;
    this.#closeSurface();
    const nodes = [...this._presentationNodes];
    const style = this.presentationStyle;
    this.dataset.causewayPresentationStyle = style;
    try {
      if (nodes.length === 0) {
        this.replaceChildren();
        this.hidden = true;
        this.#synchronizeAccessibility();
        return;
      }
      if (style === ActionResultPresentationStyle.INLINE) {
        this.replaceChildren(...nodes);
      } else {
        const surface = this.#createSurface(style);
        surface.replaceChildren(...nodes);
        this._surface = surface;
        this.replaceChildren(surface);
        this.#openSurface(surface, style);
      }
      this.hidden = false;
      this.#synchronizeAccessibility();
    } finally {
      this._rendering = false;
    }
  }

  #createSurface(style) {
    const dialog = style === ActionResultPresentationStyle.DIALOG;
    const surface = globalThis.document.createElement(dialog ? 'dialog' : 'aside');
    surface.className = `causeway-action-results-surface causeway-action-results-${style.toLowerCase()}`;
    surface.setAttribute('data-causeway-action-results-surface', style);
    surface.setAttribute('aria-label', this.#accessibleName());
    surface.setAttribute('role', dialog ? 'dialog' : 'complementary');
    if (dialog) {
      surface.setAttribute('aria-modal', 'true');
      surface.tabIndex = -1;
      surface.addEventListener('cancel', event => {
        event.preventDefault();
        this.requestDismiss('escape');
      });
    }
    return surface;
  }

  #openSurface(surface, style) {
    if (style === ActionResultPresentationStyle.DIALOG) {
      if (typeof surface.showModal === 'function') {
        try {
          surface.showModal();
        } catch {
          surface.setAttribute('open', '');
        }
      } else {
        surface.setAttribute('open', '');
      }
      schedule(() => {
        if (surface.isConnected && this._surface === surface) {
          const controls = this.#focusableControls(surface);
          (controls[0] ?? surface).focus?.();
        }
      });
    }
  }

  #closeSurface() {
    const surface = this._surface;
    this._surface = null;
    if (!surface) return;
    if (surface.localName === 'dialog' && typeof surface.close === 'function') {
      try {
        surface.close();
      } catch {
        // Removing a disconnected dialog is sufficient cleanup.
      }
    }
  }

  #handleSurfaceKeydown(event) {
    const surface = this._surface;
    if (!surface) return;
    if (event.key === 'Escape') {
      if (this.presentationStyle === ActionResultPresentationStyle.DIALOG
          || surface.contains?.(globalThis.document?.activeElement)
          || surface.contains?.(event.target)) {
        event.preventDefault();
        event.stopPropagation();
        this.requestDismiss('escape');
      }
      return;
    }
    if (event.key !== 'Tab' || this.presentationStyle !== ActionResultPresentationStyle.DIALOG) return;
    const controls = this.#focusableControls(surface);
    if (controls.length === 0) {
      event.preventDefault();
      surface.focus?.();
      return;
    }
    const active = globalThis.document?.activeElement;
    const first = controls[0];
    const last = controls.at(-1);
    if (event.shiftKey && (active === first || active === surface)) {
      event.preventDefault();
      last.focus?.();
    } else if (!event.shiftKey && active === last) {
      event.preventDefault();
      first.focus?.();
    }
  }

  #focusableControls(surface) {
    const selected = surface.querySelectorAll?.(FOCUSABLE_SELECTOR);
    const controls = selected ? [...selected] : descendantsOf(surface).filter(isFocusableControl);
    return controls.filter(control => !control.disabled && control.getAttribute?.('aria-hidden') !== 'true');
  }

  #handleExternalMutation() {
    if (this._rendering || !this.isConnected) return;
    if (this._surface && this._surface.parentNode === this) {
      this.#synchronizeAccessibility();
      return;
    }
    this._presentationNodes = [...(this.children ?? this.childNodes ?? [])];
    this.hidden = this._presentationNodes.length === 0;
    this.#synchronizeAccessibility();
  }

  #synchronizeAccessibility() {
    if (!this.getAttribute('aria-label')?.trim()) this.setAttribute('aria-label', 'Action results');
    if (this._surface) this._surface.setAttribute('aria-label', this.#accessibleName());
  }

  #accessibleName() {
    return this.getAttribute('aria-label')?.trim() || 'Action results';
  }
}

function eligibleFocusOrigin(origin) {
  return Boolean(origin && origin.isConnected !== false && typeof origin.focus === 'function');
}

function descendantsOf(root) {
  const descendants = [];
  for (const child of [...(root?.children ?? root?.childNodes ?? [])]) {
    descendants.push(child, ...descendantsOf(child));
  }
  return descendants;
}

function isFocusableControl(control) {
  if (control?.disabled) return false;
  if (['button', 'input', 'select', 'textarea'].includes(control?.localName)) return true;
  if (control?.hasAttribute?.('href')) return true;
  return control?.hasAttribute?.('tabindex') && control.getAttribute('tabindex') !== '-1';
}

function schedule(callback) {
  if (typeof globalThis.requestAnimationFrame === 'function') globalThis.requestAnimationFrame(callback);
  else callback();
}
