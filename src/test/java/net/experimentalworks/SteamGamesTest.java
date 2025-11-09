package net.experimentalworks;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SteamGamesTest {

  @Test
  void testConstructorWithValidApiKey() {
    SteamGames steamGames = new SteamGames("valid-api-key");
    assertNotNull(steamGames);
  }

  @Test
  void testConstructorWithNullApiKey() {
    assertThrows(IllegalArgumentException.class, () -> new SteamGames(null));
  }

  @Test
  void testConstructorWithBlankApiKey() {
    assertThrows(IllegalArgumentException.class, () -> new SteamGames("   "));
  }

  @Test
  void testValidateSteamIdWithNull() {
    SteamGames steamGames = new SteamGames("test-api-key");

    // getGames validates steamId internally
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> steamGames.getGames(null));
    assertTrue(exception.getMessage().contains("Steam ID cannot be null or blank"));
  }

  @Test
  void testValidateSteamIdWithBlank() {
    SteamGames steamGames = new SteamGames("test-api-key");

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> steamGames.getGames("   "));
    assertTrue(exception.getMessage().contains("Steam ID cannot be null or blank"));
  }

  @Test
  void testValidateSteamIdWithInvalidFormat() {
    SteamGames steamGames = new SteamGames("test-api-key");

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> steamGames.getGames("invalid-id"));
    assertTrue(exception.getMessage().contains("Invalid Steam ID format"));
  }

  @Test
  void testValidateSteamIdWithValidFormat() {
    SteamGames steamGames = new SteamGames("test-api-key");

    // Valid Steam ID format - should not throw on validation
    // Will throw SteamApiException due to invalid API key, but that's after validation
    try {
      steamGames.getGames("12345678901234567");
    } catch (IllegalArgumentException e) {
      // Should not get IllegalArgumentException for valid format
      throw new AssertionError("Valid Steam ID format should pass validation", e);
    } catch (Exception e) {
      // Other exceptions (like SteamApiException) are expected
      // We're only testing validation here
    }
  }

  @Test
  void testValidateSteamIdForRecentGames() {
    SteamGames steamGames = new SteamGames("test-api-key");

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> steamGames.getRecentlyGames("abc123"));
    assertTrue(exception.getMessage().contains("Invalid Steam ID format"));
  }
}
