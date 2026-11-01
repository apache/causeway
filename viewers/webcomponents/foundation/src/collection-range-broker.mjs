/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

const DEFAULT_MAX_CONCURRENT = 3;
const DEFAULT_MAX_ENTRIES = 6;
const DEFAULT_MAX_ERRORS = 20;

export class CausewayCollectionRangeBroker {
  constructor({
    loadRange,
    hydrate = () => [],
    maximumSize,
    defaultSize,
    maxConcurrent = DEFAULT_MAX_CONCURRENT,
    maxEntries = DEFAULT_MAX_ENTRIES,
    maxRows = null,
    maxErrors = DEFAULT_MAX_ERRORS
  } = {}) {
    if (typeof loadRange !== 'function') throw new TypeError('A collection range broker requires a loadRange function.');
    this.loadRange = loadRange;
    this.hydrate = hydrate;
    this.maximumSize = positiveInteger(maximumSize, 'maximumSize');
    this.defaultSize = positiveInteger(defaultSize, 'defaultSize');
    if (this.defaultSize > this.maximumSize) throw new RangeError('defaultSize cannot exceed maximumSize.');
    this.maxConcurrent = positiveInteger(maxConcurrent, 'maxConcurrent');
    this.maxEntries = positiveInteger(maxEntries, 'maxEntries');
    this.maxRows = positiveInteger(maxRows ?? this.maximumSize * this.maxEntries, 'maxRows');
    this.maxErrors = positiveInteger(maxErrors, 'maxErrors');
    if (this.maxRows < this.maximumSize) throw new RangeError('maxRows cannot be smaller than maximumSize.');
    this.entries = new Map();
    this.generation = 0;
    this.accessSequence = 0;
    this.closed = false;
  }

  seed(result, {ownedContexts = false, contexts = []} = {}) {
    this.#ensureOpen();
    const normalized = this.#normalizeResult(result);
    const {offset, requestedSize} = normalized.window;
    const key = rangeKey(offset, requestedSize);
    this.#retire(this.entries.get(key));
    const entry = this.#entry(key, offset, requestedSize);
    entry.status = 'ready';
    entry.result = normalized;
    entry.contexts = Object.freeze([...(contexts ?? [])]);
    entry.ownedContexts = ownedContexts === true;
    entry.promise = Promise.resolve(normalized);
    this.entries.set(key, entry);
    this.#enforceRetainedBounds(entry);
    return normalized;
  }

  request(offset, size = this.defaultSize) {
    this.#ensureOpen();
    const requestedOffset = nonNegativeInteger(offset, 'offset');
    const requestedSize = positiveInteger(size, 'size');
    if (requestedSize > this.maximumSize) throw new RangeError(`size cannot exceed ${this.maximumSize}.`);
    const key = rangeKey(requestedOffset, requestedSize);
    const retained = this.entries.get(key);
    if (retained && !retained.retired) {
      retained.access = ++this.accessSequence;
      return retained.promise;
    }
    this.#makeCapacityForRequest();
    const generation = this.generation;
    const entry = this.#entry(key, requestedOffset, requestedSize);
    entry.status = 'loading';
    this.entries.set(key, entry);
    entry.promise = Promise.resolve().then(() => this.loadRange({
      offset: requestedOffset,
      size: requestedSize,
      signal: entry.abortController.signal,
      requestKey: entry.requestKey
    })).then(result => {
      if (!this.#isCurrent(entry, generation)) throw obsoleteRangeError();
      const normalized = this.#normalizeResult(result, requestedOffset, requestedSize);
      const contexts = this.hydrate(normalized) ?? [];
      if (!this.#isCurrent(entry, generation)) {
        disconnectContexts(contexts);
        throw obsoleteRangeError();
      }
      entry.status = 'ready';
      entry.result = normalized;
      entry.contexts = Object.freeze([...contexts]);
      entry.ownedContexts = true;
      this.#enforceRetainedBounds(entry);
      return normalized;
    }).catch(error => {
      if (this.entries.get(key) === entry) this.entries.delete(key);
      this.#retire(entry);
      if (entry.abortController.signal.aborted || generation !== this.generation || this.closed) {
        throw obsoleteRangeError();
      }
      throw error;
    });
    return entry.promise;
  }

  invalidate() {
    if (this.closed) return false;
    this.generation += 1;
    for (const entry of [...this.entries.values()]) this.#retire(entry);
    this.entries.clear();
    return true;
  }

  disconnect() {
    if (this.closed) return false;
    this.invalidate();
    this.closed = true;
    return true;
  }

  snapshot() {
    const entries = [...this.entries.values()];
    return Object.freeze({
      generation: this.generation,
      closed: this.closed,
      entries: entries.length,
      loading: entries.filter(entry => entry.status === 'loading').length,
      rows: entries.reduce((total, entry) => total + (entry.result?.rows?.length ?? 0), 0)
    });
  }

  #entry(key, offset, size) {
    return {
      key,
      offset,
      size,
      access: ++this.accessSequence,
      status: 'new',
      result: null,
      contexts: Object.freeze([]),
      ownedContexts: false,
      abortController: new AbortController(),
      requestKey: Object.freeze({broker: this, key}),
      promise: null,
      retired: false
    };
  }

