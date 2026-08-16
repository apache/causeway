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
  constructor({
    client,
    logicalTypeName,
    objectId,
    hydration = null,
    schedule = callback => globalThis.queueMicrotask(callback)
  } = {}) {
    super();
    this.client = client ?? null;
    this.identity = Object.freeze({logicalTypeName: logicalTypeName ?? '', id: objectId ?? ''});
    this.schedule = callback => schedule(callback);
    this.description = hydration?.description ?? null;
    this.snapshot = hydration?.data
      ? deepFreeze({
        data: hydration.data,
        selection: hydration.selection ?? {},
        errors: hydration.errors ?? [],
        generation: hydration.generation ?? 0
      })
      : null;
    this.secondaryCache = new Map();
    this.secondaryAbortControllers = new Set();
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

  async loadCollection({member, columns = [], force = false, signal} = {}) {
    if (this.closed) {
      throw new Error('Cannot load a collection from a disconnected object context.');
    }
    const description = this.description ?? await this.client.describeObject(this.identity.logicalTypeName);
    this.description ??= description;
    const descriptor = description.members.get(member);
    if (!descriptor || descriptor.kind !== 'collection' || !descriptor.fields.has('get')) {
      throw new Error(`Collection '${member}' is not readable on '${description.logicalTypeName}'.`);
    }
    const rowSelection = collectionRowSelection(columns);
    const selection = {[member]: {get: rowSelection}};
    const cacheKey = JSON.stringify({member, rowSelection});
    if (!force && this.secondaryCache.has(cacheKey)) {
      return this.secondaryCache.get(cacheKey);
    }
    const abortController = new AbortController();
    this.secondaryAbortControllers.add(abortController);
    const abort = () => abortController.abort();
    signal?.addEventListener?.('abort', abort, {once: true});
    const promise = this.client.readObject({
      description,
      identity: this.identity,
      selection,
      signal: abortController.signal
    }).then(async result => {
      const data = result.data?.[member] ?? null;
      const firstObjectRow = data?.get?.find?.(row => row?._meta?.logicalTypeName);
      let rowDescription = null;
      let errors = result.errors;
      if (firstObjectRow) {
        try {
          rowDescription = await this.client.describeObject(firstObjectRow._meta.logicalTypeName);
        } catch (error) {
          errors = Object.freeze([...errors, asGraphQLError(error)]);
        }
      }
      return Object.freeze({
        descriptor,
        data,
        errors,
        rowDescription,
        rowSelection,
        selection
      });
    }).finally(() => {
      signal?.removeEventListener?.('abort', abort);
      this.secondaryAbortControllers.delete(abortController);
    });
    this.secondaryCache.set(cacheKey, promise);
    promise.catch(() => this.secondaryCache.delete(cacheKey));
    return promise;
  }

  createHydratedRowContext(row, rowSelection = {}) {
    const metadata = row?._meta;
    if (!metadata?.logicalTypeName || !metadata?.id) {
      throw new Error('A hydrated row requires _meta.logicalTypeName and _meta.id.');
    }
    return new ObjectContextController({
      client: this.client,
      logicalTypeName: metadata.logicalTypeName,
      objectId: metadata.id,
      hydration: {data: row, selection: rowSelection}
    });
  }

  subscribe(listener) {
    this.stateListeners.add(listener);
    listener(this.state);
    return () => this.stateListeners.delete(listener);
  }

  refresh() {
    this.forceFull = true;
    this.secondaryCache.clear();
    for (const abortController of this.secondaryAbortControllers) {
      abortController.abort();
    }
    this.#scheduleFlush();
  }

  invalidate() {
    this.refresh();
  }

  disconnect() {
    this.closed = true;
    this.abortController?.abort();
    for (const abortController of this.secondaryAbortControllers) {
      abortController.abort();
    }
    this.secondaryAbortControllers.clear();
    this.secondaryCache.clear();
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
      if (this.snapshot) {
        this.#setState({
          status: this.snapshot.errors.length > 0 ? ObjectContextStatus.PARTIAL_ERROR : ObjectContextStatus.READY,
          generation: this.snapshot.generation,
          snapshot: this.snapshot,
          errors: this.snapshot.errors,
          error: null
        });
      }
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
  if (['property', 'action', 'collection'].includes(requirement?.kind)
      && typeof requirement.member === 'string'
      && requirement.member.length > 0) {
    return Object.freeze({kind: requirement.kind, member: requirement.member});
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
  if (!member || member.kind !== requirement.kind) {
    const label = requirement.kind[0].toUpperCase() + requirement.kind.slice(1);
    throw new Error(`${label} '${requirement.member}' is not present on '${description.logicalTypeName}'.`);
  }
  if (requirement.kind === 'property') {
    if (!member.fields.has('get')) {
      throw new Error(`Property '${requirement.member}' does not expose a readable value.`);
    }
    const memberSelection = Object.fromEntries(
      ['hidden', 'disabled', 'datatype']
        .filter(field => member.fields.has(field))
        .map(field => [field, true])
    );
    memberSelection.get = propertyValueSelection(member);
    return {descriptor: member, selection: {[member.id]: memberSelection}};
  }
  if (requirement.kind === 'collection' && !member.fields.has('get')) {
    throw new Error(`Collection '${requirement.member}' does not expose readable contents.`);
  }
  const supportedFields = ['hidden', 'disabled'].filter(field => member.fields.has(field));
  return {
    descriptor: member,
    selection: {[member.id]: Object.fromEntries(supportedFields.map(field => [field, true]))}
  };
}

function propertyValueSelection(member) {
  const value = member.value;
  if (!value || value.typeKind === 'SCALAR' || value.typeKind === 'ENUM') {
    return true;
  }
  const typeFields = value.typeDescription?.fields ?? [];
  const lobFields = typeFields
    .filter(field => ['name', 'mimeType', 'bytes', 'chars'].includes(field.name))
    .map(field => field.name);
  if (lobFields.length > 0) {
    return Object.fromEntries(lobFields.map(field => [field, true]));
  }
  const scalarFields = typeFields
    .filter(field => ['SCALAR', 'ENUM'].includes(innermostType(field.type)?.kind))
    .map(field => field.name);
  if (scalarFields.length > 0) {
    return Object.fromEntries(scalarFields.map(field => [field, true]));
  }
  if (value.typeDescription) {
    return {__typename: true};
  }
  return {_meta: {id: true, logicalTypeName: true, title: true, version: true}};
}

function collectionRowSelection(columns) {
  const selection = {_meta: {id: true, logicalTypeName: true, title: true, version: true}};
  for (const column of columns) {
    const member = typeof column === 'string' ? column : column?.member;
    if (!member) {
      continue;
    }
    selection[member] = {hidden: true, disabled: true, datatype: true, get: true};
  }
  return selection;
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) {
    current = current.ofType;
  }
  return current;
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
