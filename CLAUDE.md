# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Model Context Protocol (MCP) server implementation in Java that exposes Steam gaming data through MCP tools. The server runs as a stdio-based service that AI assistants can connect to for accessing Steam user gaming information.

## Architecture

### Core Components

1. **App.java** - Entry point that:
   - Loads and validates configuration from environment variables via `SteamApiConfig`
   - Creates `SteamGamesServer` with stdio transport and configuration
   - Provides fail-fast error handling for configuration issues
   - Uses proper exit codes on failures

2. **SteamApiConfig.java** - Configuration management class that:
   - Loads environment variables with validation
   - Provides fail-fast validation for required configuration (STEAM_API_KEY, STEAM_ID)
   - Supports optional TOOL_PREFIX with default empty string
   - Can be instantiated with explicit values for testing

3. **SteamGamesServer.java** - MCP server implementation that:
   - Registers two MCP tools: `get-games` and `get-recent-games`
   - Handles tool invocations asynchronously using Project Reactor (Mono)
   - Uses dependency injection for `SteamGames` and `SteamApiConfig`
   - Implements proper error handling with user-friendly error messages
   - Uses `subscribeOn(Schedulers.boundedElastic())` for blocking Steam API calls
   - Returns errors as `CallToolResult` with isError flag set
   - Uses MCP SDK version 0.7.0 (io.modelcontextprotocol.sdk)

4. **SteamGames.java** - Steam API client wrapper that:
   - Wraps steam-web-api library
   - Validates API key and Steam ID on construction/invocation
   - Includes Steam ID format validation (numeric, up to 17 digits)
   - Provides null-safe handling of Steam API responses
   - Returns empty lists for null/invalid responses instead of throwing NPE

5. **Game.java** - Immutable data model for game information:
   - Contains appId, name, playtime (forever)
   - Optional playtime2weeks for recently played games
   - Properly initializes Optional.empty() to avoid null
   - Includes serialVersionUID for safe serialization

### Key Dependencies

**Production:**
- **MCP SDK** (io.modelcontextprotocol.sdk:mcp:0.7.0) - Model Context Protocol implementation
- **Project Reactor** (3.7.3) - Reactive programming for async operations
- **steam-web-api** (1.9.1) - Steam API client by lukaspradel
- **org.json** (20250107) - JSON serialization

**Testing:**
- **JUnit Jupiter** (5.11.3) - Modern unit testing framework
- **Mockito** (5.14.2) - Mocking framework for unit tests
- **Reactor Test** (3.7.3) - Testing utilities for reactive code

### MCP Tools

Both tools return playtime in **minutes** (not hours):

1. **get-games** (or `{TOOL_PREFIX}get-games`)
   - Returns all owned games with total playtime
   - Uses `GetOwnedGamesRequest` from Steam API
   - Returns JSON with `owner`, `description`, and `all_games` array

2. **get-recent-games** (or `{TOOL_PREFIX}get-recent-games`)
   - Returns games played in last 2 weeks
   - Uses `GetRecentlyPlayedGamesRequest` from Steam API
   - Returns JSON with `owner`, `description`, and `recent_games` array

## Development Commands

### Build
```bash
mvn package
```
This creates a shaded JAR with all dependencies at `target/mcp-steam-1.0-SNAPSHOT.jar`.

### Code Formatting
```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply
```
Uses Google Java Format style with specific import ordering.

### Run Tests
```bash
mvn test
```

### Run Locally
```bash
# Required environment variables
export STEAM_API_KEY=your_api_key
export STEAM_ID=your_steam_id

# Optional
export TOOL_PREFIX=steam_

java -jar target/mcp-steam-1.0-SNAPSHOT.jar
```

### Native Packaging (Self-contained executable with embedded JRE)
```bash
# Build native application image
mvn package jpackage:jpackage

# Output location
target/dist/mcp-server-steam/bin/mcp-server-steam  # Native executable
target/dist/mcp-server-steam/lib/                  # Bundled JRE and JARs

# Run the native executable (Linux/macOS)
export STEAM_API_KEY=your_api_key
export STEAM_ID=your_steam_id
export TOOL_PREFIX=steam_  # Optional
target/dist/mcp-server-steam/bin/mcp-server-steam

# Run the native executable (Windows)
set STEAM_API_KEY=your_api_key
set STEAM_ID=your_steam_id
set TOOL_PREFIX=steam_
target\dist\mcp-server-steam\bin\mcp-server-steam.exe
```

**Notes:**
- Native packaging requires Java 21+ JDK with jpackage tool
- Creates self-contained application with embedded JRE - end users don't need Java installed
- Works on Linux, macOS, and Windows (build on target platform)
- Uses jpackage-maven-plugin (org.panteleyev:jpackage-maven-plugin:1.6.5)
- Output directory: `target/dist/mcp-server-steam/`
- For Windows installers, change `type` from `APP_IMAGE` to `MSI` or `EXE` in pom.xml

### Docker (Optional Deployment Method)
```bash
# Build
docker build -t mcp-server-steam .

# Run
docker run --rm -i \
  -e STEAM_API_KEY=your_key \
  -e STEAM_ID=your_id \
  ghcr.io/dsp/mcp-server-steam:latest
```

### Development Environment
The project uses devenv.sh for development environment management:
```bash
devenv shell
```

## Configuration

Environment variables (managed by `SteamApiConfig.java`):
- `STEAM_API_KEY` (required) - Steam Web API key
- `STEAM_ID` (required) - Steam user ID to query (must be numeric, up to 17 digits)
- `TOOL_PREFIX` (optional, default: "") - Prefix for tool names

The application validates these on startup and exits with error code 1 if required variables are missing or invalid.

## Important Implementation Details

### Reactive Programming
- Server uses **stdio transport** (`StdioServerTransport`) for MCP communication
- All tool handlers return `Mono<CallToolResult>` for async processing
- Blocking Steam API calls use `subscribeOn(Schedulers.boundedElastic())` to avoid blocking reactor threads
- Error handling uses `onErrorResume()` to convert exceptions to error responses
- Server runs with `Mono.never()` to keep it alive indefinitely

### Error Handling
- Configuration errors fail fast on startup with clear error messages
- Steam API errors are caught and returned as `CallToolResult` with `isError=true`
- Input validation prevents invalid Steam IDs from reaching the API
- Null-safe response handling prevents NPE from unexpected API responses

### Dependency Injection
- `SteamGamesServer` receives `SteamApiConfig` and creates a single `SteamGames` instance
- `SteamGames` instance is reused across all tool invocations (not created per request)
- Configuration is injected for testability

### Testing
- All core classes have unit tests (GameTest, SteamGamesTest, SteamApiConfigTest, SteamGamesServerTest)
- Tests use JUnit 5 with modern assertions and annotations
- Mockito available for mocking dependencies

### Build
- Version is read from JAR manifest; defaults to "1.0.0" if unavailable
- Maven Shade plugin creates uber-jar with main class `net.experimentalworks.App`
- Java compiler target is Java 21 (pom.xml:11)
- Maven Surefire plugin (3.5.2) configured for JUnit 5 support

## Code Style

- Uses Spotless plugin with Google Java Format
- Import order: java, javax, org, com
- Trim trailing whitespace and end files with newline
