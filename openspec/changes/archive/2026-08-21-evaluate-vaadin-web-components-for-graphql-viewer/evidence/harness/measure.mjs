/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {createHash} from 'node:crypto';
import {gzipSync} from 'node:zlib';
import {mkdirSync, readFileSync, readdirSync, statSync, writeFileSync} from 'node:fs';
import {dirname, extname, normalize, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const project = resolve(directory, '../../../../..');
const broad = [resolve(directory, 'generated/vaadin-selective.js')];
const split = walk(resolve(directory, 'generated/split'));
const foundation = walk(resolve(project, 'viewers/webcomponents/foundation/src')).filter(browserAsset);
const htmx = walk(resolve(project, 'viewers/webcomponents/htmx/src/main/resources')).filter(browserAsset);
const directSource = Object.keys(JSON.parse(readFileSync(resolve(directory, 'package-lock.json'), 'utf8')).packages[''].dependencies).flatMap(name => {
  const packageDirectory = resolve(directory, 'node_modules', ...name.split('/'));
  return walk(packageDirectory).filter(path => !path.includes('/test/') && !path.includes('/docs/'));
});

const measurements = {
  currentViewerBrowserAssets: measure(foundation.concat(htmx)),
  vaadinBroadSelectiveBundle: measure(broad),
  vaadinRouteSplitBundle: measure(split),
  vaadinDirectInstalledSources: measure(directSource)
};
const splitRoot = resolve(directory, 'generated/split');
const splitFiles = Object.fromEntries(split.map(path => [path.slice(splitRoot.length + 1), measure([path])]));
const metafile = JSON.parse(readFileSync(resolve(directory, 'generated/split-metafile.json'), 'utf8'));
const entryClosures = Object.fromEntries(['reference.js', 'grid.js', 'fields.js'].map(entry => [entry, measure(outputClosure(metafile.outputs, entry))]));
const result = {generatedAt: new Date().toISOString(), measurements, splitFiles, entryClosures};

if (process.argv.includes('--write')) {
  const output = resolve(directory, '..', 'results', 'asset-sizes.json');
  mkdirSync(dirname(output), {recursive: true});
  writeFileSync(output, `${JSON.stringify(result, null, 2)}\n`);
}
console.log(JSON.stringify({measurements, splitFiles, entryClosures}, null, 2));

function measure(files) {
  let rawBytes = 0;
  let gzipBytes = 0;
  const hashes = [];
  for (const file of [...new Set(files)]) {
    const bytes = readFileSync(file);
    rawBytes += bytes.length;
    gzipBytes += gzipSync(bytes, {level: 9}).length;
    hashes.push(createHash('sha256').update(bytes).digest('hex'));
  }
  return {fileCount: new Set(files).size, rawBytes, gzipBytes, contentSetSha256: createHash('sha256').update(hashes.sort().join('\n')).digest('hex')};
}

function walk(root) {
  const result = [];
  for (const entry of readdirSync(root, {withFileTypes: true})) {
    const path = resolve(root, entry.name);
    if (entry.isDirectory()) result.push(...walk(path));
    else result.push(path);
  }
  return result;
}

function outputClosure(outputs, entry) {
  const root = Object.keys(outputs).find(path => path.endsWith(`/${entry}`) || path === entry);
  const seen = new Set();
  const visit = path => {
    if (!path || seen.has(path)) return;
    seen.add(path);
    for (const imported of outputs[path]?.imports ?? []) {
      const relative = normalize(`${dirname(path)}/${imported.path}`);
      const target = outputs[relative] ? relative : Object.keys(outputs).find(candidate => candidate.endsWith(imported.path));
      visit(target);
    }
  };
  visit(root);
  return [...seen];
}

function browserAsset(path) { return ['.css', '.js', '.mjs', '.svg', '.woff2'].includes(extname(path)); }
