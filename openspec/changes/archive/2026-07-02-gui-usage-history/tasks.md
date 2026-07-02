## 1. History module (`fibonacci_history.py`)

- [x] 1.1 Define `HistoryEntry` dataclass: `timestamp: str`, `n: int`, `recursive_state: str` (`"computed"` / `"user_excluded"` / `"forced_skip"`), `recursive_skip_reason: Optional[str]`, `result: int`, `all_match: bool`, `fastest_name: str`, `fastest_time_s: float`, `stddev_time_s: float`
- [x] 1.2 Implement `build_entry(resp: ComputeResponse, include_naive_requested: bool) -> HistoryEntry`, deriving `recursive_state`/`recursive_skip_reason` from the `recursive` result's `skipped`/`skip_reason`, and `stddev_time_s` via `statistics.pstdev` over non-skipped `time_s` values
- [x] 1.3 Implement `load_history(path: Path = HISTORY_PATH) -> List[HistoryEntry]`, returning `[]` if the file does not exist
- [x] 1.4 Implement `append_entry(entry: HistoryEntry, path: Path = HISTORY_PATH) -> None` using a temp-file + `os.replace` write for atomicity
- [x] 1.5 Add `fibonacci_history.json` to `.gitignore`

## 2. GUI integration (`fibonacci_flet.py`)

- [x] 2.1 Add a "History" button next to the "Compute" button
- [x] 2.2 In `on_compute`'s `run()`, after a successful `compute_all()` call, build and append a history entry; catch and surface any write failure as a non-blocking snackbar without affecting the displayed results
- [x] 2.3 Implement a history dialog builder that calls `load_history()`, sorts most-recent-first, and renders each entry's timestamp, `n`, recursive state, result (shortened via the existing `_shorten` helper), `all_match`, fastest algorithm + time, and `stddev_time_s`
- [x] 2.4 Wire the "History" button's `on_click` to open the dialog; show an explicit "no history yet" message when the list is empty

## 3. Verification

- [x] 3.1 Manually run `python fibonacci_flet.py`, perform computations with recursive computed / user-excluded / forced-skipped, and confirm each case is recorded correctly — verified programmatically (module-level) and confirmed manually in the running GUI by the user
- [x] 3.2 Restart the GUI and confirm previously recorded entries still appear in the history dialog — confirmed manually by the user
- [x] 3.3 Delete `fibonacci_history.json` and confirm the GUI still opens and the history dialog shows "no history yet" without error — confirmed manually by the user
