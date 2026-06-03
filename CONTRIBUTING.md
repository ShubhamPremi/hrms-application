# Contributing to HRMS

## Branch naming
feature/ · fix/ · refactor/ · chore/

## Commit message format
<type>: <short description in present tense>

Types: feat · fix · refactor · test · docs · chore

Examples:
  feat: add employee leave balance endpoint
  fix: resolve N+1 query in department fetch
  test: add TestContainers integration test for payroll
  chore: update .gitignore for IntelliJ files

## Rules
- Never commit directly to main or develop
- One feature = one branch = one PR
- PR title must match commit message format
- All PRs merge into develop, never main directly
- main only receives merges from develop via release PR