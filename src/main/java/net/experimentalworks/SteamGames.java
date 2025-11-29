package net.experimentalworks;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.lukaspradel.steamapi.core.exception.SteamApiException;
import com.lukaspradel.steamapi.data.json.ownedgames.GetOwnedGames;
import com.lukaspradel.steamapi.data.json.recentlyplayedgames.GetRecentlyPlayedGames;
import com.lukaspradel.steamapi.webapi.client.SteamWebApiClient;
import com.lukaspradel.steamapi.webapi.request.GetOwnedGamesRequest;
import com.lukaspradel.steamapi.webapi.request.GetRecentlyPlayedGamesRequest;
import com.lukaspradel.steamapi.webapi.request.builders.SteamWebApiRequestFactory;

public class SteamGames {

  private static final String GET_APP_LIST_URL =
      "https://api.steampowered.com/ISteamApps/GetAppList/v2/";
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

  private final SteamWebApiClient client;
  private final HttpClient httpClient;

  public SteamGames(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException("API key cannot be null or blank");
    }
    this.client = new SteamWebApiClient.SteamWebApiClientBuilder(apiKey).build();
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
  }

  /**
   * Constructor for testing that allows injecting a custom HttpClient.
   *
   * @param apiKey the Steam Web API key
   * @param httpClient the HttpClient to use for direct API calls
   */
  SteamGames(String apiKey, HttpClient httpClient) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException("API key cannot be null or blank");
    }
    this.client = new SteamWebApiClient.SteamWebApiClientBuilder(apiKey).build();
    this.httpClient = httpClient;
  }

  public GetOwnedGames getOwnedGames(String steamId) throws SteamApiException {
    GetOwnedGamesRequest request =
        SteamWebApiRequestFactory.createGetOwnedGamesRequest(steamId, true, true, List.of());
    return client.processRequest(request);
  }

  public GetRecentlyPlayedGames getRecentlyPlayedGames(String steamId) throws SteamApiException {
    GetRecentlyPlayedGamesRequest request =
        SteamWebApiRequestFactory.createGetRecentlyPlayedGamesRequest(steamId);

    return client.processRequest(request);
  }

  public List<Game> getGames(String steamId) throws SteamApiException {
    GetOwnedGames ownedGames = getOwnedGames(steamId);

    if (ownedGames == null
        || ownedGames.getResponse() == null
        || ownedGames.getResponse().getGames() == null) {
      return List.of();
    }

    return ownedGames.getResponse().getGames().stream()
        .map(game -> new Game(game.getAppid(), game.getName(), game.getPlaytimeForever()))
        .collect(Collectors.toList());
  }

  public List<Game> getRecentGames(String steamId) throws SteamApiException {
    GetRecentlyPlayedGames recentGames = getRecentlyPlayedGames(steamId);

    if (recentGames == null
        || recentGames.getResponse() == null
        || recentGames.getResponse().getGames() == null) {
      return List.of();
    }

    return recentGames.getResponse().getGames().stream()
        .map(
            game ->
                new Game(
                    game.getAppid(),
                    game.getName(),
                    game.getPlaytimeForever(),
                    game.getPlaytime2weeks()))
        .collect(Collectors.toList());
  }

  /**
   * Fetches the complete list of all Steam applications.
   *
   * <p>This method retrieves all public Steam apps from the Steam Web API using a direct HTTP call
   * to bypass the steam-web-api library which has issues with this endpoint. The list includes
   * games, DLC, software, videos, and other Steam applications.
   *
   * <p>This endpoint is public and does not require an API key.
   *
   * @return list of AppInfo records containing app IDs and names
   * @throws SteamApiException if the API call fails
   */
  public List<AppInfo> getAppList() throws SteamApiException {
    try {
      HttpRequest request =
          HttpRequest.newBuilder().uri(URI.create(GET_APP_LIST_URL)).GET().build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new SteamApiException(
            SteamApiException.Cause.HTTP_ERROR,
            null,
            "HTTP " + response.statusCode() + " from Steam API");
      }

      return parseAppListResponse(response.body());
    } catch (SteamApiException e) {
      throw e;
    } catch (Exception e) {
      throw new SteamApiException(SteamApiException.Cause.HTTP_ERROR, e, e.getMessage());
    }
  }

  /**
   * Parses the JSON response from the GetAppList endpoint.
   *
   * <p>Response format: { "applist": { "apps": [ {"appid": 10, "name": "Counter-Strike"}, ... ] } }
   *
   * @param responseBody the JSON response body
   * @return list of AppInfo records
   */
  private List<AppInfo> parseAppListResponse(String responseBody) {
    JSONObject root = new JSONObject(responseBody);
    JSONObject applist = root.optJSONObject("applist");

    if (applist == null) {
      return List.of();
    }

    JSONArray apps = applist.optJSONArray("apps");

    if (apps == null) {
      return List.of();
    }

    List<AppInfo> result = new ArrayList<>();
    for (int i = 0; i < apps.length(); i++) {
      JSONObject app = apps.getJSONObject(i);
      int appId = app.optInt("appid", -1);
      String name = app.optString("name", null);

      if (appId >= 0 && name != null && !name.isEmpty()) {
        result.add(new AppInfo(appId, name));
      }
    }

    return result;
  }
}
