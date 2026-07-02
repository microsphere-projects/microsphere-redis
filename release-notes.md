# Release Notes

## v0.1.4

# Release Notes - Version 0.1.4

## New Features
- Added annotation processor dependencies and initialized connections. ([096c52f](#))

## Dependency Updates
- Updated Maven Wrapper to version `3.9.15`. ([06c7213](#))

## Build and Workflow Enhancements
- Updated release workflow, README, and project POMs. ([3fa8ed9](#))
- Enabled automated release notes generation in Maven workflow. ([8cfbd0f](#))
- Set workflow permissions to read-only for contents. ([a70e579](#))

## Bug Fixes
- Fixed formatting issues in Dependabot YAML list. ([40d7136](#))

## Other Changes
- Renamed property to `spring-cloud.version`. ([1533116](#))
- Merged `release-1.x` and upstream changes into `dev-1.x`. ([882fdda](#), [4fd8d30](#), [8c882f8](#))
- Bumped version to `0.1.4`. ([20e4f88](#))

---

For additional details, refer to the [Full Changelog](#).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.1.3...0.1.4## v0.1.5

# Release Notes - Version 0.1.5

## Build and Workflow Enhancements
- Added Docker Compose step before publishing the package for streamlined deployment. (#98bcd9e)
- Integrated OSSRH credentials into the Maven publish workflow for secure releases. (#5197848)
- Standardized Java setup and Maven configurations in CI for consistency. (#d00c6d4)

## Dependency Updates
- Upgraded `microsphere-spring-cloud` parent to version 0.1.12. (#7bc9363)

## Documentation
- Updated README to reflect new branch names and versioning conventions. (#5c15351)

## Test Improvements
- Simplified dependencies by adding test-specific libraries and removing redundant `logback` tests. (#e5210cd)

## Other Changes
- Prepared the project for future versions by bumping to the next patch post publishing 0.1.4. (#d2f226b)

---

Thank you for your contributions!

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.1.4...0.1.5## v0.1.6

# Release Notes: Version 0.1.6

## Dependency Updates
- Updated parent BOM to latest version. 

## Documentation
- Updated README to reflect correct version numbers.

## Code Cleanup
- Removed trailing whitespace and duplicate blank lines across Java source files.

## Other Changes
- Advanced version to 0.1.6 following the 0.1.5 release. 
- Merged updates and cleanup changes into the development branch. 

---

*Note: Full changelog excluded for brevity.*

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.1.5...0.1.6## v0.1.7

# Release Notes for v0.1.7

## New Features
- Added support for configurable interceptor sources and registration. (#653ee3d)

## Bug Fixes
- Fixed Javadoc formatting issues. (#d7be93e)

## Dependency Updates
- Upgraded `microsphere-spring-cloud` to version 0.1.15. (#48a7822)

## Test Improvements
- Introduced `LoggingRedisConnectionInterceptor` for testing purposes. (#e490a7a)
- Added test for interceptor mapping. (#d7be93e)

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x`. (#caf0ae7)
- Bumped version to the next patch after publishing 0.1.6. (#650fc38)

---

**Note**: For a full list of changes, refer to the commit history.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.1.6...0.1.7## v0.1.8

# Release Notes - Version 0.1.8  

## Dependency Updates  
- Upgraded `microsphere-spring-cloud` to version **0.1.16**.  

## Documentation  
- Updated README to reflect bumped branch versions.  

## Build and Workflow Enhancements  
- Merged `release-1.x` into `dev-1.x` for consistency.  

## Other Changes  
- Bumped version to prepare for the next patch after publishing 0.1.7.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.1.7...0.1.8## v0.1.9

# Release Notes for Version 0.1.9

## New Features
- Added Redis Spring Boot/Cloud integration modules. ([0f283b3](#))

## Bug Fixes
- Fixed bean registration type in proxy BPP test. ([57eea67](#))

## Documentation
- Updated version numbers in `README.md`. ([ba4130c](#))

## Dependency Updates
- Bumped `microsphere-spring-cloud` dependency to version 0.1.21. ([851c638](#))

## Code Cleanup
- Trimmed stale Redis feature metadata. ([9330814](#))
- Removed generator module from reactor. ([a8ffe9b](#))
- Cleaned up unused imports. ([3341585](#))

## Other Changes
- Merged `release-1.x` into `dev-1.x`. ([d9adc82](#))
- Bumped version to next patch after publishing 0.1.8. ([e93620a](#))

---

**Note**: For a detailed commit history, refer to the full changelog.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-redis/compare/0.1.8...0.1.9