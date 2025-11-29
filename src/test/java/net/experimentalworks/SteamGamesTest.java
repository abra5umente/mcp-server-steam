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
          "applist": {
            "apps": [
              {"appid": 10, "name": "Counter-Strike"},
              {"appid": 20, "name": "Team Fortress Classic"},
              {"appid": 30, "name": "Day of Defeat"}
            ]
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

    String jsonResponse = """
        {"applist": {"apps": []}}
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
          "applist": {
            "apps": [
              {"appid": 10, "name": "Counter-Strike"},
              {"appid": 20, "name": ""},
              {"appid": -1, "name": "Invalid App"},
              {"appid": 30, "name": "Day of Defeat"}
            ]
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

    SteamApiException exception =
        assertThrows(SteamApiException.class, () -> steamGames.getAppList());
    assertTrue(exception.getMessage().contains("404"));
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
  void testGetAppListMissingApplist() throws Exception {
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
}
