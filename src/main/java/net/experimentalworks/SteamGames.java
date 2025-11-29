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
      "https://api.steampowered.com/IStoreService/GetAppList/v1/";
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
  private static final int MAX_RESULTS_PER_PAGE = 50000;

  private final SteamWebApiClient client;
  private final HttpClient httpClient;
  private final String apiKey;

  public SteamGames(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException("API key cannot be null or blank");
    }
    this.apiKey = apiKey;
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
    this.apiKey = apiKey;
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
   * <p>This method retrieves all public Steam apps from the Steam Web API using the IStoreService
   * endpoint. The list includes games and DLC. Results are paginated (max 50k per request) and this
   * method handles pagination automatically to fetch all apps.
   *
   * <p>This endpoint requires an API key.
   *
   * @return list of AppInfo records containing app IDs and names
   * @throws SteamApiException if the API call fails
   */
  public List<AppInfo> getAppList() throws SteamApiException {
    try {
      List<AppInfo> allApps = new ArrayList<>();
      int lastAppId = 0;
      boolean haveMoreResults = true;

      while (haveMoreResults) {
        String url = buildAppListUrl(lastAppId);
        HttpRequest request =
            HttpRequest.newBuilder().uri(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build();

        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
          throw new SteamApiException(
              SteamApiException.Cause.HTTP_ERROR, Integer.valueOf(response.statusCode()));
        }

        AppListPage page = parseAppListResponse(response.body());
        allApps.addAll(page.apps());
        haveMoreResults = page.haveMoreResults();
        lastAppId = page.lastAppId();
      }

      return allApps;
    } catch (SteamApiException e) {
      throw e;
    } catch (Exception e) {
      throw new SteamApiException(SteamApiException.Cause.HTTP_ERROR, e);
    }
  }

  /**
   * Builds the URL for the IStoreService/GetAppList endpoint.
   *
   * @param lastAppId the last app ID from the previous page (0 for first request)
   * @return the complete URL with query parameters
   */
  private String buildAppListUrl(int lastAppId) {
    StringBuilder url = new StringBuilder(GET_APP_LIST_URL);
    url.append("?key=").append(apiKey);
    url.append("&max_results=").append(MAX_RESULTS_PER_PAGE);
    url.append("&include_games=true");
    url.append("&include_dlc=true");
    if (lastAppId > 0) {
      url.append("&last_appid=").append(lastAppId);
    }
    return url.toString();
  }

  /**
   * Parses the JSON response from the IStoreService/GetAppList endpoint.
   *
   * <p>Response format: { "response": { "apps": [ {"appid": 10, "name": "Counter-Strike",
   * "last_modified": 123, "price_change_number": 456}, ... ], "have_more_results": true,
   * "last_appid": 12345 } }
   *
   * @param responseBody the JSON response body
   * @return AppListPage containing apps and pagination info
   */
  private AppListPage parseAppListResponse(String responseBody) {
    JSONObject root = new JSONObject(responseBody);
    JSONObject response = root.optJSONObject("response");

    if (response == null) {
      return new AppListPage(List.of(), false, 0);
    }

    JSONArray apps = response.optJSONArray("apps");
    boolean haveMoreResults = response.optBoolean("have_more_results", false);
    int lastAppId = response.optInt("last_appid", 0);

    if (apps == null) {
      return new AppListPage(List.of(), false, 0);
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

    return new AppListPage(result, haveMoreResults, lastAppId);
  }

  /** Internal record to hold pagination results from the app list endpoint. */
  private record AppListPage(List<AppInfo> apps, boolean haveMoreResults, int lastAppId) {}
}
