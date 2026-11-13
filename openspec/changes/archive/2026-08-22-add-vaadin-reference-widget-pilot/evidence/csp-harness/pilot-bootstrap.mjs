/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

const referenceModule = await import('/foundation/reference-widget.mjs');
referenceModule.configureCausewayReferenceWidgets({
  enabled: true,
  minimumSearchLength: 2,
  maximumResults: 50,
  moduleUrl: '/production/vaadin-reference.js'
});
const {CausewayPropertyElement} = await import('/foundation/index.mjs');

const references = Array.from({length: 12}, (_, index) => ({
  _meta: {
    id: `owner-${index + 1}`,
    logicalTypeName: 'petclinic.Owner',
    title: `Owner ${`${index + 1}`.padStart(3, '0')}`
  }
}));
const instances = new Map();
const semanticEvents = [];
document.querySelector('[data-testid="pilot-page"]').addEventListener('causeway-property-interaction-state-change', event => {
  semanticEvents.push({member: event.detail.member, status: event.detail.status, value: event.detail.value});
});

window.pilot = {
  ready: true,
  async create({id, multiple = false, autocomplete = false, custom = false, required = false, disabledControl = false} = {}) {
    const type = {kind: 'OBJECT', name: 'petclinic_Owner', ofType: null};
    const inputType = multiple ? {kind: 'LIST', name: null, ofType: type} : required ? {kind: 'NON_NULL', name: null, ofType: type} : type;
    let stateListener;
    const calls = [];
    const context = {
      identity: {logicalTypeName: 'petclinic.Pet', id: 'pet-1'},
      registerRequirement(requirement, listener) {
        stateListener = listener;
        return () => calls.push('released');
      },
      async prepareProperty() {
        calls.push('prepare');
        return {
          status: 'success',
          data: {
            capabilities: {autoComplete: autocomplete, validate: true, inputType},
            choices: autocomplete ? [] : references
          },
          errors: []
        };
      },
      async autoCompleteProperty(member, search, {signal}) {
        calls.push(`search:${search}`);
        await abortableDelay(search === 'Owner 0' ? 80 : 20, signal);
        return {
          status: 'success',
          data: references.filter(reference => reference._meta.title.toLowerCase().includes(search.toLowerCase())),
          errors: []
        };
      },
      async validateProperty(member, value) {
        calls.push('validate');
        const empty = multiple ? !Array.isArray(value) || value.length === 0 : value == null;
        return {status: 'success', data: required && empty ? 'Select at least one owner.' : null, errors: []};
      },
      async updateProperty(member, value) {
        calls.push('update');
        return {status: 'success', data: {_meta: {id: 'pet-1'}}, errors: []};
      }
    };
    const property = new CausewayPropertyElement();
    property.member = id;
    property.editable = true;
    property.setAttribute('label', multiple ? 'Owners' : 'Owner');
    property.setAttribute('data-testid', id);
    property.context = context;
    document.querySelector(custom ? '#custom-editors' : '#generic-editors').append(property);
    stateListener({
      status: 'ready',
      descriptor: {id, description: multiple ? 'Selected owners' : 'Selected owner'},
      data: {hidden: false, disabled: null, get: multiple ? [references[0]] : references[0]},
      errors: [],
      generation: 1
    });
    await property.beginEdit();
    if (disabledControl) {
      property.interactionState = Object.freeze({...property.interactionState, status: 'saving'});
      property.renderComponentState(property.componentState);
    }
    instances.set(id, {property, context, calls});
    return this.snapshot();
  },
  clear(id) {
    const property = instances.get(id)?.property;
    property?.setPendingValue(property.interactionState?.capabilities?.inputType?.kind === 'LIST' ? [] : null);
    return this.snapshot();
  },
  async validate(id) {
    await instances.get(id)?.property.validatePending();
    return this.snapshot();
  },
  remove(id) {
    instances.get(id)?.property.remove();
    instances.delete(id);
    return this.snapshot();
  },
  snapshot() {
    const editors = {};
    for (const [id, instance] of instances) {
      editors[id] = {
        connected: instance.property.isConnected,
        editor: instance.property.getAttribute('data-editor'),
        status: instance.property.interactionState?.status,
        pendingValue: instance.property.interactionState?.pendingValue,
        error: instance.property.interactionState?.error,
        calls: [...instance.calls],
        widgetState: instance.property.querySelector('causeway-reference-editor')?.dataset.widgetState,
        control: instance.property.querySelector('vaadin-combo-box, vaadin-multi-select-combo-box')?.localName,
        controlDisabled: instance.property.querySelector('vaadin-combo-box, vaadin-multi-select-combo-box')?.disabled ?? null
      };
    }
    return {
      editors,
      semanticEvents: structuredClone(semanticEvents),
      overlayCount: document.querySelectorAll('vaadin-combo-box-overlay, vaadin-multi-select-combo-box-overlay').length,
      horizontalOverflow: Math.max(0, document.documentElement.scrollWidth - document.documentElement.clientWidth),
      vaadinDefined: ['vaadin-combo-box', 'vaadin-multi-select-combo-box'].every(name => Boolean(customElements.get(name))),
      flowRuntime: Boolean(window.Vaadin?.Flow?.clients)
    };
  }
};

document.querySelector('#status').value = 'Ready';

function abortableDelay(milliseconds, signal) {
  return new Promise((resolve, reject) => {
    const timeout = setTimeout(resolve, milliseconds);
    signal.addEventListener('abort', () => {
      clearTimeout(timeout);
      resolve();
    }, {once: true});
  });
}
