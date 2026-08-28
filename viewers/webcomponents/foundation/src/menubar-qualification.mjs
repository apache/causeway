/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

export const MENUBAR_NARROW_WIDTH_PX = 768;

export function qualifyCausewayMenuBar(candidate = {}) {
  const base = Object.freeze({
    role: String(candidate.role ?? ''),
    generation: Number.isSafeInteger(candidate.generation) ? candidate.generation : 0
  });
  if (candidate.policy !== 'vaadin') return native(base, 'policy-native');
  if (candidate.familyAvailable !== true) return native(base, 'family-failed');
  if (candidate.connected !== true) return native(base, 'disconnected');
  if (candidate.visible !== true) return native(base, 'hidden');
  if (candidate.current !== true) return native(base, 'stale-generation');
  if (!candidate.projection?.accepted) return native(base, candidate.projection?.reason ?? 'hierarchy-unsupported');
  const width = Number(candidate.width);
  if (!Number.isFinite(width) || width <= 0) return native(base, 'width-unavailable');
  return Object.freeze({
    ...base,
    accepted: true,
    presentation: width <= MENUBAR_NARROW_WIDTH_PX ? 'vaadin-overflow' : 'vaadin-wide',
    reason: null,
    width
  });
}

function native(base, reason) {
  return Object.freeze({...base, accepted: false, presentation: 'native', reason});
}
