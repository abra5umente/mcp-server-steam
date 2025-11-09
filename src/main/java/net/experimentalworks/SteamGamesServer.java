package net.experimentalworks;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.lukaspradel.steamapi.core.exception.SteamApiException;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.ServerMcpTransport;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class SteamGamesServer {

  private final McpAsyncServer server;
  private final SteamGames steamGames;
  private final SteamApiConfig config;

  public SteamGamesServer(ServerMcpTransport transport, SteamApiConfig config) {
    this.config = config;
    this.steamGames = new SteamGames(config.getSteamApiKey());

    String version = getClass().getPackage().getImplementationVersion();
    if (version == null) {
      version = "1.0.0"; // Fallback version if not found
    }
    this.server =
        McpServer.async(transport)
            .serverInfo("steam-games", version)
            .capabilities(ServerCapabilities.builder().tools(true).logging().build())
            .build();
  }

  public Mono<Void> run() {
    return server
        .addTool(createGetGamesTool())
        .then(server.addTool(createGetRecentGamesTool()))
        .then(Mono.never());
  }

  private McpServerFeatures.AsyncToolRegistration createGetGamesTool() {
    var schema =
        """
            {
              "type": "object",
              "properties": {}
            }
            """;

    var tool =
        new Tool(
            config.getToolPrefix() + "get-games",
            """
            Get a comprehensive list of all games owned by the specified Steam user, including their total playtime in minutes.
            This includes all games in their Steam library, both installed and uninstalled, free and purchased. For each game,
            returns details like the game name, AppID, total playtime, and whether they've played it recently. The data comes
            directly from Steam's official API using the provided Steam ID.
            NOTE: playtime is sent in minutes.
            """,
            schema);

    return new McpServerFeatures.AsyncToolRegistration(tool, this::handleGetGames);
  }

  private Mono<CallToolResult> handleGetGames(Map<String, Object> args) {
    return Mono.fromCallable(
            () -> {
              List<Game> games = steamGames.getGames(config.getSteamId());

              var json =
                  new JSONObject()
                      .put("owner", config.getSteamId())
                      .put("description", "Played games by the given steam id")
                      .put("all_games", new JSONArray(games));

              return new CallToolResult(List.of(new TextContent(json.toString())), false);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(
            SteamApiException.class,
            e ->
                Mono.just(
                    new CallToolResult(
                        List.of(
                            new TextContent(
                                new JSONObject()
                                    .put("error", "Failed to retrieve games from Steam API")
                                    .put("message", e.getMessage())
                                    .toString())),
                        true)))
        .onErrorResume(
            Exception.class,
            e ->
                Mono.just(
                    new CallToolResult(
                        List.of(
                            new TextContent(
                                new JSONObject()
                                    .put("error", "Unexpected error occurred")
                                    .put("message", e.getMessage())
                                    .toString())),
                        true)));
  }

  private McpServerFeatures.AsyncToolRegistration createGetRecentGamesTool() {
    var schema =
        """
            {
              "type": "object",
              "properties": {}
            }
            """;

    var tool =
        new Tool(
            config.getToolPrefix() + "get-recent-games",
            """
            Retrieve a list of recently played games for the specified Steam user, including playtime
            details from the last 2 weeks. This tool fetches data directly from Steam's API using the
            provided Steam ID and returns information like game names, AppIDs, and recent playtime
            statistics in minutes. The results only include games that have been played in the recent time period,
            making it useful for tracking current gaming activity and habits.
            NOTE: playtime is sent in minutes.
            """,
            schema);

    return new McpServerFeatures.AsyncToolRegistration(tool, this::handleGetRecentGames);
  }

  private Mono<CallToolResult> handleGetRecentGames(Map<String, Object> args) {
    return Mono.fromCallable(
            () -> {
              List<Game> games = steamGames.getRecentlyGames(config.getSteamId());

              var json =
                  new JSONObject()
                      .put("owner", config.getSteamId())
                      .put("description", "Recently played games by the given steam id")
                      .put("recent_games", new JSONArray(games));

              return new CallToolResult(List.of(new TextContent(json.toString())), false);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(
            SteamApiException.class,
            e ->
                Mono.just(
                    new CallToolResult(
                        List.of(
                            new TextContent(
                                new JSONObject()
                                    .put("error", "Failed to retrieve recent games from Steam API")
                                    .put("message", e.getMessage())
                                    .toString())),
                        true)))
        .onErrorResume(
            Exception.class,
            e ->
                Mono.just(
                    new CallToolResult(
                        List.of(
                            new TextContent(
                                new JSONObject()
                                    .put("error", "Unexpected error occurred")
                                    .put("message", e.getMessage())
                                    .toString())),
                        true)));
  }
}
