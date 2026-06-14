#!/usr/bin/env node

import fs from 'node:fs/promises';
import fsSync from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const contentRoot = path.join(repoRoot, 'site-content');
const docsRoot = path.join(contentRoot, 'docs');
const staticRoot = path.join(contentRoot, 'static');

const markdownExtensions = new Set(['.md']);
const excludedMarkdownBaseNames = new Set([
  'changelog',
  'contributing',
  'maintainers',
]);
const staticAssetExtensions = new Set([
  '.zip',
  '.config',
  '.conf',
  '.properties',
  '.yml',
  '.yaml',
  '.json',
  '.png',
  '.jpg',
  '.jpeg',
  '.gif',
  '.svg',
  '.webp',
  '.ico',
]);

const ignoredDirectories = new Set([
  '.git',
  '.docusaurus',
  'node_modules',
  'pages-build',
  'site-content',
]);

const alwaysStaticAssets = ['docs/images/icon_guc.jpg'];
const linkedAssets = new Map();

await resetGeneratedContent();
const allFiles = await collectFiles(repoRoot);
const markdownFiles = allFiles.filter(shouldGenerateMarkdown);

for (const sourcePath of markdownFiles) {
  const relPath = toPosix(path.relative(repoRoot, sourcePath));
  const markdown = await fs.readFile(sourcePath, 'utf8');
  const rewritten = rewriteAssetLinks(markdown, relPath);
  await writeGeneratedFile(path.join(docsRoot, relPath), rewritten);
}

for (const relPath of alwaysStaticAssets) {
  const resolved = resolveExistingRepoPath(relPath);
  if (resolved) {
    linkedAssets.set(resolved.relPath, resolved.absPath);
  }
}

await copyLinkedAssets();

console.log(
  `Generated Docusaurus content: ${markdownFiles.length} Markdown files, ${linkedAssets.size} static assets.`,
);

async function resetGeneratedContent() {
  if (!contentRoot.endsWith(`${path.sep}site-content`)) {
    throw new Error(`Refusing to delete unexpected path: ${contentRoot}`);
  }

  await fs.rm(contentRoot, {recursive: true, force: true});
  await fs.mkdir(docsRoot, {recursive: true});
  await fs.mkdir(staticRoot, {recursive: true});
}

async function collectFiles(directory) {
  const entries = await fs.readdir(directory, {withFileTypes: true});
  const files = [];

  for (const entry of entries.sort((a, b) => a.name.localeCompare(b.name))) {
    if (entry.isDirectory()) {
      if (ignoredDirectories.has(entry.name)) {
        continue;
      }

      files.push(...(await collectFiles(path.join(directory, entry.name))));
      continue;
    }

    if (entry.isFile()) {
      files.push(path.join(directory, entry.name));
    }
  }

  return files;
}

