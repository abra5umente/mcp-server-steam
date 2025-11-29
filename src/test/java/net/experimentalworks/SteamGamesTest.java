package net.experimentalworks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.lukaspradel.steamapi.core.exception.SteamApiException;

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
  @SuppressWarnings("unchecked")
  void testGetAppListSuccess() throws Exception {
    HttpClient mockHttpClient = mock(HttpClient.class);
    HttpResponse<String> mockResponse = mock(HttpResponse.class);

    String jsonResponse =
        """
        {
          "response": {
            "apps": [
              {"appid": 10, "name": "Counter-Strike", "last_modified": 1234567890, "price_change_number": 123},
              {"appid": 20, "name": "Team Fortress Classic", "last_modified": 1234567891, "price_change_number": 124},
              {"appid": 30, "name": "Day of Defeat", "last_modified": 1234567892, "price_change_number": 125}
            ],
            "have_more_results": false,
            "last_appid": 30
          }
        }
        """;

    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(jsonResponse);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    SteamGames steamGames = new SteamGames("valid-api-key", mockHttpClient);
    List<AppInfo> apps = steamGames.getAppList();

    assertEquals(3, apps.size());
    assertEquals(10, apps.get(0).appId());
    assertEquals("Counter-Strike", apps.get(0).name());
    assertEquals(20, apps.get(1).appId());
    assertEquals("Team Fortress Classic", apps.get(1).name());
    assertEquals(30, apps.get(2).appId());
    assertEquals("Day of Defeat", apps.get(2).name());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetAppListEmptyResponse() throws Exception {
    HttpClient mockHttpClient = mock(HttpClient.class);
    HttpResponse<String> mockResponse = mock(HttpResponse.class);

    String jsonResponse =
        """
        {"response": {"apps": [], "have_more_results": false}}
        """;

    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(jsonResponse);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    SteamGames steamGames = new SteamGames("valid-api-key", mockHttpClient);
    List<AppInfo> apps = steamGames.getAppList();

    assertTrue(apps.isEmpty());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetAppListFiltersInvalidEntries() throws Exception {
    HttpClient mockHttpClient = mock(HttpClient.class);
    HttpResponse<String> mockResponse = mock(HttpResponse.class);

    String jsonResponse =
        """
        {
          "response": {
            "apps": [
              {"appid": 10, "name": "Counter-Strike", "last_modified": 123, "price_change_number": 1},
              {"appid": 20, "name": "", "last_modified": 124, "price_change_number": 2},
              {"appid": -1, "name": "Invalid App", "last_modified": 125, "price_change_number": 3},
              {"appid": 30, "name": "Day of Defeat", "last_modified": 126, "price_change_number": 4}
            ],
            "have_more_results": false,
            "last_appid": 30
          }
        }
        """;

    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(jsonResponse);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    SteamGames steamGames = new SteamGames("valid-api-key", mockHttpClient);
    List<AppInfo> apps = steamGames.getAppList();

    // Should filter out entries with empty names or negative appids
    assertEquals(2, apps.size());
    assertEquals("Counter-Strike", apps.get(0).name());
    assertEquals("Day of Defeat", apps.get(1).name());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetAppListHttpError() throws Exception {
    HttpClient mockHttpClient = mock(HttpClient.class);
    HttpResponse<String> mockResponse = mock(HttpResponse.class);

    when(mockResponse.statusCode()).thenReturn(404);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    SteamGames steamGames = new SteamGames("valid-api-key", mockHttpClient);

    assertThrows(SteamApiException.class, () -> steamGames.getAppList());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetAppListNetworkError() throws Exception {
    HttpClient mockHttpClient = mock(HttpClient.class);

    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenThrow(new IOException("Network error"));

    SteamGames steamGames = new SteamGames("valid-api-key", mockHttpClient);

    assertThrows(SteamApiException.class, () -> steamGames.getAppList());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetAppListMalformedJson() throws Exception {
    HttpClient mockHttpClient = mock(HttpClient.class);
    HttpResponse<String> mockResponse = mock(HttpResponse.class);

    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("not valid json");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    SteamGames steamGames = new SteamGames("valid-api-key", mockHttpClient);

    assertThrows(SteamApiException.class, () -> steamGames.getAppList());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetAppListMissingResponse() throws Exception {
    HttpClient mockHttpClient = mock(HttpClient.class);
    HttpResponse<String> mockResponse = mock(HttpResponse.class);

    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("{}");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    SteamGames steamGames = new SteamGames("valid-api-key", mockHttpClient);
    List<AppInfo> apps = steamGames.getAppList();

    assertTrue(apps.isEmpty());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetAppListPagination() throws Exception {
    HttpClient mockHttpClient = mock(HttpClient.class);
    HttpResponse<String> mockResponse1 = mock(HttpResponse.class);
    HttpResponse<String> mockResponse2 = mock(HttpResponse.class);

    // First page with have_more_results = true
    String jsonPage1 =
        """
        {
          "response": {
            "apps": [
              {"appid": 10, "name": "Counter-Strike", "last_modified": 123, "price_change_number": 1},
              {"appid": 20, "name": "Team Fortress", "last_modified": 124, "price_change_number": 2}
            ],
            "have_more_results": true,
            "last_appid": 20
          }
        }
        """;

    // Second page with have_more_results = false
    String jsonPage2 =
        """
        {
          "response": {
            "apps": [
              {"appid": 30, "name": "Day of Defeat", "last_modified": 125, "price_change_number": 3}
            ],
            "have_more_results": false,
            "last_appid": 30
          }
        }
        """;

    when(mockResponse1.statusCode()).thenReturn(200);
    when(mockResponse1.body()).thenReturn(jsonPage1);
    when(mockResponse2.statusCode()).thenReturn(200);
    when(mockResponse2.body()).thenReturn(jsonPage2);

    // Return first response, then second response
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse1)
        .thenReturn(mockResponse2);

    SteamGames steamGames = new SteamGames("valid-api-key", mockHttpClient);
    List<AppInfo> apps = steamGames.getAppList();

    // Should have all 3 apps from both pages
    assertEquals(3, apps.size());
    assertEquals(10, apps.get(0).appId());
    assertEquals("Counter-Strike", apps.get(0).name());
    assertEquals(20, apps.get(1).appId());
    assertEquals("Team Fortress", apps.get(1).name());
    assertEquals(30, apps.get(2).appId());
    assertEquals("Day of Defeat", apps.get(2).name());

    // Verify two requests were made
    verify(mockHttpClient, times(2))
        .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }
}
