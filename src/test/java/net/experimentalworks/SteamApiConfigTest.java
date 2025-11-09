package net.experimentalworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SteamApiConfigTest {

  @Test
  void testConfigWithValidParameters() {
    SteamApiConfig config = new SteamApiConfig("test-api-key", "12345678901234567", "steam_");

    assertEquals("test-api-key", config.getSteamApiKey());
    assertEquals("12345678901234567", config.getSteamId());
    assertEquals("steam_", config.getToolPrefix());
  }

  @Test
  void testConfigWithEmptyToolPrefix() {
    SteamApiConfig config = new SteamApiConfig("test-api-key", "12345678901234567", "");

    assertEquals("", config.getToolPrefix());
  }

  @Test
  void testConfigWithNullToolPrefix() {
    SteamApiConfig config = new SteamApiConfig("test-api-key", "12345678901234567", null);

    assertEquals("", config.getToolPrefix());
  }

  @Test
  void testConfigWithNullApiKey() {
    assertThrows(
        IllegalArgumentException.class, () -> new SteamApiConfig(null, "12345678901234567", ""));
  }

  @Test
  void testConfigWithBlankApiKey() {
    assertThrows(
        IllegalArgumentException.class, () -> new SteamApiConfig("  ", "12345678901234567", ""));
  }

  @Test
  void testConfigWithNullSteamId() {
    assertThrows(
        IllegalArgumentException.class, () -> new SteamApiConfig("test-api-key", null, ""));
  }

  @Test
  void testConfigWithBlankSteamId() {
    assertThrows(
        IllegalArgumentException.class, () -> new SteamApiConfig("test-api-key", "  ", ""));
  }
}
