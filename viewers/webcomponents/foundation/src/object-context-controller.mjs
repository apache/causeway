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

import {createSemanticEvent, OBJECT_CONTEXT_STATE_EVENT} from './context-events.mjs';
import {deepMerge, differenceSelection, isSelectionEmpty, mergeSelections} from './selection.mjs';
import {ObjectContextStatus, RequirementStatus} from './types.mjs';

export class ObjectContextController extends EventTarget {
  constructor({client, logicalTypeName, objectId, schedule = callback => globalThis.queueMicrotask(callback)} = {}) {
    super();
    this.client = client ?? null;
    this.identity = Object.freeze({logicalTypeName: logicalTypeName ?? '', id: objectId ?? ''});
    this.schedule = callback => schedule(callback);
    this.description = null;
    this.snapshot = null;
    this.registrations = new Map();
    this.stateListeners = new Set();
    this.scheduled = false;
    this.revision = 0;
    this.generation = 0;
    this.forceFull = false;
    this.abortController = null;
    this.closed = false;
    this.state = freezeState({
      status: ObjectContextStatus.IDLE,
      generation: 0,
      snapshot: null,
      errors: [],
      error: null
    });
    const identityError = validateIdentity(this.identity, this.client);
    if (identityError) {
      this.#setState({
        status: ObjectContextStatus.TERMINAL_ERROR,
        error: identityError,
        errors: [asGraphQLError(identityError)]
      });
    }
  }

  registerRequirement(requirement, listener = () => {}) {
    if (this.closed) {
      throw new Error('Cannot register a requirement on a disconnected object context.');
    }
    const normalized = normalizeRequirement(requirement);
    const token = Symbol(`${normalized.kind}:${normalized.member ?? ''}`);
    this.registrations.set(token, {requirement: normalized, listener, error: null, descriptor: null});
    this.#notifyRegistration(this.registrations.get(token));
    this.#scheduleFlush();
    let released = false;
    return () => {
      if (!released) {
        released = true;
        this.registrations.delete(token);
        this.#scheduleFlush();
      }
    };
  }

  subscribe(listener) {
    this.stateListeners.add(listener);
    listener(this.state);
    return () => this.stateListeners.delete(listener);
  }

  refresh() {
    this.forceFull = true;
    this.#scheduleFlush();
  }

  invalidate() {
    this.refresh();
  }

  disconnect() {
    this.closed = true;
    this.abortController?.abort();
    this.registrations.clear();
    this.stateListeners.clear();
  }

  #scheduleFlush() {
    if (this.closed || this.state.status === ObjectContextStatus.TERMINAL_ERROR && !this.description) {
      return;
    }
    this.revision += 1;
    if (this.scheduled) {
      return;
    }
    this.scheduled = true;
    this.schedule(() => {
      this.scheduled = false;
      void this.#flush(this.revision);
    });
  }

  async #flush(revision) {
    if (this.closed || this.registrations.size === 0) {
      return;
    }
    if (!this.description) {
      this.#setState({status: ObjectContextStatus.SCHEMA_LOADING, error: null, errors: []});
      try {
        const description = await this.client.describeObject(this.identity.logicalTypeName);
        if (this.closed || revision !== this.revision) {
          return;
        }
        this.description = description;
      } catch (error) {
        if (revision === this.revision) {
          this.#setState({
            status: ObjectContextStatus.TERMINAL_ERROR,
            error,
            errors: [asGraphQLError(error)]
          });
          this.#notifyAll();
        }
        return;
      }
    }

    const selections = [];
    for (const registration of this.registrations.values()) {
      try {
        const translated = translateRequirement(registration.requirement, this.description);
        registration.error = null;
        registration.descriptor = translated.descriptor;
        selections.push(translated.selection);
      } catch (error) {
        registration.error = error;
        registration.descriptor = null;
      }
    }
    const activeSelection = mergeSelections(...selections);
    if (isSelectionEmpty(activeSelection)) {
      this.#notifyAll();
      return;
    }

    const cachedSelection = this.snapshot?.selection ?? {};
    const requestedSelection = this.forceFull
      ? activeSelection
      : differenceSelection(activeSelection, cachedSelection);
    if (isSelectionEmpty(requestedSelection)) {
      this.forceFull = false;
      this.#notifyAll();
      return;
    }

    this.forceFull = false;
    const generation = ++this.generation;
    this.abortController?.abort();
    this.abortController = new AbortController();
    this.#setState({status: ObjectContextStatus.OBJECT_LOADING, generation, error: null});
    this.#notifyAll();

    try {
      const result = await this.client.readObject({
        description: this.description,
        identity: this.identity,
        selection: requestedSelection,
        signal: this.abortController.signal
      });
      if (this.closed || generation !== this.generation) {
        return;
      }
      if (result.data === null) {
        const error = new Error(result.errors[0]?.message ?? `Object '${this.identity.logicalTypeName}:${this.identity.id}' was not found.`);
        this.#setState({
          status: ObjectContextStatus.TERMINAL_ERROR,
          generation,
          error,
          errors: result.errors.length ? result.errors : [asGraphQLError(error)]
        });
        this.#notifyAll();
        return;
      }
      const previousData = this.snapshot?.data ?? {};
      const previousSelection = this.snapshot?.selection ?? {};
      const errors = mergeErrors(this.snapshot?.errors ?? [], result.errors, requestedSelection);
      this.snapshot = deepFreeze({
        data: deepMerge(previousData, result.data),
        selection: mergeSelections(previousSelection, requestedSelection),
        errors,
        generation
      });
      this.#setState({
        status: errors.length > 0 ? ObjectContextStatus.PARTIAL_ERROR : ObjectContextStatus.READY,
        generation,
        snapshot: this.snapshot,
        errors,
        error: null
      });
      this.#notifyAll();
    } catch (error) {
      if (error?.name === 'AbortError' || generation !== this.generation || this.closed) {
        return;
      }
      const errors = [asGraphQLError(error)];
      if (this.snapshot) {
        this.snapshot = deepFreeze({...this.snapshot, errors, generation});
        this.#setState({
          status: ObjectContextStatus.PARTIAL_ERROR,
          generation,
          snapshot: this.snapshot,
          errors,
          error
        });
      } else {
        this.#setState({
          status: ObjectContextStatus.TERMINAL_ERROR,
          generation,
          errors,
          error
        });
      }
      this.#notifyAll();
    }
  }

  #setState(changes) {
    this.state = freezeState({
      status: changes.status ?? this.state.status,
      generation: changes.generation ?? this.generation,
      snapshot: changes.snapshot === undefined ? this.snapshot : changes.snapshot,
      errors: changes.errors ?? this.state.errors,
      error: changes.error === undefined ? this.state.error : changes.error
    });
    for (const listener of this.stateListeners) {
      listener(this.state);
    }
    this.dispatchEvent(createSemanticEvent(OBJECT_CONTEXT_STATE_EVENT, {state: this.state}, {bubbles: false, composed: false}));
  }

  #notifyAll() {
    for (const registration of this.registrations.values()) {
      this.#notifyRegistration(registration);
    }
  }

  #notifyRegistration(registration) {
    const requirement = registration.requirement;
    if (registration.error) {
      registration.listener(freezeRequirementState({
        status: RequirementStatus.UNSUPPORTED,
        requirement,
        descriptor: null,
        data: null,
        errors: [asGraphQLError(registration.error)],
        generation: this.generation
      }));
      return;
    }
    const mappedStatus = mapRequirementStatus(this.state.status);
    const pathHead = requirement.kind === 'header'
      ? this.description?.metadata?.id
      : requirement.member;
    const errors = this.snapshot?.errors.filter(error => error.path?.[0] === pathHead) ?? [];
    const data = this.snapshot && pathHead ? this.snapshot.data[pathHead] : null;
    registration.listener(freezeRequirementState({
      status: errors.length > 0 && mappedStatus === RequirementStatus.READY
        ? RequirementStatus.PARTIAL_ERROR
        : mappedStatus,
      requirement,
      descriptor: registration.descriptor,
      data,
      errors,
      generation: this.generation
    }));
  }
}

