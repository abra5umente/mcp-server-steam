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
  void testConfigWithInvalidSteamIdFormat() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new SteamApiConfig("test-api-key", "invalid-id", ""));
    assertEquals(
        "Invalid Steam ID format. Steam IDs should be numeric and up to 17 digits.",
        exception.getMessage());
  }

  @Test
  void testConfigWithTooLongSteamId() {
    // 18 digits - exceeds maximum of 17
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new SteamApiConfig("test-api-key", "123456789012345678", ""));
    assertEquals(
        "Invalid Steam ID format. Steam IDs should be numeric and up to 17 digits.",
        exception.getMessage());
  }

  @Test
  void testConfigWithAlphanumericSteamId() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new SteamApiConfig("test-api-key", "abc123def456", ""));
    assertEquals(
        "Invalid Steam ID format. Steam IDs should be numeric and up to 17 digits.",
        exception.getMessage());
  }
}
