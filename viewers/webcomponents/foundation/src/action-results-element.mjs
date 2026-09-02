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

import {CausewayHostClass} from './component-contracts.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};

export class CausewayActionResultsElement extends HTMLElementBase {
  static get observedAttributes() {
    return ['aria-label'];
  }

  constructor() {
    super();
    this._observer = typeof globalThis.MutationObserver === 'function'
      ? new MutationObserver(() => this.#synchronize())
      : null;
  }

  connectedCallback() {
    this.classList?.add?.(CausewayHostClass.ACTION_RESULTS);
    this.setAttribute('role', 'region');
    this._observer?.observe(this, {childList: true});
    this.#synchronize();
  }

  disconnectedCallback() {
    this._observer?.disconnect();
  }

  attributeChangedCallback() {
    if (this.isConnected) this.#synchronize();
  }

  replacePresentation(...nodes) {
    this.replaceChildren(...nodes.filter(Boolean));
    this.#synchronize();
  }

  clear() {
    this.replaceChildren();
    this.#synchronize();
  }

  #synchronize() {
    if (!this.getAttribute('aria-label')?.trim()) this.setAttribute('aria-label', 'Action results');
    this.hidden = (this.children?.length ?? this.childNodes?.length ?? 0) === 0;
  }
}
