/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';

import {CausewayCollectionRangeBroker} from '../src/collection-range-broker.mjs';

function rangeResult(offset, size, count = size, overrides = {}) {
  const rows = Array.from({length: count}, (_, index) => ({id: `${offset + index}`}));
  return {
    rows,
    errors: [],
    window: {
      offset,
      requestedSize: size,
      returnedCount: count,
      totalCount: 100,
      maximumSize: 20,
      hasPrevious: offset > 0,
      hasNext: offset + count < 100,
      previousOffset: offset > 0 ? Math.max(0, offset - size) : null,
      nextOffset: offset + count < 100 ? offset + count : null,
      ordering: 'CONFIGURED'
    },
    ...overrides
  };
}

function deferred() {
  let resolve;
  let reject;
  const promise = new Promise((accept, decline) => {
    resolve = accept;
    reject = decline;
  });
  return {promise, resolve, reject};
}

function broker(options = {}) {
  return new CausewayCollectionRangeBroker({
    maximumSize: 20,
    defaultSize: 10,
    loadRange: async ({offset, size}) => rangeResult(offset, size),
    ...options
  });
}

test('range inputs and retained bounds are validated before loading', async () => {
  const subject = broker();
  assert.throws(() => subject.request(-1, 10), /non-negative/);
  assert.throws(() => subject.request(0, 0), /positive/);
  assert.throws(() => subject.request(0, 21), /cannot exceed/);
  assert.throws(() => broker({defaultSize: 21}), /cannot exceed/);
  assert.throws(() => broker({maxRows: 19}), /cannot be smaller/);
  assert.deepEqual(subject.snapshot(), {generation: 0, closed: false, entries: 0, loading: 0, rows: 0});
});

test('authoritative initial range seeds the cache without a duplicate request', async () => {
  let loads = 0;
  const subject = broker({loadRange: async () => { loads += 1; }});
  const initial = subject.seed(rangeResult(0, 10));
  const repeated = await subject.request(0, 10);
  assert.equal(repeated, initial);
  assert.equal(loads, 0);
  assert.deepEqual(subject.snapshot(), {generation: 0, closed: false, entries: 1, loading: 0, rows: 10});
});

test('identical in-flight ranges deduplicate and overlapping ranges remain independent', async () => {
  const pending = new Map();
  const calls = [];
  const subject = broker({loadRange: request => {
    calls.push(request);
    const work = deferred();
    pending.set(request.offset, work);
    return work.promise;
  }});
  const first = subject.request(0, 10);
  const duplicate = subject.request(0, 10);
  const overlap = subject.request(5, 10);
  await Promise.resolve();
  assert.equal(first, duplicate);
  assert.equal(calls.length, 2);
  assert.notEqual(calls[0].requestKey, calls[1].requestKey);
  assert.equal(calls.every(call => call.signal.aborted === false), true);
  pending.get(5).resolve(rangeResult(5, 10));
  pending.get(0).resolve(rangeResult(0, 10));
  assert.equal((await first).window.offset, 0);
  assert.equal((await overlap).window.offset, 5);
});

test('concurrent work is capped and least-current obsolete callbacks are aborted', async () => {
  const calls = [];
  const subject = broker({
    maxConcurrent: 2,
    maxEntries: 4,
    loadRange: request => {
      calls.push(request);
      return new Promise((resolve, reject) => request.signal.addEventListener('abort', () => {
        const error = new Error('cancelled');
        error.name = 'AbortError';
        reject(error);
      }, {once: true}));
    }
  });
  const first = subject.request(0, 10);
  subject.request(10, 10).catch(() => {});
  await Promise.resolve();
  subject.request(20, 10).catch(() => {});
  await Promise.resolve();
  assert.equal(calls.length, 3);
  assert.equal(calls[0].signal.aborted, true);
  await assert.rejects(first, {name: 'AbortError'});
  subject.disconnect();
});

test('cache and row bounds evict least-current owned contexts exactly once', async () => {
  const disconnects = new Map();
  const subject = broker({
    maxEntries: 2,
    maxRows: 20,
    hydrate(result) {
      return result.rows.map(row => ({disconnect() {
        disconnects.set(row.id, (disconnects.get(row.id) ?? 0) + 1);
      }}));
    }
  });
  await subject.request(0, 10);
  await subject.request(10, 10);
  await subject.request(20, 10);
  assert.equal(subject.snapshot().entries, 2);
  for (let index = 0; index < 10; index += 1) assert.equal(disconnects.get(`${index}`), 1);
  subject.disconnect();
  for (const count of disconnects.values()) assert.equal(count, 1);
});

test('partial errors stay range-specific and are bounded with their rows', async () => {
  const errors = Array.from({length: 8}, (_, index) => ({message: `bounded-${index}`, path: [index]}));
  const subject = broker({
    maxErrors: 3,
    loadRange: async ({offset, size}) => rangeResult(offset, size, 2, {errors})
  });
  const result = await subject.request(40, 10);
  assert.equal(result.errors.length, 3);
  assert.equal(result.errors[0], errors[0]);
  assert.equal(result.rows.length, 2);
  assert.equal(Object.isFrozen(result.errors), true);
  assert.equal(Object.isFrozen(result.rows), true);
});

test('invalidation ignores stale results and never hydrates obsolete rows', async () => {
  const work = deferred();
  let hydrated = 0;
  const subject = broker({
    loadRange: () => work.promise,
    hydrate() {
      hydrated += 1;
      return [];
    }
  });
  const request = subject.request(0, 10);
  await Promise.resolve();
  subject.invalidate();
  work.resolve(rangeResult(0, 10));
  await assert.rejects(request, {name: 'AbortError'});
  assert.equal(hydrated, 0);
  assert.deepEqual(subject.snapshot(), {generation: 1, closed: false, entries: 0, loading: 0, rows: 0});
});

test('disconnect is idempotent and rejects later work', () => {
  const subject = broker();
  assert.equal(subject.disconnect(), true);
  assert.equal(subject.disconnect(), false);
  assert.throws(() => subject.request(0, 10), /disconnected/);
  assert.throws(() => subject.seed(rangeResult(0, 10)), /disconnected/);
});
