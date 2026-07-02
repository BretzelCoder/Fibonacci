## Context

`fibonacci_flet.py` computes a `ComputeResponse` via `compute_all()` on
every "Compute" click but discards it once the UI updates — nothing is
recorded across calls. There is no persistence layer anywhere in the
project today (no database, no config file, no existing JSON I/O). The
GUI is explicitly a single-user desktop POC ("validating the
architecture before migrating to FastAPI + React" per its module
docstring), so the persistence approach should stay proportionate to
that scope.

`ComputeResponse.results` already carries everything needed to derive a
history entry: each `AlgoResult` has `skipped` / `skip_reason`, so the
distinction the user asked for — recursive excluded by explicit user
choice ("disabled by user") vs. forced skip (`n > MAX_N_NAIVE`, i.e. the
"O(2^n) cost" reason) — is already encoded in the existing
`skip_reason` string and does not need a new field to disambiguate.

## Goals / Non-Goals

**Goals:**
- Record one history entry per successful GUI computation: timestamp,
  `n`, recursive inclusion state (computed / user-disabled / forced-skip,
  derived from the existing `skip_reason`), the Fibonacci result,
  `all_match`, fastest algorithm + its time, and the population standard
  deviation of execution times across the algorithms actually computed.
- Persist entries to a local JSON file so history survives GUI restarts.
- Expose history via an on-demand "History" dialog, most recent first.

**Non-Goals:**
- No history for the CLI (`MyFibonacciTest.py`) — GUI only, per the
  proposal's scope.
- No size cap / retention policy on the history file in this iteration
  (see Risks).
- No concurrent-writer protection — single local user, single GUI
  instance assumed, consistent with the project's current POC scope.
- No change to `fibonacci_service.compute_all`'s signature or behavior.

## Decisions

- **New module `fibonacci_history.py`** holding a `HistoryEntry`
  dataclass and `load_history()` / `append_entry()` functions, mirroring
  the existing `fibonacci_algorithms.py` / `fibonacci_service.py` split
  (pure data + I/O, kept separate from UI code in `fibonacci_flet.py`).
  Alternative considered: put persistence directly in `fibonacci_flet.py`
  — rejected because it would mix UI and I/O concerns and make the
  history logic untestable without a running Flet page.
- **Storage format: a single JSON array file, `fibonacci_history.json`,
  at the repo root, gitignored.** Alternative considered: SQLite —
  rejected as disproportionate for a learning-project-scale, single-user
  history list; JSON keeps the file human-readable, which fits the
  project's educational intent. Alternative considered: CSV — rejected
  because entries have optional/nested-feeling fields (e.g. skip
  reasons) that JSON represents more naturally than flat CSV columns.
- **Whole-file read-modify-write on each entry**: `append_entry()` loads
  the existing list, appends, and writes the full array back (via a
  temp-file + `os.replace` swap for atomicity). Alternative considered:
  append-only line-delimited JSON (JSONL) — rejected because a plain
  JSON array is simpler to load as a whole for the history dialog, and
  entry volume from manual GUI clicks is far too low for
  read-modify-write overhead to matter.
- **`HistoryEntry.timestamp` stored as an ISO-8601 string** (via
  `datetime.now().isoformat(timespec="seconds")`), not a `datetime`
  object, so the dataclass round-trips through `json.dump`/`json.load`
  with no custom encoder/decoder.
- **Standard deviation via `statistics.pstdev`** (population stdev) over
  the `time_s` of non-skipped results. Chosen over `statistics.stdev`
  (sample stdev) because the computed algorithms in a single run are the
  entire population of interest for that entry, not a sample; `pstdev`
  is also defined for exactly 1 data point (returns 0.0), avoiding an
  edge case `stdev` would raise on.
- **Recording point**: inside `on_compute`'s background `run()` in
  `fibonacci_flet.py`, right after a successful `compute_all()` call,
  wrapped so a history-write failure (e.g. disk full) is caught and
  surfaced as a non-blocking snackbar rather than losing the computed
  results already shown to the user.
- **History dialog reloads from disk on each open** (calls
  `load_history()` fresh) rather than keeping an in-memory cache on the
  page — guarantees the dialog reflects the persisted file exactly, and
  the read cost is negligible at this scale.

## Risks / Trade-offs

- [History file grows unbounded over a long session] → Mitigation:
  acceptable at this project's scale (manual GUI clicks, JSON array of
  small records); revisit with a max-entries cap only if it becomes a
  real problem.
- [Crash mid-write corrupts the JSON file] → Mitigation: write to a
  `.tmp` file and `os.replace()` it over the real file, so a crash never
  leaves a partially-written file in place.
- [Very large `n` produces a very large integer result] → Mitigation:
  bounded by the existing `MAX_N = 5000` guard in `compute_all`; Python's
  `json` module serializes arbitrary-precision ints natively, so no
  truncation risk within that bound.
- [Multiple GUI instances writing concurrently could race] → Mitigation:
  out of scope per Non-Goals; documented here as a known limitation of
  the single-user POC.

## Migration Plan

Additive only. `fibonacci_history.json` is created on first successful
GUI computation if absent; no existing file/format to migrate. Rollback
is reverting the change's commits; the gitignored JSON file (if created
locally) can simply be deleted.
