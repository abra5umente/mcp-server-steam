package net.experimentalworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class GameTest {

  @Test
  void testGameConstructorWithoutRecentPlaytime() {
    Game game = new Game(123456L, "Test Game", 150.5f);

    assertEquals(123456L, game.getAppId());
    assertEquals("Test Game", game.getName());
    assertEquals(150.5f, game.getPlaytimeForever());
    assertTrue(game.getPlaytime2weeks().isEmpty());
  }

  @Test
  void testGameConstructorWithRecentPlaytime() {
    Game game = new Game(123456L, "Test Game", 150.5f, 25.0f);

    assertEquals(123456L, game.getAppId());
    assertEquals("Test Game", game.getName());
    assertEquals(150.5f, game.getPlaytimeForever());
    assertEquals(Optional.of(25.0f), game.getPlaytime2weeks());
  }

  @Test
  void testGameWithZeroPlaytime() {
    Game game = new Game(999L, "Unplayed Game", 0.0f);

    assertEquals(999L, game.getAppId());
    assertEquals("Unplayed Game", game.getName());
    assertEquals(0.0f, game.getPlaytimeForever());
    assertTrue(game.getPlaytime2weeks().isEmpty());
  }
}
