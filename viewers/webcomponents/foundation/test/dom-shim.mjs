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

export function installDomShim() {
  class ShimEvent {
    constructor(type, {bubbles = false, composed = false, cancelable = false} = {}) {
      this.type = type;
      this.bubbles = bubbles;
      this.composed = composed;
      this.cancelable = cancelable;
      this.defaultPrevented = false;
      this.propagationStopped = false;
      this.target = null;
      this.currentTarget = null;
    }
    stopPropagation() {
      this.propagationStopped = true;
    }
    preventDefault() {
      if (this.cancelable) {
        this.defaultPrevented = true;
      }
    }
  }

  class ShimCustomEvent extends ShimEvent {
    constructor(type, options = {}) {
      super(type, options);
      this.detail = options.detail;
    }
  }

  class ShimEventTarget {
    constructor() {
      this.listeners = new Map();
    }
    addEventListener(type, listener) {
      const listeners = this.listeners.get(type) ?? new Set();
      listeners.add(listener);
      this.listeners.set(type, listeners);
    }
    removeEventListener(type, listener) {
      this.listeners.get(type)?.delete(listener);
    }
    dispatchEvent(event) {
      event.target ??= this;
      event.currentTarget = this;
      for (const listener of [...(this.listeners.get(event.type) ?? [])]) {
        if (typeof listener === 'function') {
          listener.call(this, event);
        } else {
          listener.handleEvent(event);
        }
      }
      if (event.bubbles && !event.propagationStopped && this.parentNode) {
        this.parentNode.dispatchEvent(event);
      }
      return !event.defaultPrevented;
    }
  }

  class ShimHTMLElement extends ShimEventTarget {
    constructor() {
      super();
      this.attributes = new Map();
      this.parentNode = null;
      this.childNodes = [];
      this.isConnected = false;
      this._innerHTML = '';
      this.hidden = false;
      this.dataset = {};
      this._mutationObservers = new Set();
    }
    setAttribute(name, value) {
      const oldValue = this.getAttribute(name);
      const newValue = String(value);
      this.attributes.set(name, newValue);
      if (name.startsWith('data-')) {
        this.dataset[name.slice(5).replace(/-([a-z])/g, (_, character) => character.toUpperCase())] = newValue;
      }
      if (this.constructor.observedAttributes?.includes(name)) {
        this.attributeChangedCallback?.(name, oldValue, newValue);
      }
    }
    getAttribute(name) {
      return this.attributes.has(name) ? this.attributes.get(name) : null;
    }
    hasAttribute(name) {
      return this.attributes.has(name);
    }
    removeAttribute(name) {
      const oldValue = this.getAttribute(name);
      this.attributes.delete(name);
      if (this.constructor.observedAttributes?.includes(name)) {
        this.attributeChangedCallback?.(name, oldValue, null);
      }
    }
    get children() {
      return this.childNodes;
    }
    get firstChild() {
      return this.childNodes[0] ?? null;
    }
    get innerHTML() {
      if (this.childNodes.length === 0) {
        return this._innerHTML;
      }
      return `${this._innerHTML}${this.childNodes.map(child => child.innerHTML ?? '').join('')}`;
    }
    set innerHTML(value) {
      this._innerHTML = String(value ?? '');
    }
    appendChild(child) {
      return this.insertBefore(child, null);
    }
    insertBefore(child, reference) {
      child.parentNode = this;
      const index = reference == null ? this.childNodes.length : this.childNodes.indexOf(reference);
      this.childNodes.splice(index < 0 ? this.childNodes.length : index, 0, child);
      if (this.isConnected) {
        connectTree(child);
      }
      notifyMutation(this, {type: 'childList', target: this, addedNodes: [child], removedNodes: []});
      return child;
    }
    contains(candidate) {
      for (let current = candidate; current; current = current.parentNode) {
        if (current === this) {
          return true;
        }
      }
      return false;
    }
    replaceChildren(...children) {
      for (const child of [...this.childNodes]) this.removeChild(child);
      for (const child of children) this.appendChild(child);
      this.innerHTML = '';
    }
    focus() {
      document.activeElement = this;
    }
    blur() {
      if (document.activeElement === this) document.activeElement = null;
    }
    removeChild(child) {
      const index = this.childNodes.indexOf(child);
      if (index >= 0) {
        this.childNodes.splice(index, 1);
        disconnectTree(child);
        child.parentNode = null;
        notifyMutation(this, {type: 'childList', target: this, addedNodes: [], removedNodes: [child]});
      }
      return child;
    }
  }

  class ShimMutationObserver {
    constructor(callback) {
      this.callback = callback;
      this.targets = new Set();
    }
    observe(target) {
      this.targets.add(target);
      target._mutationObservers?.add(this);
    }
    disconnect() {
      for (const target of this.targets) {
        target._mutationObservers?.delete(this);
      }
      this.targets.clear();
    }
  }

  const registry = new Map();
  const customElements = {
    define(name, constructor) {
      registry.set(name, constructor);
    },
    get(name) {
      return registry.get(name);
    },
    whenDefined(name) {
      return registry.has(name) ? Promise.resolve(registry.get(name)) : Promise.reject(new Error(`Custom element ${name} is not defined.`));
    }
  };
  const body = new ShimHTMLElement();
  body.isConnected = true;
  const document = new ShimEventTarget();
  document.body = body;
  document.createElement = name => {
    const Constructor = registry.get(name) ?? ShimHTMLElement;
    const element = new Constructor();
    element.localName = name;
    return element;
  };

  Object.assign(globalThis, {
    Event: ShimEvent,
    CustomEvent: ShimCustomEvent,
    EventTarget: ShimEventTarget,
    HTMLElement: ShimHTMLElement,
    MutationObserver: ShimMutationObserver,
    customElements,
    document
  });

  function notifyMutation(target, record) {
    for (const observer of [...(target._mutationObservers ?? [])]) {
      observer.callback([record], observer);
    }
  }

  function connectTree(element) {
    if (element.isConnected) {
      return;
    }
    element.isConnected = true;
    element.connectedCallback?.();
    for (const child of element.childNodes) {
      connectTree(child);
    }
  }

  function disconnectTree(element) {
    if (!element.isConnected) {
      return;
    }
    for (const child of element.childNodes) {
      disconnectTree(child);
    }
    element.disconnectedCallback?.();
    element.isConnected = false;
  }

  return {document, customElements, ShimHTMLElement};
}
