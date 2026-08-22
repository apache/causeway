/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

const events = [];
const styleCreations = [];
const styleMutations = [];
const policyMode = new URL(location.href).searchParams.get('policy') ?? 'exact';
const originalCreateElement = document.createElement.bind(document);

if (policyMode.startsWith('nonce')) globalThis.litNonce = 'causeway-analysis';

document.addEventListener('securitypolicyviolation', event => {
  events.push({
    blockedURI: event.blockedURI,
    columnNumber: event.columnNumber,
    disposition: event.disposition,
    documentURI: event.documentURI,
    effectiveDirective: event.effectiveDirective,
    lineNumber: event.lineNumber,
    originalPolicy: event.originalPolicy,
    referrer: event.referrer,
    sample: event.sample,
    sourceFile: event.sourceFile,
    statusCode: event.statusCode,
    violatedDirective: event.violatedDirective
  });
});

document.createElement = function createElement(name, options) {
  const element = originalCreateElement(name, options);
  if (`${name}`.toLowerCase() === 'style') {
    if (policyMode === 'nonce-patched') element.nonce = 'causeway-analysis';
    styleCreations.push({stack: new Error('style-created').stack});
  }
  return element;
};

new MutationObserver(records => {
  for (const record of records) {
    if (record.type === 'attributes' && record.attributeName === 'style') {
      styleMutations.push({name: record.target.localName, id: record.target.id, style: record.target.getAttribute('style')});
    }
  }
}).observe(document.documentElement, {attributes: true, attributeFilter: ['style'], subtree: true});

const items = Array.from({length: 12}, (_, index) => ({id: `owner-${index + 1}`, label: `Owner ${`${index + 1}`.padStart(3, '0')}`}));
let control;

window.cspHarness = {
  async create(component) {
    await import('/generated/candidate.js');
    const tag = component === 'multi' ? 'vaadin-multi-select-combo-box' : 'vaadin-combo-box';
    await customElements.whenDefined(tag);
    control = originalCreateElement(tag);
    control.label = component === 'multi' ? 'Owners' : 'Owner';
    control.itemLabelPath = 'label';
    control.itemValuePath = 'id';
    control.items = items;
    control.clearButtonVisible = true;
    control.dataset.testid = 'control';
    document.querySelector('#fixture').replaceChildren(control);
    await control.updateComplete;
    await new Promise(resolveFrame => requestAnimationFrame(() => requestAnimationFrame(resolveFrame)));
    document.querySelector('#status').value = 'Ready';
    return this.snapshot();
  },
  async operate(operation) {
    if (operation === 'open') {
      control.focus();
      control.opened = true;
    } else if (operation === 'close') {
      control.opened = true;
      await nextFrame();
      control.opened = false;
    } else if (operation === 'filter') {
      control.focus();
      control.filter = 'Owner 00';
      control.opened = true;
    } else if (operation === 'select') {
      if (control.localName.includes('multi-select')) control.selectedItems = [items[1], items[2]];
      else control.selectedItem = items[1];
    } else if (operation === 'clear') {
      if (control.localName.includes('multi-select')) {
        control.selectedItems = [items[1], items[2]];
        await nextFrame();
        control.selectedItems = [];
      } else {
        control.selectedItem = items[1];
        await nextFrame();
        control.selectedItem = null;
      }
    } else if (operation === 'validation') {
      control.required = true;
      control.validate();
    } else if (operation === 'disabled') {
      control.disabled = true;
    } else if (operation === 'disconnect') {
      control.remove();
    } else if (operation === 'reconnect') {
      control.remove();
      document.querySelector('#fixture').append(control);
    }
    await control.updateComplete;
    await nextFrame();
    return this.snapshot();
  },
  async snapshot() {
    const styles = [];
    for (const [index, style] of [...document.querySelectorAll('style')].entries()) {
      const bytes = new TextEncoder().encode(style.textContent);
      const digest = await crypto.subtle.digest('SHA-256', bytes);
      styles.push({
        hash: `sha256-${btoa(String.fromCharCode(...new Uint8Array(digest)))}`,
        id: style.id,
        nonce: style.nonce,
        parent: style.parentNode?.localName ?? style.parentNode?.nodeName,
        parentHost: style.getRootNode()?.host?.localName ?? null,
        sourceStack: styleCreations[index]?.stack ?? null,
        textLength: style.textContent.length
      });
    }
    return {
      events: structuredClone(events),
      styleCreations: structuredClone(styleCreations),
      styleMutations: structuredClone(styleMutations),
      styles,
      adoptedStyleSheetCount: [...document.querySelectorAll('*')].reduce((count, element) => count + (element.shadowRoot?.adoptedStyleSheets?.length ?? 0), 0),
      activeElement: document.activeElement?.localName,
      controlConnected: control?.isConnected ?? false,
      controlOpened: control?.opened ?? false,
      overlayCount: document.querySelectorAll('vaadin-combo-box-overlay, vaadin-multi-select-combo-box-overlay').length,
      horizontalOverflow: Math.max(0, document.documentElement.scrollWidth - document.documentElement.clientWidth)
    };
  }
};

document.querySelector('#status').value = 'Harness loaded';

function nextFrame() {
  return new Promise(resolveFrame => requestAnimationFrame(() => requestAnimationFrame(resolveFrame)));
}
