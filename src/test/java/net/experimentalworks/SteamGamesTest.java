package net.experimentalworks;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
