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
      this.innerHTML = '';
      this.hidden = false;
    }
    setAttribute(name, value) {
      const oldValue = this.getAttribute(name);
      const newValue = String(value);
      this.attributes.set(name, newValue);
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
    appendChild(child) {
      child.parentNode = this;
      this.childNodes.push(child);
      if (this.isConnected) {
        connectTree(child);
      }
      return child;
    }
    removeChild(child) {
      const index = this.childNodes.indexOf(child);
      if (index >= 0) {
        this.childNodes.splice(index, 1);
        disconnectTree(child);
        child.parentNode = null;
      }
      return child;
    }
  }

  const registry = new Map();
  const customElements = {
    define(name, constructor) {
      registry.set(name, constructor);
    },
    get(name) {
      return registry.get(name);
    }
  };
  const body = new ShimHTMLElement();
  body.isConnected = true;
  const document = {
    body,
    createElement(name) {
      const Constructor = registry.get(name) ?? ShimHTMLElement;
      const element = new Constructor();
      element.localName = name;
      return element;
    }
  };

  Object.assign(globalThis, {
    Event: ShimEvent,
    CustomEvent: ShimCustomEvent,
    EventTarget: ShimEventTarget,
    HTMLElement: ShimHTMLElement,
    customElements,
    document
  });

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
