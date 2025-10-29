# Version 0.1.3

## New Features

### Admin Management Commands
- Added comprehensive `/omniecon admin` command suite for server administrators
- **Available Admin Commands**:
  - `/omniecon admin give <player> <amount>` - Give money to a player
  - `/omniecon admin take <player> <amount>` - Remove money from a player's balance
  - `/omniecon admin set <player> <amount>` - Set a player's balance to a specific amount
  - `/omniecon admin balance <player>` - Check any player's balance
  - `/omniecon admin resetdaily <player>` - Reset a player's daily reward cooldown
  - `/omniecon admin resetplaytime <player>` - Reset a player's playtime earnings cap for the day
- **Permission System**: All admin commands require OP level 4
- **Feedback System**:
  - Admins receive confirmation messages when executing commands
  - Affected players receive notifications about balance changes
  - Other online operators are notified of admin actions

### Interactive Help System
- Added `/omniecon help` command accessible to all players
- **Smart Display**:
  - Shows all available player commands (based on config settings)
  - Displays admin commands only to operators
  - Automatically filters commands based on enabled features (lottery, daily, rewards, etc.)
- **Clickable Commands**:
  - Color-coded common categories (Player, Lottery, Admin, Telemetry)
  - Clean headers and separators for easy navigation
  - Shows current currency symbol in footer

## Improvements
- Enhanced user experience with interactive command discovery
- Streamlined admin workflow for server management
- Better permission handling for administrative functions

## Configuration
- Admin commands respect all existing feature toggles (daily rewards, lottery, telemetry, etc.)
- No new configuration options required - works with existing settings