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

  @Test
  void testConfigWithNumericSteamId() {
    // SteamID64 format (17 digits) should work
    SteamApiConfig config = new SteamApiConfig("test-api-key", "76561198012345678", "");
    assertEquals("76561198012345678", config.getSteamId());
  }

  @Test
  void testConfigWithVanityUrlSteamId() {
    // Custom vanity URL format should work
    SteamApiConfig config = new SteamApiConfig("test-api-key", "my_custom_url", "");
    assertEquals("my_custom_url", config.getSteamId());
  }

  @Test
  void testConfigWithTooLongSteamId() {
    // 33+ characters exceeds Steam's maximum for custom URLs
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new SteamApiConfig("test-api-key", "a".repeat(33), ""));
    assertEquals(
        "Invalid Steam ID format. Steam IDs should be at most 32 characters.",
        exception.getMessage());
  }
}
