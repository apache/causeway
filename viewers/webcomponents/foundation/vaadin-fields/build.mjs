/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {build} from 'esbuild';
import {createHash} from 'node:crypto';
import {cpSync, existsSync, mkdirSync, readFileSync, readdirSync, rmSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {gzipSync} from 'node:zlib';

const directory = fileURLToPath(new URL('.', import.meta.url));
const generated = resolve(directory, 'generated');
const assets = resolve(generated, 'assets');
const licenses = resolve(generated, 'licenses');
const policyPath = resolve(directory, 'policy.json');
const policy = JSON.parse(readFileSync(policyPath, 'utf8'));
const lock = JSON.parse(readFileSync(resolve(directory, 'package-lock.json'), 'utf8'));
const updatePolicy = process.argv.includes('--write-policy');
const families = [
  {id: 'basic', entry: 'basic-entry.mjs'},
  {id: 'numeric', entry: 'numeric-entry.mjs'},
  {id: 'local-temporal', entry: 'local-temporal-entry.mjs'}
];

rmSync(generated, {recursive: true, force: true});
mkdirSync(assets, {recursive: true});
mkdirSync(licenses, {recursive: true});

const bundleMetadata = {};
for (const family of families) {
  const outfile = resolve(assets, `vaadin-${family.id}.js`);
  await build({
    entryPoints: [resolve(directory, family.entry)],
    outfile,
    bundle: true,
    format: 'esm',
    platform: 'browser',
    target: ['es2022'],
    minify: true,
    legalComments: 'none',
    sourcemap: false,
    define: {'process.env.NODE_ENV': '"production"'},
    plugins: [usageStatisticsOptOut()]
  });
  const bytes = readFileSync(outfile);
  const metadata = {
    file: `vaadin-${family.id}.js`,
    rawBytes: bytes.length,
    gzipBytes: gzipSync(bytes, {level: 9}).length,
    sha256: sha256(bytes)
  };
  bundleMetadata[family.id] = metadata;
  if (updatePolicy) {
    policy.families[family.id].bundleSha256 = metadata.sha256;
  } else if (policy.families[family.id].bundleSha256 !== metadata.sha256) {
    throw new Error(`${family.id} checksum ${metadata.sha256} does not match policy ${policy.families[family.id].bundleSha256}.`);
  }
  if (metadata.gzipBytes > policy.families[family.id].maximumGzipBytes) {
    throw new Error(`${family.id} gzip size ${metadata.gzipBytes} exceeds ${policy.families[family.id].maximumGzipBytes}.`);
  }
  writeFileSync(resolve(assets, `csp-policy-${family.id}.json`), `${JSON.stringify({
    family: family.id,
    styleHashes: policy.families[family.id].cspStyleHashes
  }, null, 2)}\n`);
}

const aggregateGzipBytes = Object.values(bundleMetadata).reduce((total, bundle) => total + bundle.gzipBytes, 0);
if (aggregateGzipBytes > policy.aggregateMaximumGzipBytes) {
  throw new Error(`Aggregate gzip size ${aggregateGzipBytes} exceeds ${policy.aggregateMaximumGzipBytes}.`);
}
if (updatePolicy) writeFileSync(policyPath, `${JSON.stringify(policy, null, 2)}\n`);

const packages = productionPackages(lock);
for (const pkg of packages) copyLegalFiles(pkg);
const metadata = {
  bundles: bundleMetadata,
  aggregateGzipBytes,
  aggregateMaximumGzipBytes: policy.aggregateMaximumGzipBytes,
  families: Object.fromEntries(families.map(family => [family.id, {
    entryPoints: policy.families[family.id].entryPoints,
    maximumGzipBytes: policy.families[family.id].maximumGzipBytes,
    cspStyleHashes: policy.families[family.id].cspStyleHashes
  }])),
  usageStatisticsAlias: '@vaadin/vaadin-usage-statistics/vaadin-usage-statistics-optout.js',
  packages
};
writeFileSync(resolve(licenses, 'THIRD-PARTY.json'), `${JSON.stringify(metadata, null, 2)}\n`);
console.log(JSON.stringify({bundles: bundleMetadata, aggregateGzipBytes, packageCount: packages.length}, null, 2));

function usageStatisticsOptOut() {
  return {
    name: 'disable-vaadin-usage-statistics',
    setup(pluginBuild) {
      pluginBuild.onResolve({filter: /^@vaadin\/vaadin-usage-statistics\/vaadin-usage-statistics\.js$/}, () => ({
        path: resolve(directory, 'node_modules/@vaadin/vaadin-usage-statistics/vaadin-usage-statistics-optout.js')
      }));
    }
  };
}

function productionPackages(packageLock) {
  const result = [];
  for (const [packagePath, entry] of Object.entries(packageLock.packages ?? {})) {
    if (!packagePath.startsWith('node_modules/') || entry.dev) continue;
    const name = packagePath.slice('node_modules/'.length);
    const packageJsonPath = resolve(directory, packagePath, 'package.json');
    if (!existsSync(packageJsonPath)) throw new Error(`Missing installed package ${name}. Run npm ci --ignore-scripts.`);
    const manifest = JSON.parse(readFileSync(packageJsonPath, 'utf8'));
    const license = manifest.license ?? entry.license;
    if (!license) throw new Error(`Package ${name} has no declared license.`);
    result.push({
      name,
      version: manifest.version,
      license,
      integrity: entry.integrity ?? null,
      repository: typeof manifest.repository === 'string' ? manifest.repository : manifest.repository?.url ?? null
    });
  }
  return result.sort((left, right) => left.name.localeCompare(right.name));
}

function copyLegalFiles(pkg) {
  const packageDirectory = resolve(directory, 'node_modules', pkg.name);
  let candidates = readdirSync(packageDirectory).filter(file => /^(LICENSE|LICENCE|NOTICE)(\.|$)/i.test(file));
  let sourceDirectory = packageDirectory;
  if (!candidates.some(file => /^LICEN[CS]E(\.|$)/i.test(file)) && pkg.name === '@lit-labs/ssr-dom-shim') {
    sourceDirectory = resolve(directory, 'node_modules/lit');
    candidates = readdirSync(sourceDirectory).filter(file => /^LICEN[CS]E(\.|$)/i.test(file));
    pkg.licenseSource = 'lit package from the same repository and BSD-3-Clause license';
  }
  if (!candidates.some(file => /^LICEN[CS]E(\.|$)/i.test(file))) throw new Error(`Package ${pkg.name} has no distributable license file.`);
  for (const file of candidates) {
    const suffix = sourceDirectory === packageDirectory ? file : `${file}-from-lit`;
    cpSync(resolve(sourceDirectory, file), resolve(licenses, `${sanitize(pkg.name)}-${pkg.version}-${suffix}`));
  }
}

function sanitize(value) {
  return value.replace(/^@/, '').replaceAll('/', '__');
}

function sha256(value) {
  return createHash('sha256').update(value).digest('hex');
}
