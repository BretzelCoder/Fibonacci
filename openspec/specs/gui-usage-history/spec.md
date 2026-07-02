# gui-usage-history Specification

## Purpose
TBD - created by archiving change gui-usage-history. Update Purpose after archive.
## Requirements
### Requirement: A history entry is recorded for each successful GUI computation
The system SHALL record one history entry each time the Flet GUI
completes a `compute_all()` call without raising an exception, capturing
the timestamp, requested `n`, recursive-inclusion state, the computed
result, whether all computed algorithms agreed, the fastest algorithm
and its time, and the standard deviation of execution times across the
algorithms actually computed.

#### Scenario: Successful computation is recorded
- **WHEN** the user clicks "Compute" in the GUI with a valid `n` and the
  computation succeeds
- **THEN** a new history entry is appended containing the current
  timestamp, `n`, the result, `all_match`, the fastest algorithm's name
  and time, and the standard deviation of the computed algorithms' times

#### Scenario: Failed computation is not recorded
- **WHEN** `compute_all()` raises an exception during a GUI computation
- **THEN** no history entry is recorded for that attempt

### Requirement: Recursive inclusion state distinguishes user choice from forced skip
Each history entry SHALL indicate whether the recursive algorithm was
computed, excluded by the user's explicit choice (the "Include
Recursive" checkbox was unchecked), or force-skipped because `n`
exceeded `MAX_N_NAIVE`.

#### Scenario: User explicitly excludes recursive
- **WHEN** the user unchecks "Include Recursive" and clicks "Compute"
- **THEN** the recorded entry marks recursive as excluded by user choice

#### Scenario: Recursive force-skipped for large n
- **WHEN** the user checks "Include Recursive", enters `n` greater than
  `MAX_N_NAIVE`, and clicks "Compute"
- **THEN** the recorded entry marks recursive as force-skipped due to
  the `n > MAX_N_NAIVE` guard, distinct from an explicit user exclusion

#### Scenario: Recursive computed normally
- **WHEN** the user checks "Include Recursive", enters `n` within
  `MAX_N_NAIVE`, and clicks "Compute"
- **THEN** the recorded entry marks recursive as computed, with no skip
  reason

### Requirement: History persists across GUI restarts
The system SHALL persist history entries to a local JSON file so that
entries recorded in a previous GUI session are available in a later
session.

#### Scenario: History survives a restart
- **WHEN** the GUI is closed after recording at least one history entry
  and reopened later
- **THEN** the previously recorded entries are available when the
  history is viewed

#### Scenario: No history file yet
- **WHEN** the GUI is opened for the first time and no history file
  exists
- **THEN** the history is treated as empty rather than raising an error

### Requirement: History is viewable on demand in the GUI
The system SHALL provide a "History" control in the GUI that, when
activated, displays the recorded history entries in a dialog, most
recent first.

#### Scenario: Opening the history dialog
- **WHEN** the user activates the "History" control
- **THEN** a dialog opens listing all recorded entries ordered from most
  recent to oldest, showing for each: timestamp, `n`, recursive
  inclusion state, result, whether all algorithms agreed, fastest
  algorithm and its time, and the standard deviation of computed times

#### Scenario: Empty history
- **WHEN** the user activates the "History" control before any
  computation has been recorded
- **THEN** the dialog opens showing that no history is available yet

