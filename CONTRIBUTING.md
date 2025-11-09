# Contributing to MCP Steam Server

## Getting Your Environment Ready

### What You'll Need

- **Java 21+**
- **Maven 3.6+** - For building and managing dependencies
- **Git** - Obviously
- **Docker** - Only if you're working on container stuff
- **devenv.sh** - Optional, but makes life easier if you're into declarative dev environments

### Setting Up

**Option 1: Using devenv.sh (recommended if you have it)**
```bash
git clone https://github.com/dsp/mcp-steam.git
cd mcp-steam
devenv shell
```

**Option 2: The manual way**
```bash
git clone https://github.com/dsp/mcp-steam.git
cd mcp-steam
# Make sure Java 21+ and Maven are in your PATH
mvn package
```

## Working on the Code

### Project Structure

Here's where everything lives:

```
src/main/java/net/experimentalworks/
├── App.java                    # Entry point - sets up the server
├── SteamApiConfig.java         # Handles env vars and validation
├── SteamGamesServer.java       # The actual MCP server
├── SteamGames.java             # Wraps the Steam API client
└── Game.java                   # Simple data model for games

src/test/java/net/experimentalworks/
├── SteamApiConfigTest.java     # Config validation tests
├── SteamGamesTest.java         # Steam API wrapper tests
├── SteamGamesServerTest.java   # MCP server tests
└── GameTest.java               # Model tests
```

### The Stack

- **MCP SDK 0.7.0** - For implementing the Model Context Protocol
- **Project Reactor** - Makes async operations less painful
- **steam-web-api** (by lukaspradel) - Does the heavy lifting for Steam API calls
- **JUnit 5 + Mockito** - Testing framework

### Running Tests

Before you do anything, make sure tests pass:

```bash
mvn test
```

All tests should pass.

### Code Formatting

Spotless with Google Java Format.

```bash
# Check if your code is formatted correctly
mvn spotless:check

# Fix formatting automatically (do this before committing!)
mvn spotless:apply
```

Run `mvn spotless:apply` before every commit to avoid the CI yelling at you.

### Running Locally

To actually run the thing:

```bash
# Set up your environment
export STEAM_API_KEY=your_actual_steam_api_key
export STEAM_ID=your_actual_steam_id

# Build and run
mvn package
java -jar target/mcp-steam-1.0-SNAPSHOT.jar
```

The server communicates via stdio using the MCP protocol. You can test it by sending MCP messages to stdin, but honestly, it's easier to just configure it with Claude Desktop/whatever MCP client and test that way.

### Building Native Executables

If you're working on native builds:

```bash
mvn package jpackage:jpackage
```

This creates a self-contained app in `target/dist/mcp-server-steam/` with an embedded JRE.


### Docker Stuff

Building the container locally:

```bash
docker build -t mcp-server-steam .
```

Running it:
```bash
docker run --rm -i \
  -e STEAM_API_KEY=your_key \
  -e STEAM_ID=your_id \
  mcp-server-steam
```

## Making Changes

### The Workflow

1. **Fork the repo** and clone your fork
2. **Run & or write tests**: `mvn test`
3. **Format your code**: `mvn spotless:apply`
4. **Push and open a PR**


## Code Style Notes

- **Google Java Format**
- **Null safety** - Use `Optional` where appropriate, validate inputs early
- **Error handling** - Return user-friendly error messages, don't let exceptions bubble up
- **Async patterns** - Use `Mono.subscribeOn(Schedulers.boundedElastic())` for blocking calls
- **Tests** - Use descriptive test method names like `shouldReturnEmptyListWhenSteamApiReturnsNull()`

## Common Tasks

### Adding a New Tool

1. Add the tool definition in `SteamGamesServer.java` (see existing tools)
2. Implement the handler logic (probably involves calling `SteamGames`)
3. Add a new method to `SteamGames.java` if you need new Steam API functionality
4. Write tests in `SteamGamesServerTest.java` and `SteamGamesTest.java`
5. Update documentation (README.md and CLAUDE.md)

### Adding a New Configuration Option

1. Update `SteamApiConfig.java` to read the new env var
2. Add validation if needed
3. Update the constructor and add tests
4. Update documentation

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
