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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.2.7...0.2.8## v0.2.9

# Release Notes for Version 0.2.9

## 🚀 New Features
- **Redis Support**:  
  - Added Redis Spring Boot and Spring Cloud modules.  
  - Introduced a Redis Cloud listener and refactored auto-configuration.  
  - Added `microsphere-redis-spring-test` module for Redis testing.  
  - Enabled Redis by default and updated related tests and POMs.  
  - Replaced MyBatis tests with Redis equivalents.  
  - Externalized Redis feature metadata to YAML for easier configuration.  

## 🐞 Bug Fixes
- Fixed bean registration test casting issue.  

## 📚 Documentation Updates
- Updated README to include new modules and versions.  

## 🔧 Dependency Updates
- Upgraded Spring Cloud parent to version `0.2.21`.  

## 🧪 Test Improvements
- Updated expected feature count in tests to reflect Redis changes.  

## ⚙️ Build and Workflow Enhancements
- Removed optional Mockito dependency to streamline dependencies.  

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.2.8...0.2.9## v0.2.10

# Release Notes - Version 0.2.10

## New Features
- Introduced shared Redis interceptor enablement checks for streamlined configuration. ([#458070a](#))
- Added `RedisSpringUtils` helper for managing Redis interceptor toggles. ([#e2ee381](#))
- Enabled inheritance for Redis-related annotations: `EnableRedisInterceptor`, `EnableRedisConfiguration`, and Redis context annotations. ([#f898b78](#), [#88c5c0f](#), [#ac6cafc](#))

## Bug Fixes
- Removed unused imports and loggers to improve code cleanliness. ([#3e23bf3](#), [#9eff240](#), [#56a5ffd](#))
- Ensured Redis support classes are properly required for related configurations. ([#91a0ec1](#))

## Test Improvements
- Added specific test cases to validate Redis interceptor toggle functionality. ([#cede24b](#))

## Documentation
- Updated README with revised branch version matrix information. ([#c05f76e](#))
- Improved Javadoc comments for `ConditionalOnRedisAvailable` annotation. ([#a58fc82](#))

## Build and Workflow Enhancements
- Routine merges between `main` and `release` branches. ([#9a4f575](#), [#cb0c6bc](#), [#ea249e8](#))
- Bumped version to prepare for the next patch release. ([#a1de953](#))

## Other Changes
- Refactored base classes for Redis initializers and registrars to improve modularity. ([#4e8a7ae](#), [#1ca6383](#))
- Cleaned up redundant whitespace in `RedisSpringUtils`. ([#173f529](#))
- Added gating for Redis interceptor registrar based on configuration. ([#692e99d](#))

---

**Note**: For the complete list of changes, refer to the [Full Changelog](#).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.2.9...0.2.10