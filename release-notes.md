# Release Notes

## v0.2.3

```markdown
# Release Notes - Version 0.2.3

## Other Changes
- Internal merges from the main branch into the release branch. [#209ce78](https://github.com/your-repo/commit/209ce78), [#48198f4](https://github.com/your-repo/commit/48198f4), [#31f0557](https://github.com/your-repo/commit/31f0557)
```

## v0.2.4

# Release Notes: Version 0.2.4

## New Features
- **Added `microsphere-annotation-processor` to POMs** for enhanced annotation processing. ([fac6d50](https://example.com/commit/fac6d50))

## Bug Fixes
- Resolved stray blank line in `RedisCommandUtils`. ([51228b2](https://example.com/commit/51228b2))
- Fixed indentation issues in `dependabot.yml` updates. ([e083d70](https://example.com/commit/e083d70))

## Documentation
- Updated README to reflect accurate branch versioning. ([8b73ae4](https://example.com/commit/8b73ae4))
- Improved release notes and release creation process. ([263b7bd](https://example.com/commit/263b7bd))

## Dependency Updates
- Bumped `microsphere-spring-cloud` dependency to version `0.2.11`. ([4cb49cd](https://example.com/commit/4cb49cd))
- Corrected dependency comment for Spring Cloud. ([4530c6e](https://example.com/commit/4530c6e))

## Build and Workflow Enhancements
- Removed trailing newline from `generate-wiki-docs.py`. ([2f61642](https://example.com/commit/2f61642))
- Automated version bump to prepare for patch release after `0.2.3`. ([08e4274](https://example.com/commit/08e4274))

---

For a full history of changes, please refer to the [changelog](https://example.com/changelog).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.2.3...0.2.4## v0.2.5

# Release Notes - Version 0.2.5

## New Features
- Added `microsphere-java-test` module.  
- Added Docker Compose step to the publish workflow.  
- Integrated OSSRH credentials into the Maven publish workflow.  

## Dependency Updates
- Upgraded parent and Spring Cloud versions to `0.2.12`.  

## Test Improvements
- Removed redundant test logback configuration files.  

## Build and Workflow Enhancements
- Adjusted GitHub Actions workflows for Java/Maven.  
- Bumped README branch versions to `0.2.5/0.1.5`.  

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.2.4...0.2.5## v0.2.6

# Release Notes - Version 0.2.6

## Build and Workflow Enhancements
- Merged `main` into `release` branches to ensure alignment. [#skip ci]

## Documentations
- Updated `README` with new version references. [6d6a291]

## Other Changes
- Cleaned up Java source code by removing duplicate line separators and trailing whitespace. [#45]

---

**Note:** This release focuses on minor housekeeping and alignment tasks.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.2.5...0.2.6## v0.2.7

# Release Notes: Version 0.2.7

## New Features
- **Configurable Interceptor Sources**: Added support for configurable Redis interceptor bean sources. ([fc1151a](#))

## Bug Fixes
- Removed unused imports in Redis interceptor files, improving code cleanliness. ([a4f1abe](#))

## Documentation
- Updated README with the latest version references. ([d135c11](#))

## Test Improvements
- Enabled test interceptor and normalized Javadoc spacing. ([2959548](#))  
- Refactored Redis interceptor registration tests for better maintainability. ([6fb2dd7](#))

## Build and Workflow Enhancements
- Bumped parent version to `0.2.15`. ([d135c11](#))  
- Prepared for next development iteration by bumping patch version. ([c579d33](#))  

---

**Note**: Internal chore commits and merges excluded.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.2.6...0.2.7## v0.2.8

# Release Notes for v0.2.8

## Dependency Updates
- Upgraded Microsphere parent and documentation dependencies to version `0.2.16`. ([c731f80](#))

## Other Changes
- Version bumped to `0.2.8` post publishing `0.2.7`. ([5c17d85](#))  
- Merged `main` into `release` and vice versa. ([2ff097b](#), [3632411](#))  

---

**Note:** This release primarily includes dependency updates and preparatory version bumps.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.2.7...0.2.8