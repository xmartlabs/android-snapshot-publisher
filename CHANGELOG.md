Change Log
==========
All notable changes to this project will be documented in this file.

[Version 1.0.0 _(2018-04-11)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v1.0.0)
---

### New Features
- Add the ability to include the last commit in the release note history. (#16)
- Create preparation tasks.
It enables you to create snapshot builds and test them locally without the necessity of deploy it. (#17)
- Include git branch name in the app custom version name options (#18)

### Fixes
- Truncate Fabric Beta's release notes if its length is greater than `16384` characters.

### Breaking changes
- `distributionEmails` and `distributionGroupAliases` in Fabric's Beta configuration block are changed to strings instead of a list of string.
These values are built by joining all `emails` or `aliases` with commas.
This change was made in order to make easier the setup process. (#19)

[Version 0.0.1 _(2018-03-08)_](https://github.com/xmartlabs/android-snapshot-publisher/releases/tag/v0.0.1)
---

This is the initial version.
