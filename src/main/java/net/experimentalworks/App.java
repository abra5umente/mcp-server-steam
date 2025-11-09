package net.experimentalworks;

import io.modelcontextprotocol.server.transport.StdioServerTransport;

/** Main entry point for the MCP Steam server. */
public class App {

  public static void main(String[] args) {
    try {
      // Load and validate configuration from environment variables
      SteamApiConfig config = new SteamApiConfig();

      // Create and start the server
      var server = new SteamGamesServer(new StdioServerTransport(), config);
      server.run().block();
    } catch (IllegalStateException e) {
      System.err.println("Configuration error: " + e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Failed to start server: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
