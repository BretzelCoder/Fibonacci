"""
Persisted history of Fibonacci computations run through the Flet GUI.

Stores one JSON array (fibonacci_history.json, gitignored) at the repo
root; each entry summarizes a single compute_all() call.
"""

import json
import os
import statistics
from dataclasses import asdict, dataclass
from datetime import datetime
from pathlib import Path
from typing import List, Optional

from fibonacci_service import ComputeResponse

HISTORY_PATH: Path = Path(__file__).parent / "fibonacci_history.json"


@dataclass
class HistoryEntry:
    """One recorded GUI computation."""
    timestamp: str
    n: int
    recursive_state: str  # "computed" | "user_excluded" | "forced_skip"
    recursive_skip_reason: Optional[str]
    result: int
    all_match: bool
    fastest_name: str
    fastest_time_s: float
    stddev_time_s: float


def build_entry(resp: ComputeResponse, include_naive_requested: bool) -> HistoryEntry:
    """Derive a HistoryEntry from a ComputeResponse and the GUI's checkbox state."""
    recursive = next(r for r in resp.results if r.name == "recursive")
    if not recursive.skipped:
        recursive_state = "computed"
    elif not include_naive_requested:
        recursive_state = "user_excluded"
    else:
        recursive_state = "forced_skip"

    computed = [r for r in resp.results if not r.skipped]
    result = computed[0].result if computed else None
    assert result is not None

    return HistoryEntry(
        timestamp=datetime.now().isoformat(timespec="seconds"),
        n=resp.n,
        recursive_state=recursive_state,
        recursive_skip_reason=recursive.skip_reason if recursive.skipped else None,
        result=result,
        all_match=resp.all_match,
        fastest_name=resp.best_name,
        fastest_time_s=resp.best_time,
        stddev_time_s=statistics.pstdev(r.time_s for r in computed),
    )


def load_history(path: Path = HISTORY_PATH) -> List[HistoryEntry]:
    """Load recorded history entries, most-recent-last (file order)."""
    if not path.exists():
        return []
    with path.open("r", encoding="utf-8") as f:
        raw = json.load(f)
    return [HistoryEntry(**entry) for entry in raw]


def append_entry(entry: HistoryEntry, path: Path = HISTORY_PATH) -> None:
    """Append a history entry, writing the file atomically."""
    entries = load_history(path)
    entries.append(entry)

    tmp_path = path.with_suffix(".tmp")
    with tmp_path.open("w", encoding="utf-8") as f:
        json.dump([asdict(e) for e in entries], f, indent=2)
    os.replace(tmp_path, path)
