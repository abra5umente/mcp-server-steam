package net.experimentalworks;

import java.util.List;
import java.util.stream.Collectors;

import com.lukaspradel.steamapi.core.exception.SteamApiException;
import com.lukaspradel.steamapi.data.json.applist.GetAppList;
import com.lukaspradel.steamapi.data.json.ownedgames.GetOwnedGames;
import com.lukaspradel.steamapi.data.json.recentlyplayedgames.GetRecentlyPlayedGames;
import com.lukaspradel.steamapi.webapi.client.SteamWebApiClient;
import com.lukaspradel.steamapi.webapi.request.GetAppListRequest;
import com.lukaspradel.steamapi.webapi.request.GetOwnedGamesRequest;
import com.lukaspradel.steamapi.webapi.request.GetRecentlyPlayedGamesRequest;
import com.lukaspradel.steamapi.webapi.request.builders.SteamWebApiRequestFactory;

public class SteamGames {

  private final SteamWebApiClient client;

  public SteamGames(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException("API key cannot be null or blank");
    }
    this.client = new SteamWebApiClient.SteamWebApiClientBuilder(apiKey).build();
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
   * <p>This method retrieves all public Steam apps from the Steam Web API. The list includes games,
   * DLC, software, videos, and other Steam applications. This endpoint does not require a Steam ID
   * or authentication beyond the API key.
   *
   * @return list of AppInfo records containing app IDs and names
   * @throws SteamApiException if the API call fails
   */
  public List<AppInfo> getAppList() throws SteamApiException {
    GetAppListRequest request = SteamWebApiRequestFactory.createGetAppListRequest();
    GetAppList appList = client.processRequest(request);

    if (appList == null || appList.getApplist() == null || appList.getApplist().getApps() == null) {
      return List.of();
    }

    return appList.getApplist().getApps().stream()
        .filter(app -> app.getAppid() != null && app.getName() != null)
        .map(app -> new AppInfo(app.getAppid().intValue(), app.getName()))
        .collect(Collectors.toList());
  }
}
