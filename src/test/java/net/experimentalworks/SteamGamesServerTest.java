package net.experimentalworks;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.server.transport.StdioServerTransport;

class SteamGamesServerTest {

  private SteamApiConfig config;
  private StdioServerTransport transport;

  @BeforeEach
  void setUp() {
    // Create test configuration
    config = new SteamApiConfig("test-api-key", "12345678901234567", "test_");
    transport = new StdioServerTransport();
  }

  @Test
  void testServerCreation() {
    SteamGamesServer server = new SteamGamesServer(transport, config);
    assertNotNull(server);
  }

  @Test
  void testServerRunReturnsNonNullMono() {
    SteamGamesServer server = new SteamGamesServer(transport, config);
    assertNotNull(server.run());
  }
}
