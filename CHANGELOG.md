# Version 0.1.2

## New Features

### Anonymous Telemetry System
- Added optional anonymous telemetry to help track mod usage and improve future development
- Telemetry collects only:
    - Unique anonymous server ID (randomly generated UUID stored locally)
    - Mod version
    - Minecraft version
    - Server heartbeat (sent hourly when enabled)
- **Privacy First**:
    - Completely anonymous - no server names, player data, or identifying information
    - Can be disabled at any time via config (`enableTelemetry = false` in `omnieconomy-common.toml`)
    - Runs silently in the background with no console spam
    - Fails gracefully if offline or unreachable
- Server ID is stored locally in `omnieconomy_server_id.dat` in your world folder
- Helps developers understand active installations and version distribution

## Bug Fixes
- Fixed visual client bugs occurring on ATM

## Configuration
- New config option: `enableTelemetry` (default: true)
    - Set to `false` in `omnieconomy-common.toml` to completely disable telemetry