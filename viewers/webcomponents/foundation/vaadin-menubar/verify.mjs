/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {createHash} from 'node:crypto';
import {existsSync, readFileSync, readdirSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {gzipSync} from 'node:zlib';

const directory = fileURLToPath(new URL('.', import.meta.url));
const policy = JSON.parse(readFileSync(resolve(directory, 'policy.json'), 'utf8'));
const lock = JSON.parse(readFileSync(resolve(directory, 'package-lock.json'), 'utf8'));
const bundlePath = resolve(directory, 'generated/assets/vaadin-menubar.js');
const metadataPath = resolve(directory, 'generated/licenses/THIRD-PARTY.json');
const htmxControllerPath = resolve(directory, '../../htmx/src/main/java/org/apache/causeway/viewer/webcomponents/htmx/HtmxViewerController.java');
if (!existsSync(bundlePath) || !existsSync(metadataPath)) throw new Error('Generated Vaadin Menu Bar assets are missing. Run npm ci --ignore-scripts and npm run build.');
const bundle = readFileSync(bundlePath);
const metadata = JSON.parse(readFileSync(metadataPath, 'utf8'));
const sha256 = createHash('sha256').update(bundle).digest('hex');
const gzipBytes = gzipSync(bundle, {level: 9}).length;
if (sha256 !== policy.bundleSha256 || sha256 !== metadata.bundle.sha256) throw new Error(`Bundle checksum mismatch: ${sha256}.`);
if (gzipBytes > policy.maximumGzipBytes) throw new Error(`Bundle is ${gzipBytes} gzip bytes, exceeding ${policy.maximumGzipBytes}.`);
if (JSON.stringify(metadata.cspStyleHashes) !== JSON.stringify(policy.cspStyleHashes)) throw new Error('Packaged CSP style hashes differ from policy.');
if (!existsSync(htmxControllerPath)) throw new Error('The HTMX CSP source is missing.');
const controllerSource = readFileSync(htmxControllerPath, 'utf8');
for (const hash of policy.cspStyleHashes) {
  if (!controllerSource.includes(`\"${hash}\"`)) throw new Error(`HTMX CSP source omits the Menu Bar policy hash ${hash}.`);
}
for (const [name, version] of Object.entries(policy.directPackages)) {
  const entry = lock.packages?.[`node_modules/${name}`];
  if (entry?.version !== version || !entry.integrity) throw new Error(`Pinned package mismatch for ${name}: ${entry?.version ?? 'missing'}.`);
}
if (JSON.stringify(metadata.entryPoints) !== JSON.stringify(['@vaadin/menu-bar'])) {
  throw new Error(`Unexpected Menu Bar entry points: ${JSON.stringify(metadata.entryPoints)}.`);
}
const prohibited = [
  '@vaadin/grid', '@vaadin/grid-pro', '@vaadin/rich-text-editor',
  '@vaadin/charts', '@vaadin/spreadsheet', '@vaadin/crud', '@vaadin/board'
];
for (const name of prohibited) if (lock.packages?.[`node_modules/${name}`]) throw new Error(`Commercial or prohibited package present: ${name}.`);
const text = bundle.toString('utf8');
for (const marker of [
  'vaadin-usage-statistics-collect', 'tools.vaadin.com/usage-stats', 'Vaadin.Flow.clients',
  'vaadin-grid', 'BinderNode', 'window.Vaadin.Flow'
]) {
  if (text.includes(marker)) throw new Error(`Prohibited runtime marker remains in the bundle: ${marker}.`);
}
if (/\b(?:import|export)\s*(?:\([^)]*)?\s*['"]https?:\/\//.test(text)) throw new Error('External runtime module reference found.');
const licenses = readdirSync(resolve(directory, 'generated/licenses'));
for (const pkg of metadata.packages) {
  const prefix = pkg.name.replace(/^@/, '').replaceAll('/', '__') + `-${pkg.version}-`;
  if (!licenses.some(file => file.startsWith(prefix) && /^LICENSE|^LICENCE/i.test(file.slice(prefix.length)))) throw new Error(`Packaged license missing for ${pkg.name}.`);
  if (!pkg.integrity || !pkg.license) throw new Error(`Incomplete package metadata for ${pkg.name}.`);
}
console.log(JSON.stringify({bundleSha256: sha256, rawBytes: bundle.length, gzipBytes, packageCount: metadata.packages.length, cspStyleHashes: policy.cspStyleHashes.length}, null, 2));