function validateIdentity(identity, client) {
  if (!client) {
    return new Error('A Causeway GraphQL client is required.');
  }
  if (!identity.logicalTypeName) {
    return new Error('The logical-type attribute is required.');
  }
  if (!identity.id) {
    return new Error('The object-id attribute is required.');
  }
  return null;
}

function normalizeRequirement(requirement) {
  if (requirement?.kind === 'header') {
    return Object.freeze({kind: 'header'});
  }
  if (requirement?.kind === 'property' && typeof requirement.member === 'string' && requirement.member.length > 0) {
    return Object.freeze({kind: 'property', member: requirement.member});
  }
  throw new Error(`Unsupported semantic read requirement '${JSON.stringify(requirement)}'.`);
}

function translateRequirement(requirement, description) {
  if (requirement.kind === 'header') {
    const metadata = description.metadata;
    if (!metadata) {
      throw new Error(`Type '${description.generatedTypeName}' does not expose rich object metadata.`);
    }
    const supportedFields = ['id', 'logicalTypeName', 'title', 'version']
      .filter(field => metadata.fields.has(field));
    if (!supportedFields.includes('id') || !supportedFields.includes('logicalTypeName')) {
      throw new Error(`Metadata type '${metadata.generatedTypeName}' lacks object identity fields.`);
    }
    return {
      descriptor: metadata,
      selection: {[metadata.id]: Object.fromEntries(supportedFields.map(field => [field, true]))}
    };
  }
  const member = description.members.get(requirement.member);
  if (!member || member.kind !== 'property') {
    throw new Error(`Property '${requirement.member}' is not present on '${description.logicalTypeName}'.`);
  }
  const supportedFields = ['hidden', 'disabled', 'get'].filter(field => member.fields.has(field));
  if (!supportedFields.includes('get')) {
    throw new Error(`Property '${requirement.member}' does not expose a readable value.`);
  }
  return {
    descriptor: member,
    selection: {[member.id]: Object.fromEntries(supportedFields.map(field => [field, true]))}
  };
}

