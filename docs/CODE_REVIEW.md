# Code Review: docker-kotlin

**Date:** 2026-01-11
**Reviewer:** Claude (Opus 4.5)
**Version:** 0.1.0-SNAPSHOT

---

## Overview

This is a JVM port of a Rust docker-wrapper project - a type-safe Docker CLI wrapper for Kotlin/JVM. It wraps the Docker CLI (not the HTTP API) to provide idiomatic Kotlin DSLs and Java builder patterns for programmatic Docker container and image management.

**Key Philosophy:** Uses the CLI rather than the Docker HTTP API to avoid socket/TLS configuration complexity, achieve better cross-platform compatibility (Docker Desktop, Podman, Colima, OrbStack), and provide full Docker Compose support.

---

## What's Really Good

### 1. Clean Architecture

- The `DockerCommand<T>` interface is well-designed with typed return values
- `AbstractDockerCommand` handles common concerns (timeout, raw args, preview)
- Clear separation: core → templates → compose → testing modules

### 2. API Ergonomics

- Fluent builders work well for both Kotlin DSL and Java
- `preview()` for dry-run debugging is a nice touch
- The `arg()`/`args()` escape hatch for unmapped flags is smart

### 3. Compose DSL

- Really comprehensive `ServiceSpec` - covers almost everything Docker Compose supports
- Properly handles both short and long syntax for volumes, networks, depends_on

### 4. Testing Module

- JUnit 5 `DockerExtension` is clean with proper lifecycle management
- `keepOnFailure` for debugging is thoughtful

---

## Concerns & Suggestions

### 1. Process I/O Blocking Issue

**File:** `CommandExecutor.kt:97-117`

```kotlin
val stdout = process.inputStream.bufferedReader().readText()
val stderr = process.errorStream.bufferedReader().readText()
val exitCode = process.waitFor()
```

This can deadlock on large output. If stdout buffer fills while stderr is waiting (or vice versa), the process blocks. You should read both streams concurrently:

```kotlin
// Consider using async readers or ProcessBuilder.redirectErrorStream(true)
// Or read with threads/coroutines in parallel
```

**Priority:** High

---

### 2. `executeRaw` vs `executeRawBlocking` Inconsistency

The async version (`executeProcess`) doesn't call `waitFor()` with timeout - it relies on the coroutine timeout wrapper, but the process keeps running if cancelled. The blocking version properly uses `destroyForcibly()`. Consider aligning behavior.

**Priority:** Medium

---

### 3. Missing Dynamic Port Discovery

**File:** `RedisTemplate.kt`

`RedisTemplate.getMappedPort()` returns the config value but never actually queries Docker for the mapped port when using `dynamicPort()`. You'd need to call `docker port <container>` or parse inspect output:

```kotlin
// After start, query actual mapping:
val portOutput = PortCommand(containerId.value, config.port).executeBlocking()
mappedPort = parsePort(portOutput)
```

**Priority:** High (breaks dynamic port use case)

---

### 4. Wait Strategies Not Fully Implemented

**File:** `Template.kt:316-329`

```kotlin
protected open suspend fun checkPortReady(port: Int): Boolean {
    return isRunning  // Just returns isRunning!
}
protected open suspend fun checkHttpReady(strategy: WaitStrategy.ForHttp): Boolean {
    return isRunning  // Same - no actual HTTP check
}
```

`ForPort` and `ForHttp` wait strategies fall back to just checking `isRunning`. Real implementations would need socket connection attempts or HTTP client calls.

**Priority:** High

---

### 5. `ExecCommand` Swallows Non-Zero Exit Codes

`ExecCommand` calls `executeRaw()` which throws on non-zero exit. But exec should return the exit code, not throw. The exec in Template does this correctly by catching. Consider:

```kotlin
// ExecCommand should handle exit codes differently than other commands
override suspend fun execute(): ExecOutput {
    val args = buildArgs() + rawArgs
    val output = executor.execute(args, commandTimeout) // Don't throw on non-zero
    return ExecOutput(output.stdout, output.stderr, output.exitCode)
}
```

**Priority:** Medium

---

### 6. Docker Class is Incomplete

**File:** `Docker.kt`

`Docker.kt` only wraps `run`, `stop`, `rm`, `pull`, `version`. The class suggests it's the main entry point but most commands require direct instantiation. Either:

- Complete it with all commands, or
- Rename/reposition it as a convenience helper

**Priority:** Low

---

### 7. Missing Docker CLI Commands

