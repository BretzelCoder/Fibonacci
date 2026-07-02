## Why

The Flet GUI (`fibonacci_flet.py`) currently discards every computation
once the next one starts: there is no way to review what values of `n`
were tried, compare past runs, or see how timings evolved across a
session. A persisted usage history turns each session into a small,
reusable dataset for the project's caching/performance learning goal,
and gives the user a way to revisit earlier results without recomputing.

## What Changes

- Add a `HistoryEntry` record capturing, for each GUI computation:
  timestamp, requested `n`, whether the recursive algorithm was included
  (and whether that inclusion/exclusion was the user's explicit choice
  or a forced skip due to `n > MAX_N_NAIVE`), the Fibonacci result,
  whether all computed algorithms agreed (`all_match`), the fastest
  algorithm name and its time, and the standard deviation of the
  execution times across all algorithms actually computed in that run.
- Persist history entries to a local JSON file (`fibonacci_history.json`,
  gitignored) so the history survives across GUI restarts.
- Add a "History" button to the GUI that opens an on-demand dialog
  listing past entries (most recent first).
- No change to the CLI (`MyFibonacciTest.py`) or to the core algorithms.

## Capabilities

### New Capabilities
- `gui-usage-history`: recording, persisting, and displaying a history
  of Fibonacci computations performed through the Flet GUI.

### Modified Capabilities
(none — no existing spec-level behavior changes)

## Impact

- Affected files: `fibonacci_flet.py` (new History button + dialog,
  records an entry after each successful computation),
  `fibonacci_service.py` or a new `fibonacci_history.py` module (history
  entry model + JSON persistence), `.gitignore` (exclude the local
  history file).
- New runtime behavior: a small JSON file is written to the project
  directory during GUI use.
- No impact on the CLI or on algorithm correctness/performance.
