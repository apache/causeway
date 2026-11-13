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
import {createSemanticEvent, requestObjectContext} from './context-events.mjs';
import {escapeHtml} from './rendering.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};

export class CausewayObjectLinkElement extends HTMLElementBase {
  static get observedAttributes() {
    return ['logical-type', 'object-id', 'title', 'icon', 'disabled'];
  }

  constructor() {
    super();
    this.addEventListener('click', event => {
      if (event.target !== this) {
        this.activate();
      }
    });
  }

  get target() {
    return Object.freeze({
      logicalTypeName: this.getAttribute('logical-type') || '',
      id: this.getAttribute('object-id') || '',
      title: this.getAttribute('title') || this.getAttribute('object-id') || ''
    });
  }

  set target(value) {
    this.setAttribute('logical-type', value?.logicalTypeName ?? '');
    this.setAttribute('object-id', value?.id ?? '');
    this.setAttribute('title', value?.title ?? value?.id ?? '');
  }

  get icon() {
    return this.getAttribute('icon') || '';
  }

  set icon(value) {
    if (value == null || String(value).trim() === '') {
      this.removeAttribute('icon');
    } else {
      this.setAttribute('icon', String(value));
    }
  }

  get disabled() {
    return this.hasAttribute('disabled');
  }

  set disabled(value) {
    if (value) {
      this.setAttribute('disabled', '');
    } else {
      this.removeAttribute('disabled');
    }
  }

  connectedCallback() {
    this.render();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue !== newValue && this.isConnected) {
      this.render();
    }
  }

  activate() {
    const target = this.target;
    if (this.disabled || !target.logicalTypeName || !target.id) {
      return false;
    }
    return this.dispatchEvent(createSemanticEvent(
      CausewaySemanticEvent.NAVIGATION_REQUEST,
      Object.freeze({target, sourceContext: requestObjectContext(this)}),
      {cancelable: true}
    ));
  }

  render() {
    const target = this.target;
    const disabled = this.disabled || !target.logicalTypeName || !target.id;
    const icon = this.icon;
    this.innerHTML = `<button type="button" class="causeway-object-link" role="link"${disabled ? ' disabled aria-disabled="true"' : ''}>
  ${icon ? `<img class="causeway-object-link-icon" src="${escapeHtml(icon)}" alt="" aria-hidden="true">` : ''}
  <span class="causeway-object-link-title">${escapeHtml(target.title)}</span>
  <span class="causeway-object-link-identity">${escapeHtml(target.logicalTypeName)}:${escapeHtml(target.id)}</span>
</button>`;
    const image = this.querySelector?.('.causeway-object-link-icon');
    image?.addEventListener?.('error', () => {
      image.hidden = true;
      image.setAttribute('aria-hidden', 'true');
    }, {once: true});
  }
}