Compared to `docker --help`, the following command groups are not implemented:

- `docker context` commands
- `docker manifest` commands
- `docker plugin` commands
- `docker trust` commands
- `docker config` / `docker secret` (Swarm)
- `docker service` / `docker stack` / `docker node` (Swarm)
- `docker buildx` commands

Not necessarily all needed, but worth documenting what's in/out of scope.

**Priority:** Low (document scope)

---

### 8. No JSON Parsing for Structured Output

`InspectCommand`, `PsCommand`, `VersionCommand` etc. return raw strings. Consider typed results:

```kotlin
data class ContainerInfo(val id: String, val name: String, val state: State, ...)

class InspectCommand(...) : AbstractDockerCommand<ContainerInfo>(...) {
    // Parse JSON with kotlinx.serialization
}
```

At minimum, could offer both `.execute()` → String and `.executeTyped()` → parsed object.

**Priority:** Medium (quality of life improvement)

---

### 9. Tests Are Gated Behind ENV Var

```kotlin
@EnabledIfEnvironmentVariable(named = "DOCKER_TESTS_ENABLED", matches = "true")
```

Fine for CI, but the README should document this clearly.

**Priority:** Low

---

### 10. Template Cleanup Race Condition

`AbstractTemplate.close()` is synchronous but calls `executeBlocking()` inside a try-catch that swallows errors. If the container is in a weird state, this silently fails. Consider logging at WARN level (you do, good) but also consider a `forceClose()` or retry mechanism.

**Priority:** Low

---

## Missing RunCommand Options

Comparing to `docker run --help`:

| Flag | Status |
|------|--------|
| `--add-host` | ❌ Missing |
| `--blkio-weight` | ❌ Missing |
| `--cap-add/--cap-drop` | ❌ Missing |
| `--cgroup-parent` | ❌ Missing |
| `--cgroupns` | ❌ Missing |
| `--device` | ❌ Missing |
| `--dns`, `--dns-search` | ❌ Missing |
| `--gpus` | ❌ Missing |
| `--health-*` | ❌ Missing |
| `--ipc`, `--pid`, `--uts` | ❌ Missing |
| `--isolation` | ❌ Missing |
| `--log-driver`, `--log-opt` | ❌ Missing |
| `--mac-address` | ❌ Missing |
| `--mount` | ❌ Missing (only `--volume`) |
| `--oom-*` | ❌ Missing |
| `--read-only` | ❌ Missing |
| `--security-opt` | ❌ Missing |
| `--shm-size` | ❌ Missing |
| `--stop-signal`, `--stop-timeout` | ❌ Missing |
| `--sysctl` | ❌ Missing |
| `--tmpfs` | ❌ Missing |
| `--ulimit` | ❌ Missing |
| `--userns` | ❌ Missing |

---

## Minor Nits

1. **Inconsistent method naming:** `env()` vs `envs()` on ExecCommand, but `env()` with vararg on RunCommand

2. **`dryRun` in DockerConfig is unused** - it's declared but never checked

3. **`ContainerId.short()` assumes 12+ chars** - could fail on weird edge cases

4. **No connection pooling/reuse** - each command creates a new process. Fine for now, but worth noting

---

## Summary

| Aspect | Rating | Notes |
|--------|--------|-------|
| Architecture | ⭐⭐⭐⭐⭐ | Clean, extensible design |
| API Design | ⭐⭐⭐⭐ | Good Kotlin/Java interop, minor inconsistencies |
| Command Coverage | ⭐⭐⭐⭐ | Core commands solid, some options missing |
| Error Handling | ⭐⭐⭐ | Works but could be more nuanced |
| Testing | ⭐⭐⭐⭐ | Good unit tests, integration tests gated |
| Compose DSL | ⭐⭐⭐⭐⭐ | Excellent, very complete |
| Templates | ⭐⭐⭐ | Good foundation, wait strategies need work |

---

## Priority Fixes

1. **Process I/O deadlock potential** - can cause hangs with large output
2. **Wait strategy implementations** - ForPort/ForHttp don't actually check
3. **Dynamic port discovery** - breaks the dynamic port use case entirely
4. **ExecCommand exit code handling** - throws instead of returning exit code

---

## Conclusion

This is a well-architected, production-quality port with clean abstractions and comprehensive command coverage. The Compose DSL is particularly impressive. The main areas needing attention are the process I/O handling and completing the wait strategy implementations. Overall status: ~90% complete and ready for real-world use with the noted caveats.
