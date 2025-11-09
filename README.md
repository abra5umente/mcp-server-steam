# MCP Steam Server

A Model Context Protocol (MCP) server that connects AI assistants to the Steam gaming platform, enabling them to access and understand your Steam gaming library and activity.

## What It Does

This MCP server provides AI assistants (like Claude) with access to Steam user gaming data through two simple tools. When integrated with an AI assistant, it allows the assistant to answer questions about your gaming habits, recommend games based on your library, analyze your playtime patterns, and provide personalized gaming insights.

### Available Tools

The server exposes two MCP tools that query the Steam Web API:

**`get-games`**
- Retrieves all games owned by a Steam user
- Returns game names, App IDs, and total playtime (in minutes)
- Useful for: Library analysis, game recommendations, collection statistics

**`get-recent-games`**
- Retrieves games played in the last 2 weeks
- Returns game names, App IDs, total playtime, and recent playtime (in minutes)
- Useful for: Activity tracking, current gaming interests, time management insights

Both tools can optionally use a custom prefix (configured via `TOOL_PREFIX` environment variable).

### Example Use Cases

Once configured with your AI assistant, you can ask questions like:
- "What games have I been playing recently?"
- "How many hours have I spent in [game name]?"
- "Recommend a game from my library I haven't played much"
- "What's my most-played game?"
- "Which games in my library have I never launched?"

## How to Use It

### Prerequisites

To use this MCP server, you need:
- A Steam account and Steam API key ([get one here](https://steamcommunity.com/dev/apikey))
- Your Steam ID ([find yours here](https://steamid.io/))
- An MCP-compatible AI assistant (like [Claude Desktop](https://claude.ai/download))

### Installation Options

#### Option 1: Native Executable (Recommended)

The easiest way to run - no Java installation required:

1. Download the native application bundle for your platform from the [Releases](https://github.com/dsp/mcp-steam/releases) page
2. Extract the archive (includes an embedded Java runtime)
3. Configure your AI assistant to use the executable (see Claude Desktop setup below)

#### Option 2: Using JAR

If you have Java 21+ installed, you can download and run the JAR directly:

```bash
java -jar mcp-steam-1.0-SNAPSHOT.jar
```

#### Option 3: Using Docker

For containerized deployments:

```bash
docker run --rm -i \
  -e STEAM_API_KEY=your_api_key \
  -e STEAM_ID=your_steam_id \
  ghcr.io/dsp/mcp-server-steam:latest
```

### Configuration

The server requires these environment variables:

- **`STEAM_API_KEY`** (required) - Your Steam Web API key
- **`STEAM_ID`** (required) - Steam user ID to query (numeric, up to 17 digits)
- **`TOOL_PREFIX`** (optional) - Prefix for MCP tool names (default: empty string)

### Setting Up with Claude Desktop

1. **Get your Steam credentials:**
   - Steam API Key: [steamcommunity.com/dev/apikey](https://steamcommunity.com/dev/apikey)
   - Steam ID: [steamid.io](https://steamid.io/)

2. **Locate your Claude Desktop config file:**
   - **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - **Windows**: `%APPDATA%/Claude/claude_desktop_config.json`
   - **Linux**: `~/.config/Claude/claude_desktop_config.json`

   Or: Claude → Settings → Developer → Edit Config

3. **Add the MCP server configuration:**

   > **⚠️ Security Warning**: Your config file will contain your Steam API key. Never commit this file to version control or share it publicly.

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

   **For JAR:**
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

   **Windows Note:** Escape backslashes in paths: `C:\\\\Users\\\\...`

4. **Restart Claude Desktop**

5. **Verify it's working:**
   - Look for the 🔌 icon in Claude Desktop
   - Click it to see available MCP tools
   - You should see `get-games` and `get-recent-games`
   - Try asking Claude: "What games have I been playing recently?"

## Development

### Prerequisites

- Java 21+ JDK
- Maven 3.6+

### Quick Start

```bash
git clone https://github.com/dsp/mcp-steam.git
cd mcp-steam
mvn package
```

Run locally:
```bash
export STEAM_API_KEY=your_key
export STEAM_ID=your_id
java -jar target/mcp-steam-1.0-SNAPSHOT.jar
```

Build native executable:
```bash
mvn package jpackage:jpackage
```

### Tech Stack

- Java 21 with Project Reactor for async operations
- MCP SDK 0.7.0 for protocol implementation
- Steam Web API client by lukaspradel
- Maven for builds, JUnit 5 + Mockito for testing

For detailed development guidelines, see [CONTRIBUTING.md](CONTRIBUTING.md).

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes with tests
4. Ensure all tests pass (`mvn test`)
5. Apply code formatting (`mvn spotless:apply`)
6. Submit a Pull Request

## Resources

- [Model Context Protocol Documentation](https://modelcontextprotocol.io)
- [Steam Web API Documentation](https://steamcommunity.com/dev)
- [MCP Servers Repository](https://github.com/modelcontextprotocol/servers)

## License

MIT License - see [LICENSE](LICENSE) file for details.
