# [OPEN] m3g-runtime-stall

## Symptom

- Running a new J2ME 3D game sometimes stalls for about 10 seconds.
- Need runtime evidence before changing logic.

## Hypotheses

1. The render thread blocks on audio or media initialization fallback.
2. The render loop enters a slow path in M3G software rasterization for certain frames.
3. The UI/event thread waits on synchronized repaint or target binding contention.
4. Resource loading or image/model decode performs a blocking read on the main/game thread.
5. A forced sleep, retry loop, or timeout path is hit during game startup or scene transitions.

## Plan

1. Add minimal instrumentation around repaint, render, media, and resource load paths.
2. Reproduce the stall and collect timestamps.
3. Compare normal frames and stalled intervals.
4. Implement the smallest fix only after evidence points to one root cause.

## Iteration Notes

- First and second reproductions did not generate `trae-debug-log-m3g-runtime-stall.ndjson`.
- Health check on the Debug Server showed `log_count = 0`, so the issue was in the reporting path rather than missing reproduction.
- Root cause for missing evidence: debug reporting depended on a relative `.dbg` path and a single fallback port, which did not match the actual runtime working directory / active server port.
- Instrumentation transport was hardened to read both absolute and relative env paths and to retry `7779`, `7778`, and `7777`.
- Rebuilt both `freej2me` and `freej2meOnMinijvm` so the next reproduction should finally emit logs.
- A later reproduction still showed only old sub-second events in `trae-debug-log-m3g-runtime-stall.ndjson`, while the user observed an approximately 10-second freeze.
- This means the current end-of-scope logging strategy is insufficient for hangs where the process is terminated before the `finally` block can report.
- Next iteration focuses on in-flight watchdog instrumentation: emit begin markers and delayed "still running" events for repaint/render/flush paths so a hang is visible before scope exit.

## Evidence

| ID | Hypothesis | Status | Evidence Summary |
|----|------------|--------|------------------|
| H1 | The stall happens inside `Canvas.serviceRepaints()` but previous logging missed it because logging happened only after scope end | ✅ Confirmed | Watchdog logs reported `Canvas.serviceRepaints.watch` still running for about `15623ms` in stage `paint`, and the matching end event completed around `15672ms`. |
| H2 | The stall is in `flushGraphics()` / window presentation rather than game paint logic | ❌ Rejected | The same long frame reported `paintMs = 15671` and `flushMs = 1`, so presentation is not the bottleneck. |
| H3 | The stall is primarily caused by CPU-heavy per-vertex work in the minijvm GL renderer | ✅ Confirmed | `ProfileServlet` snapshots show stall intervals dominated by `MiniJvmGraphics3DFactory$GlRenderer.buildTriangleData`, `appendVertex`, `resolveVertexColor`, `applyLighting`, and `VertexArray.getComponentAsFloat`. |
| H4 | The stall is mainly blocking I/O or sleeping | ❌ Rejected | `SocketNative.readByte`, `Object.wait`, and `Thread.sleep` appear in profiles but match background/network/idle behavior; the render-thread evidence points to CPU time in the GL renderer. |
| H5 | The main hotspot is duplicate per-triangle recomputation of shared vertex data | ✅ Confirmed | `buildTriangleData()` recalculates color, lighting, positions, and texture coordinates for every triangle vertex, even across strip-adjacent triangles sharing the same indices. |

## Root Cause

- The visible 10+ second stall is a CPU-bound render stall, not a present/flush stall.
- On the minijvm path, `Canvas.paint(...)` enters the M3G GL renderer and spends most of the long frame in `MiniJvmGraphics3DFactory$GlRenderer.buildTriangleData(...)`.
- That method expands triangle strips into a flat triangle list and, for every triangle, repeatedly calls:
  - `resolveVertexColor(...)`
  - `applyLighting(...)`
  - `appendVertex(...)`
  - `VertexArray.getComponentAsFloat(...)`
- Shared strip vertices are recomputed again and again, so vertex fetch, lighting, and texture coordinate work scale with expanded triangle vertices instead of unique source vertices.