  #makeCapacityForRequest() {
    while (this.entries.size >= this.maxEntries || this.#loadingCount() >= this.maxConcurrent) {
      const victim = [...this.entries.values()].sort((left, right) => left.access - right.access)[0];
      if (!victim) break;
      this.entries.delete(victim.key);
      this.#retire(victim);
    }
  }

  #enforceRetainedBounds(current) {
    while (this.entries.size > this.maxEntries || this.#retainedRowCount() > this.maxRows) {
      const victim = [...this.entries.values()]
        .filter(entry => entry !== current)
        .sort((left, right) => left.access - right.access)[0];
      if (!victim) throw new RangeError('One collection range exceeds the retained row bound.');
      this.entries.delete(victim.key);
      this.#retire(victim);
    }
  }

  #normalizeResult(result, expectedOffset = null, expectedSize = null) {
    const window = result?.window;
    if (!window) throw new TypeError('A collection range result requires normalized window metadata.');
    const offset = nonNegativeInteger(window.offset, 'result offset');
    const requestedSize = positiveInteger(window.requestedSize, 'result requestedSize');
    const returnedCount = nonNegativeInteger(window.returnedCount, 'result returnedCount');
    const maximumSize = positiveInteger(window.maximumSize, 'result maximumSize');
    const rows = Array.isArray(result?.rows) ? result.rows : [];
    if (expectedOffset != null && offset !== expectedOffset) throw new RangeError('Collection range offset did not match its request.');
    if (expectedSize != null && requestedSize !== expectedSize) throw new RangeError('Collection range size did not match its request.');
    if (requestedSize > this.maximumSize || requestedSize > maximumSize) throw new RangeError('Collection range exceeded its maximum size.');
    if (returnedCount !== rows.length || returnedCount > requestedSize) {
      throw new RangeError('Collection range row count did not match normalized metadata.');
    }
    const errors = Object.freeze([...(Array.isArray(result?.errors) ? result.errors : [])].slice(0, this.maxErrors));
    return Object.freeze({...result, rows: Object.freeze([...rows]), errors});
  }

  #retire(entry) {
    if (!entry || entry.retired) return false;
    entry.retired = true;
    entry.abortController.abort();
    if (entry.ownedContexts) disconnectContexts(entry.contexts);
    entry.contexts = Object.freeze([]);
    return true;
  }

  #isCurrent(entry, generation) {
    return !this.closed
      && generation === this.generation
      && !entry.retired
      && this.entries.get(entry.key) === entry;
  }

  #loadingCount() {
    return [...this.entries.values()].filter(entry => entry.status === 'loading').length;
  }

  #retainedRowCount() {
    return [...this.entries.values()].reduce((total, entry) => total + (entry.result?.rows?.length ?? 0), 0);
  }

  #ensureOpen() {
    if (this.closed) throw new Error('Collection range broker is disconnected.');
  }
}

function disconnectContexts(contexts) {
  for (const context of contexts ?? []) context?.disconnect?.();
}

function rangeKey(offset, size) {
  return `${offset}:${size}`;
}

function nonNegativeInteger(value, label) {
  if (!Number.isSafeInteger(value) || value < 0) throw new RangeError(`${label} must be a non-negative safe integer.`);
  return value;
}

function positiveInteger(value, label) {
  if (!Number.isSafeInteger(value) || value < 1) throw new RangeError(`${label} must be a positive safe integer.`);
  return value;
}

function obsoleteRangeError() {
  const error = new Error('Collection range response was superseded.');
  error.name = 'AbortError';
  return error;
}