function rewriteAssetLinks(markdown, markdownRelPath) {
  const lines = markdown.split('\n');
  let inFence = false;
  let fenceMarker = '';

  return lines
    .map((line) => {
      const fenceMatch = line.match(/^(\s*)(`{3,}|~{3,})/);
      if (fenceMatch) {
        const marker = fenceMatch[2];
        const markerStart = fenceMatch[1].length;
        const closesOnSameLine = line.indexOf(marker, markerStart + marker.length) !== -1;
        if (closesOnSameLine) {
          return line;
        }

        if (!inFence) {
          inFence = true;
          fenceMarker = marker;
        } else if (marker[0] === fenceMarker[0] && marker.length >= fenceMarker.length) {
          inFence = false;
          fenceMarker = '';
        }
        return line;
      }

      if (inFence) {
        return line;
      }

      return rewriteLine(line, markdownRelPath);
    })
    .join('\n');
}

function shouldGenerateMarkdown(filePath) {
  if (!markdownExtensions.has(path.extname(filePath).toLowerCase())) {
    return false;
  }

  const relPath = toPosix(path.relative(repoRoot, filePath));
  return !isExcludedMarkdownRelPath(relPath);
}

function isExcludedMarkdownRelPath(relPath) {
  if (relPath === '.github' || relPath.startsWith('.github/')) {
    return true;
  }

  const baseName = path.basename(relPath, path.extname(relPath)).toLowerCase();
  return excludedMarkdownBaseNames.has(baseName);
}

function rewriteLine(line, markdownRelPath) {
  const withMarkdownLinks = line.replace(/(!?)\[([^\]]*?)\]\(([^)\n]+)\)/g, (match, imageMarker, label, target) => {
    if (!imageMarker && shouldUnwrapExcludedMarkdownLink(target, markdownRelPath)) {
      return label || stripMarkdownAngles(extractMarkdownDestination(target) ?? target);
    }

    const rewritten = rewriteMarkdownDestination(target, markdownRelPath);
    return rewritten ? `${imageMarker}[${label}](${rewritten})` : match;
  });

  const withHtmlAttributes = withMarkdownLinks.replace(/\b(href|src)=(["'])([^"']+)\2/gi, (match, attr, quote, target) => {
    const rewritten = toStaticAssetUrl(target, markdownRelPath);
    return rewritten ? `${attr}=${quote}${rewritten}${quote}` : match;
  });

  return escapeMdxTextExpressions(escapePlaceholderPortUrls(withHtmlAttributes));
}

function rewriteMarkdownDestination(rawDestination, markdownRelPath) {
  const leadingWhitespace = rawDestination.match(/^\s*/)?.[0] ?? '';
  const trailingWhitespace = rawDestination.match(/\s*$/)?.[0] ?? '';
  const trimmed = rawDestination.trim();

  if (!trimmed) {
    return null;
  }

  if (trimmed.startsWith('<')) {
    const closeIndex = trimmed.indexOf('>');
    if (closeIndex === -1) {
      return null;
    }

    const destination = trimmed.slice(1, closeIndex);
    const title = trimmed.slice(closeIndex + 1);
    const rewritten = toStaticAssetUrl(destination, markdownRelPath);
    return rewritten ? `${leadingWhitespace}<${rewritten}>${title}${trailingWhitespace}` : null;
  }

  const titleMatch = trimmed.match(/^(\S+)(\s+["'(].*)$/);
  const destination = titleMatch ? titleMatch[1] : trimmed;
  const title = titleMatch ? titleMatch[2] : '';
  const rewritten = toStaticAssetUrl(destination, markdownRelPath);

  return rewritten ? `${leadingWhitespace}${rewritten}${title}${trailingWhitespace}` : null;
}

function extractMarkdownDestination(rawDestination) {
  const trimmed = rawDestination.trim();

  if (!trimmed) {
    return null;
  }

  if (trimmed.startsWith('<')) {
    const closeIndex = trimmed.indexOf('>');
    return closeIndex === -1 ? null : trimmed.slice(1, closeIndex);
  }

  const titleMatch = trimmed.match(/^(\S+)(\s+["'(].*)$/);
  return titleMatch ? titleMatch[1] : trimmed;
}

function shouldUnwrapExcludedMarkdownLink(rawDestination, markdownRelPath) {
  const destination = extractMarkdownDestination(rawDestination);
  if (!destination) {
    return false;
  }

  const resolved = resolveLinkedMarkdown(destination, markdownRelPath);
  if (resolved) {
    return isExcludedMarkdownRelPath(resolved.relPath);
  }

  if (/^https?:\/\//i.test(destination)) {
    return false;
  }

  const {pathname} = splitUrlPathAndSuffix(stripMarkdownAngles(destination));
  const baseName = path.basename(pathname, path.extname(pathname)).toLowerCase();
  return markdownExtensions.has(path.extname(pathname).toLowerCase()) && excludedMarkdownBaseNames.has(baseName);
}

function resolveLinkedMarkdown(rawTarget, markdownRelPath) {
  const target = rawTarget.trim();
  if (!target || target.startsWith('#') || target.startsWith('//')) {
    return null;
  }

  if (/^(mailto|tel|javascript|data):/i.test(target)) {
    return null;
  }

  let candidatePath;
  if (/^https?:\/\//i.test(target)) {
    candidatePath = repoPathFromHttpUrl(target);
    if (!candidatePath) {
      return null;
    }
  } else {
    const {pathname} = splitUrlPathAndSuffix(stripMarkdownAngles(target));
    const decodedPathname = decodeUrlPath(pathname);
    const sourceDir = path.posix.dirname(markdownRelPath);
    candidatePath = decodedPathname.startsWith('/')
      ? decodedPathname.slice(1)
      : path.posix.normalize(path.posix.join(sourceDir, decodedPathname));
  }

  const normalized = path.posix.normalize(candidatePath).replace(/^\.\//, '');
  if (!markdownExtensions.has(path.extname(normalized).toLowerCase())) {
    return null;
  }

  return resolveExistingRepoPath(normalized);
}

function toStaticAssetUrl(rawTarget, markdownRelPath) {
  const resolved = resolveLinkedStaticAsset(rawTarget, markdownRelPath);
  if (!resolved) {
    return null;
  }

  linkedAssets.set(resolved.relPath, resolved.absPath);
  return `/${encodePathForUrl(resolved.relPath)}${resolved.suffix}`;
}

function resolveLinkedStaticAsset(rawTarget, markdownRelPath) {
  const target = rawTarget.trim();
  if (!target || target.startsWith('#') || target.startsWith('//')) {
    return null;
  }

  if (/^(mailto|tel|javascript|data):/i.test(target)) {
    return null;
  }

  if (/^https?:\/\//i.test(target)) {
    return resolveRepoHttpUrl(target);
  }

  const {pathname, suffix} = splitUrlPathAndSuffix(stripMarkdownAngles(target));
  const decodedPathname = decodeUrlPath(pathname);
  const sourceDir = path.posix.dirname(markdownRelPath);
  const candidate = decodedPathname.startsWith('/')
    ? decodedPathname.slice(1)
    : path.posix.normalize(path.posix.join(sourceDir, decodedPathname));

  return resolveStaticAsset(candidate, suffix);
}

function resolveRepoHttpUrl(rawTarget) {
  const repoPath = repoPathFromHttpUrl(rawTarget);
  if (!repoPath) {
    return null;
  }

  let parsed;
  try {
    parsed = new URL(rawTarget);
  } catch {
    return null;
  }

  return resolveStaticAsset(repoPath, parsed.search + parsed.hash);
}

function repoPathFromHttpUrl(rawTarget) {
  let parsed;
  try {
    parsed = new URL(rawTarget);
  } catch {
    return null;
  }

  const host = parsed.hostname.toLowerCase();
  const parts = parsed.pathname.split('/').filter(Boolean).map(decodeUrlPath);
  let candidateParts = null;

  if (host === 'github.com' || host === 'github.ibm.com') {
    const repoIndex = parts.findIndex((part) => part.toLowerCase() === 'universal-connectors');
    const urlMode = repoIndex === -1 ? null : parts[repoIndex + 1];

    if ((urlMode === 'blob' || urlMode === 'raw') && parts.length > repoIndex + 3) {
      candidateParts = parts.slice(repoIndex + 3);
    }
  }

  if (host === 'raw.githubusercontent.com') {
    const repoIndex = parts.findIndex((part) => part.toLowerCase() === 'universal-connectors');
    if (repoIndex !== -1 && parts.length > repoIndex + 2) {
      candidateParts = parts.slice(repoIndex + 2);
    }
  }

  if (!candidateParts) {
    return null;
  }

  return candidateParts.join('/');
}

function resolveStaticAsset(candidatePath, suffix = '') {
  const normalized = path.posix.normalize(candidatePath).replace(/^\.\//, '');

  if (!normalized || normalized === '.' || normalized.startsWith('../') || path.posix.isAbsolute(normalized)) {
    return null;
  }

  if (!staticAssetExtensions.has(path.extname(normalized).toLowerCase())) {
    return null;
  }

  const resolved = resolveExistingRepoPath(normalized);
  if (!resolved) {
    return null;
  }

  return {...resolved, suffix};
}

function resolveExistingRepoPath(repoRelPath) {
  const segments = toPosix(repoRelPath)
    .split('/')
    .filter((segment) => segment && segment !== '.');

  if (segments.some((segment) => segment === '..')) {
    return null;
  }

  let currentPath = repoRoot;
  const actualSegments = [];

  for (const segment of segments) {
    const exactPath = path.join(currentPath, segment);
    if (fsSync.existsSync(exactPath)) {
      currentPath = exactPath;
      actualSegments.push(segment);
      continue;
    }

    if (!fsSync.existsSync(currentPath) || !fsSync.statSync(currentPath).isDirectory()) {
      return null;
    }

    const directoryEntries = fsSync.readdirSync(currentPath);
    let matchingName = directoryEntries.find((name) => name.toLowerCase() === segment.toLowerCase());

    if (!matchingName) {
      const looseSegment = normalizeLoosePathSegment(segment);
      const looseMatches = directoryEntries.filter((name) => normalizeLoosePathSegment(name) === looseSegment);
      if (looseMatches.length === 1) {
        matchingName = looseMatches[0];
      }
    }

    if (!matchingName) {
      return null;
    }

    currentPath = path.join(currentPath, matchingName);
    actualSegments.push(matchingName);
  }

  if (!fsSync.existsSync(currentPath) || !fsSync.statSync(currentPath).isFile()) {
    return null;
  }

  return {
    absPath: currentPath,
    relPath: actualSegments.join('/'),
  };
}

async function copyLinkedAssets() {
  for (const [relPath, sourcePath] of [...linkedAssets.entries()].sort(([a], [b]) => a.localeCompare(b))) {
    await copyFile(sourcePath, path.join(staticRoot, relPath));
  }
}

async function writeGeneratedFile(destinationPath, contents) {
  await fs.mkdir(path.dirname(destinationPath), {recursive: true});
  await fs.writeFile(destinationPath, contents);
}

async function copyFile(sourcePath, destinationPath) {
  await fs.mkdir(path.dirname(destinationPath), {recursive: true});
  await fs.copyFile(sourcePath, destinationPath);
}

function stripMarkdownAngles(target) {
  return target.startsWith('<') && target.endsWith('>') ? target.slice(1, -1) : target;
}

function splitUrlPathAndSuffix(target) {
  const queryIndex = target.indexOf('?');
  const hashIndex = target.indexOf('#');
  const suffixIndex = [queryIndex, hashIndex].filter((index) => index !== -1).sort((a, b) => a - b)[0];

  if (suffixIndex === undefined) {
    return {pathname: target, suffix: ''};
  }

  return {
    pathname: target.slice(0, suffixIndex),
    suffix: target.slice(suffixIndex),
  };
}

function decodeUrlPath(value) {
  try {
    return decodeURIComponent(value);
  } catch {
    return value;
  }
}

function escapePlaceholderPortUrls(line) {
  return transformOutsideInlineCode(line, (segment) =>
    segment.replace(/\bhttps?:\/\/[^\s<>()`/]+:[A-Za-z_][A-Za-z0-9_<>-]*(\/[^\s<>()`]*)?/g, (url) => `\`${url}\``),
  );
}

function escapeMdxTextExpressions(line) {
  return transformOutsideInlineCode(line, (segment) =>
    segment.replace(/[{}]/g, (character) => (character === '{' ? '&#123;' : '&#125;')),
  );
}

function transformOutsideInlineCode(line, transform) {
  let result = '';
  let cursor = 0;

  while (cursor < line.length) {
    const nextTick = line.indexOf('`', cursor);
    if (nextTick === -1) {
      result += transform(line.slice(cursor));
      break;
    }

    result += transform(line.slice(cursor, nextTick));

    const tickRun = line.slice(nextTick).match(/^`+/)?.[0] ?? '`';
    const closingTick = line.indexOf(tickRun, nextTick + tickRun.length);
    if (closingTick === -1) {
      result += line.slice(nextTick);
      break;
    }

    result += line.slice(nextTick, closingTick + tickRun.length);
    cursor = closingTick + tickRun.length;
  }

  return result;
}

function normalizeLoosePathSegment(value) {
  return value.toLowerCase().replace(/[-_\s]/g, '');
}

function encodePathForUrl(repoRelPath) {
  return repoRelPath.split('/').map(encodeURIComponent).join('/');
}

function toPosix(value) {
  return value.split(path.sep).join('/');
}
