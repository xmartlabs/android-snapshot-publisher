Change Log
==========
All notable changes to this project will be documented in this file.

[Version 1.0.2 _(2018-04-30)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v1.0.2)
---

### New Features
- Allow to use the `debuggable` build types in Fabric's Beta. (#30)

[Version 1.0.1 _(2018-04-17)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v1.0.1)
---

### New Features
- Added new `history` variable to use in `releaseNotesFormat` given by `historyFormat`.
It's shown only if the `{commitHistory}` is not empty. (#25)
- Added `includeHistorySinceLastTag` release notes setup variable.
It enables to generate the history only for the commits after the latest git's tag.  
It's useful to show only the changes that changed since the last build. (#25)

### Fixes
- Fix version name issue in generated release notes. (#24)

[Version 1.0.0 _(2018-04-12)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v1.0.0)
---

### New Features
- Add the ability to include the last commit in the release notes history. (#16)
- Create preparation tasks.
It enables you to create snapshot builds and test them locally without the necessity of deploy it. (#17)
- Include git branch name in the app custom version name options (#18)

### Fixes
- Truncate Fabric Beta's release notes if its length is greater than `16384` characters. (#21)

### Breaking changes
- `distributionEmails` and `distributionGroupAliases` in Fabric's Beta configuration block are changed to strings instead of a list of strings.
These values are built by joining all `emails` or `aliases` with commas.
This change was made in order to make the setup process easier. (#19)

[Version 0.0.1 _(2018-03-08)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v0.0.1)
---

This is the initial version.
