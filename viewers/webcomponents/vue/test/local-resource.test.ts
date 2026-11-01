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

import {describe, expect, it, vi} from 'vitest';
import {navigateLocalResource, resolveLocalResourceTarget} from '../src/local-resource';

const target = (path = '/guide', openUrlStrategy: 'SAME_WINDOW' | 'NEW_WINDOW' = 'SAME_WINDOW') => ({path, openUrlStrategy});
const location = (href = 'https://example.test/vue/object/example.Type/1') => ({href, assign: vi.fn()});

describe('local-resource navigation', () => {
  it('resolves root and nested deployment bases exactly once', () => {
    expect(resolveLocalResourceTarget(target(), {location: location(), applicationBase: '/'}).url.href).toBe('https://example.test/guide');
    expect(resolveLocalResourceTarget(target('/logout'), {location: location(), applicationBase: '/'}).url.href).toBe('https://example.test/logout');
    expect(resolveLocalResourceTarget(target(), {location: location(), applicationBase: '/app'}).url.href).toBe('https://example.test/app/guide');
    expect(resolveLocalResourceTarget(target('/app/guide'), {location: location(), applicationBase: '/app'}).url.href).toBe('https://example.test/app/guide');
  });

  it('rejects malformed, escaping, and cross-origin targets', () => {
    for (const path of ['https://evil.test/x', '//evil.test/x', 'javascript:alert(1)', '../secret', '/app/../secret', '..\\secret']) {
      expect(() => resolveLocalResourceTarget(target(path), {location: location(), applicationBase: '/app'})).toThrow();
    }
    expect(() => resolveLocalResourceTarget(target('/guide'), {
      location: location(), applicationBase: 'https://evil.test/app'
    })).toThrow(/same-origin/);
  });

  it('uses document navigation and opener isolation without touching a router', () => {
    const current = location();
    navigateLocalResource(target('/guide'), {location: current, applicationBase: '/app'});
    expect(current.assign).toHaveBeenCalledWith('https://example.test/app/guide');

    const opened = {opener: {} as unknown};
    const open = vi.fn(() => opened as unknown as Window);
    navigateLocalResource(target('/guide', 'NEW_WINDOW'), {location: current, applicationBase: '/app', open});
    expect(open).toHaveBeenCalledWith('https://example.test/app/guide', '_blank', 'noopener,noreferrer');
    expect(opened.opener).toBeNull();
    expect(() => navigateLocalResource(target('/guide', 'NEW_WINDOW'), {
      location: current,
      applicationBase: '/app',
      open: () => null
    })).not.toThrow();
  });
});
