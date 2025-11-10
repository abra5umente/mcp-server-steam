package net.experimentalworks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.lukaspradel.steamapi.core.exception.SteamApiException;

class SteamAppSearchTest {

  @Mock private SteamGames mockSteamGames;

  private SteamAppSearch appSearch;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    appSearch = new SteamAppSearch(mockSteamGames);
  }

  @Test
  void testSearchAppsExactMatch() throws SteamApiException {
    // Mock app list
    List<AppInfo> mockApps =
        List.of(
            new AppInfo(10, "Counter-Strike"),
            new AppInfo(20, "Team Fortress Classic"),
            new AppInfo(240, "Counter-Strike: Source"));

    when(mockSteamGames.getAppList()).thenReturn(mockApps);

    List<AppSearchResult> results = appSearch.searchApps("Counter-Strike", 5);

    assertFalse(results.isEmpty());
    assertEquals("Counter-Strike", results.get(0).getName());
    assertEquals(10, results.get(0).getAppId());
    assertEquals(1.0, results.get(0).getScore(), 0.01); // Exact match score
  }

  @Test
  void testSearchAppsPartialMatch() throws SteamApiException {
    List<AppInfo> mockApps =
        List.of(
            new AppInfo(10, "Counter-Strike"),
            new AppInfo(20, "Team Fortress Classic"),
            new AppInfo(440, "Team Fortress 2"));

    when(mockSteamGames.getAppList()).thenReturn(mockApps);

    List<AppSearchResult> results = appSearch.searchApps("counter", 5);

    assertFalse(results.isEmpty());
    // Should find Counter-Strike as it contains "counter"
    assertTrue(results.stream().anyMatch(r -> r.getName().equals("Counter-Strike")));
  }

  @Test
  void testSearchAppsFuzzyMatch() throws SteamApiException {
    List<AppInfo> mockApps =
        List.of(
            new AppInfo(10, "Counter-Strike"),
            new AppInfo(20, "Team Fortress Classic"),
            new AppInfo(440, "Team Fortress 2"));

    when(mockSteamGames.getAppList()).thenReturn(mockApps);

    // Typo in search query
    List<AppSearchResult> results = appSearch.searchApps("counter strike", 5);

    assertFalse(results.isEmpty());
    // Should still find Counter-Strike despite the space
    assertTrue(results.stream().anyMatch(r -> r.getName().contains("Counter-Strike")));
  }

  @Test
  void testSearchAppsLimit() throws SteamApiException {
    List<AppInfo> mockApps =
        List.of(
            new AppInfo(10, "Counter-Strike"),
            new AppInfo(80, "Counter-Strike: Condition Zero"),
            new AppInfo(240, "Counter-Strike: Source"),
            new AppInfo(730, "Counter-Strike: Global Offensive"),
            new AppInfo(1, "Counter-Strike 1.6"),
            new AppInfo(2, "Counter-Strike Beta"));

    when(mockSteamGames.getAppList()).thenReturn(mockApps);

    List<AppSearchResult> results = appSearch.searchApps("Counter-Strike", 3);

    assertEquals(3, results.size());
  }

  @Test
  void testSearchAppsEmptyQuery() throws SteamApiException {
    List<AppInfo> mockApps = List.of(new AppInfo(10, "Counter-Strike"));

    when(mockSteamGames.getAppList()).thenReturn(mockApps);

    List<AppSearchResult> results = appSearch.searchApps("", 5);

    assertTrue(results.isEmpty());
  }

  @Test
  void testSearchAppsNullQuery() throws SteamApiException {
    List<AppInfo> mockApps = List.of(new AppInfo(10, "Counter-Strike"));

    when(mockSteamGames.getAppList()).thenReturn(mockApps);

    List<AppSearchResult> results = appSearch.searchApps(null, 5);

    assertTrue(results.isEmpty());
  }

  @Test
  void testSearchAppsCaching() throws SteamApiException {
    List<AppInfo> mockApps = List.of(new AppInfo(10, "Counter-Strike"));

    when(mockSteamGames.getAppList()).thenReturn(mockApps);

    // First search loads the cache
    appSearch.searchApps("Counter-Strike", 5);

    // Second search should use cache (getAppList called only once)
    appSearch.searchApps("Counter-Strike", 5);

    verify(mockSteamGames, times(1)).getAppList();
  }

  @Test
  void testSearchAppsEmptyAppList() throws SteamApiException {
    when(mockSteamGames.getAppList()).thenReturn(List.of());

    List<AppSearchResult> results = appSearch.searchApps("Counter-Strike", 5);

    assertTrue(results.isEmpty());
  }

  @Test
  void testSearchAppsApiException() throws SteamApiException {
    when(mockSteamGames.getAppList()).thenThrow(new SteamApiException("API Error"));

    assertThrows(SteamApiException.class, () -> appSearch.searchApps("Counter-Strike", 5));
  }

  @Test
  void testSearchAppsCaseInsensitive() throws SteamApiException {
    List<AppInfo> mockApps = List.of(new AppInfo(10, "Counter-Strike"));

    when(mockSteamGames.getAppList()).thenReturn(mockApps);

    List<AppSearchResult> results = appSearch.searchApps("counter-strike", 5);

    assertFalse(results.isEmpty());
    assertEquals("Counter-Strike", results.get(0).getName());
  }
}
