package net.experimentalworks;

import java.util.List;
import java.util.stream.Collectors;

import com.lukaspradel.steamapi.core.exception.SteamApiException;
import com.lukaspradel.steamapi.data.json.ownedgames.GetOwnedGames;
import com.lukaspradel.steamapi.data.json.recentlyplayedgames.GetRecentlyPlayedGames;
import com.lukaspradel.steamapi.webapi.client.SteamWebApiClient;
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
    validateSteamId(steamId);
    GetOwnedGamesRequest request =
        SteamWebApiRequestFactory.createGetOwnedGamesRequest(steamId, true, true, List.of());
    return client.processRequest(request);
  }

  public GetRecentlyPlayedGames getRecentlyPlayedGames(String steamId) throws SteamApiException {
    validateSteamId(steamId);
    GetRecentlyPlayedGamesRequest request =
        SteamWebApiRequestFactory.createGetRecentlyPlayedGamesRequest(steamId);

    return client.processRequest(request);
  }

  public List<Game> getGames(String steamId) throws SteamApiException {
    validateSteamId(steamId);
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

  public List<Game> getRecentlyGames(String steamId) throws SteamApiException {
    validateSteamId(steamId);
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

  private void validateSteamId(String steamId) {
    if (steamId == null || steamId.isBlank()) {
      throw new IllegalArgumentException("Steam ID cannot be null or blank");
    }
    // Basic format validation - Steam IDs are typically 17-digit numbers
    if (!steamId.matches("\\d{1,17}")) {
      throw new IllegalArgumentException(
          "Invalid Steam ID format. Steam IDs should be numeric and up to 17 digits.");
    }
  }
}
