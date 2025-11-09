# MCP Steam Server

A Model Context Protocol (MCP) server that provides Steam gaming context to AI assistants. This service integrates with the Steam API to fetch user gaming information and exposes it through the MCP protocol, allowing AI assistants to access and understand users' gaming activities and preferences.

## Installation

### Option 1: Native Executable (Recommended)

The easiest way to run - no Java installation required:

1. Download the native application bundle for your platform from the [Releases](https://github.com/dsp/mcp-steam/releases) page
2. Extract the archive - it includes an embedded Java runtime
3. Set environment variables:
   ```bash
   # Linux/macOS:
   export STEAM_API_KEY=your_steam_api_key
   export STEAM_ID=your_steam_id

   # Windows:
   set STEAM_API_KEY=your_steam_api_key
   set STEAM_ID=your_steam_id
   ```
4. Run the executable:
   ```bash
   # Linux/macOS:
   ./mcp-server-steam/bin/mcp-server-steam

   # Windows:
   mcp-server-steam\bin\mcp-server-steam.exe
   ```

### Option 2: Using JAR (Cross-platform)

If you have Java 21+ installed:

```bash
# Download the JAR from releases
java -jar mcp-steam-1.0-SNAPSHOT.jar
```

**Required environment variables:**
```bash
# Linux/macOS:
export STEAM_API_KEY=your_steam_api_key
export STEAM_ID=your_steam_id
export TOOL_PREFIX=steam_  # Optional

# Windows:
set STEAM_API_KEY=your_steam_api_key
set STEAM_ID=your_steam_id
set TOOL_PREFIX=steam_
```

### Option 3: Using Docker

For containerized deployments:

```bash
docker run --rm -i \
  -e STEAM_API_KEY=your_api_key \
  -e STEAM_ID=your_steam_id \
  ghcr.io/dsp/mcp-server-steam:latest
```

### Configuration

The server requires these environment variables:

- **STEAM_API_KEY** (required) - Your Steam Web API key from [steamcommunity.com/dev/apikey](https://steamcommunity.com/dev/apikey)
- **STEAM_ID** (required) - Steam user ID to query (numeric, up to 17 digits)
- **TOOL_PREFIX** (optional) - Prefix for MCP tool names (default: empty string)

## Setting Up with Claude Desktop

To use this MCP server with Claude Desktop:

1. **Get your Steam credentials:**
   - Steam API Key: [steamcommunity.com/dev/apikey](https://steamcommunity.com/dev/apikey)
   - Steam ID: Find yours at [steamid.io](https://steamid.io/)

2. **Locate your Claude Desktop config file:**
   - **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - **Windows**: `%APPDATA%/Claude/claude_desktop_config.json`
   - **Linux**: `~/.config/Claude/claude_desktop_config.json`

   Or open via: Claude → Settings → Developer → Edit Config

3. **Add the MCP server configuration:**

   > **⚠️ Security Warning**: Your `claude_desktop_config.json` file will contain your Steam API key. Never commit this file to version control or share it publicly. Consider using environment-specific configuration files and add `claude_desktop_config.json` to your `.gitignore` if you're working in a repository.

   **For Native Executable (Recommended):**
   ```json
   {
     "mcpServers": {
       "steam": {
         "command": "/absolute/path/to/mcp-server-steam/bin/mcp-server-steam",
         "env": {
           "STEAM_API_KEY": "your_steam_api_key_here",
           "STEAM_ID": "your_steam_id_here"
         }
       }
     }
   }
   ```

   **For JAR (if you have Java installed):**
   ```json
   {
     "mcpServers": {
       "steam": {
         "command": "java",
         "args": ["-jar", "/absolute/path/to/mcp-steam-1.0-SNAPSHOT.jar"],
         "env": {
           "STEAM_API_KEY": "your_steam_api_key_here",
           "STEAM_ID": "your_steam_id_here"
         }
       }
     }
   }
   ```

   **Windows Path Example (note the escaped backslashes):**
   ```json
   "command": "C:\\\\Users\\\\YourName\\\\Downloads\\\\mcp-server-steam\\\\bin\\\\mcp-server-steam.exe"
   ```

4. **Restart Claude Desktop**

5. **Verify it's working:**
   - Look for the 🔌 icon in Claude Desktop
   - Click it to see available MCP tools
   - You should see `get-games` and `get-recent-games`

### Available MCP Tools

Once configured, Claude can access these tools:

- **get-games** - Retrieve all owned games with total playtime
- **get-recent-games** - Retrieve games played in the last 2 weeks

## Development

### Prerequisites

- OpenJDK 21
- Docker (for container builds)
- Git
- [devenv.sh](https://devenv.sh)

### Setting Up Development Environment

1. Clone the repository:
   ```bash
   git clone https://github.com/dsp/mcp-steam.git
   cd mcp-steam
   ```

2. Use the development shell:
   ```bash
   devshell shell
   ```
   This will set up the required development environment with all necessary dependencies.

3. Build the project:
   ```bash
   mvn package
   ```

### Building Native Executable

To create a native application bundle with embedded JRE:

```bash
# Requires Java 21+ JDK with jpackage tool
mvn package jpackage:jpackage

# Output will be in:
# target/dist/mcp-server-steam/bin/mcp-server-steam (executable)
# target/dist/mcp-server-steam/lib/ (bundled JRE and libraries)
```

### Building Docker Image Locally

```bash
docker build -t mcp-server-steam .
```

## API Documentation

The server implements the Model Context Protocol (MCP) specification. For detailed API documentation, please refer to the [MCP Documentation](https://modelcontextprotocol.io).

## Contributing

Contributions are welcome! Please feel free to submit a PR.

## License

MIT License