function mapRequirementStatus(objectStatus) {
  return {
    [ObjectContextStatus.IDLE]: RequirementStatus.IDLE,
    [ObjectContextStatus.SCHEMA_LOADING]: RequirementStatus.SCHEMA_LOADING,
    [ObjectContextStatus.OBJECT_LOADING]: RequirementStatus.OBJECT_LOADING,
    [ObjectContextStatus.READY]: RequirementStatus.READY,
    [ObjectContextStatus.PARTIAL_ERROR]: RequirementStatus.READY,
    [ObjectContextStatus.TERMINAL_ERROR]: RequirementStatus.TERMINAL_ERROR
  }[objectStatus] ?? RequirementStatus.TERMINAL_ERROR;
}

function mergeErrors(previousErrors, nextErrors, requestedSelection) {
  const replacedHeads = new Set(Object.keys(requestedSelection));
  return Object.freeze([
    ...previousErrors.filter(error => !replacedHeads.has(error.path?.[0])),
    ...nextErrors
  ]);
}

function asGraphQLError(error) {
  return Object.freeze({
    message: error?.message ?? String(error),
    path: [],
    extensions: Object.freeze(error?.code ? {code: error.code} : {})
  });
}

function freezeState(state) {
  return Object.freeze({
    status: state.status,
    generation: state.generation,
    snapshot: state.snapshot,
    errors: Object.freeze([...(state.errors ?? [])]),
    error: state.error ?? null
  });
}

function freezeRequirementState(state) {
  return Object.freeze({...state, errors: Object.freeze([...(state.errors ?? [])])});
}

function deepFreeze(value) {
  if (value && typeof value === 'object' && !Object.isFrozen(value)) {
    Object.freeze(value);
    for (const nested of Object.values(value)) {
      deepFreeze(nested);
    }
  }
  return value;
}
