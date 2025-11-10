package net.experimentalworks;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * HTTP client wrapper for the Steam Store API.
 *
 * <p>Provides methods to fetch detailed store information for Steam applications using the public
 * Steam Store API endpoint (https://store.steampowered.com/api/appdetails).
 *
 * <p>This API does not require authentication but has rate limiting (200 requests per 5 minutes).
 */
public class SteamStoreClient {
  private static final String STORE_API_BASE_URL = "https://store.steampowered.com/api/appdetails";
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

  private final HttpClient httpClient;

  /** Creates a new SteamStoreClient with a default HttpClient. */
  public SteamStoreClient() {
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
  }

  /**
   * Creates a new SteamStoreClient with a custom HttpClient (primarily for testing).
   *
   * @param httpClient the HttpClient to use for requests
   */
  public SteamStoreClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  /**
   * Fetches store details for multiple Steam applications.
   *
   * @param appIds list of Steam app IDs to fetch details for
   * @param countryCode optional ISO 3166-1 country code for region-specific pricing (e.g., "US",
   *     "GB")
   * @param language optional language code for localized descriptions (e.g., "en", "es", "fr")
   * @return Mono emitting a list of StoreDetails objects (may include failed requests with
   *     success=false)
   */
  public Mono<List<StoreDetails>> getStoreDetails(
      List<Integer> appIds, Optional<String> countryCode, Optional<String> language) {
    if (appIds == null || appIds.isEmpty()) {
      return Mono.just(List.of());
    }

    // Make parallel requests for each app ID (Steam Store API doesn't support batch requests)
    return Flux.fromIterable(appIds)
        .flatMap(appId -> fetchSingleAppDetails(appId, countryCode, language))
        .collectList()
        .subscribeOn(Schedulers.boundedElastic());
  }

  /**
   * Fetches store details for a single Steam application.
   *
   * @param appId the Steam app ID
   * @param countryCode optional country code for region-specific pricing
   * @param language optional language code for localized descriptions
   * @return Mono emitting a StoreDetails object
   */
  private Mono<StoreDetails> fetchSingleAppDetails(
      int appId, Optional<String> countryCode, Optional<String> language) {
    String url = buildUrl(appId, countryCode, language);

    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

    CompletableFuture<StoreDetails> future =
        httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> parseStoreDetailsResponse(appId, response.body()));

    return Mono.fromCompletionStage(future)
        .onErrorResume(
            e ->
                Mono.just(
                    createFailedStoreDetails(appId, "HTTP request failed: " + e.getMessage())));
  }

  /**
   * Builds the Steam Store API URL with optional parameters.
   *
   * @param appId the Steam app ID
   * @param countryCode optional country code
   * @param language optional language code
   * @return the complete URL string
   */
  private String buildUrl(int appId, Optional<String> countryCode, Optional<String> language) {
    StringBuilder url = new StringBuilder(STORE_API_BASE_URL);
    url.append("?appids=").append(appId);

    countryCode.ifPresent(cc -> url.append("&cc=").append(cc));
    language.ifPresent(lang -> url.append("&l=").append(lang));

    return url.toString();
  }

  /**
   * Parses the JSON response from Steam Store API into a StoreDetails object.
   *
   * @param appId the Steam app ID that was requested
   * @param responseBody the JSON response body
   * @return StoreDetails object with success flag indicating if the request succeeded
   */
  private StoreDetails parseStoreDetailsResponse(int appId, String responseBody) {
    try {
      JSONObject root = new JSONObject(responseBody);
      JSONObject appData = root.getJSONObject(String.valueOf(appId));

      boolean success = appData.optBoolean("success", false);

      if (!success) {
        return createFailedStoreDetails(appId, "Steam API returned success=false");
      }

      JSONObject data = appData.getJSONObject("data");

      return new StoreDetails(
          appId,
          data.optString("type", "unknown"),
          data.optString("name", "Unknown"),
          data.optInt("required_age", 0),
          data.optBoolean("is_free", false),
          optString(data, "controller_support"),
          optIntList(data, "dlc"),
          optString(data, "detailed_description"),
          optString(data, "about_the_game"),
          optString(data, "short_description"),
          parsePriceOverview(data),
          optString(data, "header_image"),
          parseScreenshots(data),
          parseMovies(data),
          parseCategories(data),
          parseGenres(data),
          parseStringList(data, "developers"),
          parseStringList(data, "publishers"),
          optString(data, "supported_languages"),
          parsePlatforms(data),
          parseRequirements(data, "pc_requirements"),
          parseRequirements(data, "mac_requirements"),
          parseRequirements(data, "linux_requirements"),
          parseMetacritic(data),
          parseRecommendations(data),
          parseAchievements(data),
          parseReleaseDate(data),
          optString(data, "website"),
          optString(data, "legal_notice"),
          true);

    } catch (Exception e) {
      return createFailedStoreDetails(appId, "Failed to parse response: " + e.getMessage());
    }
  }

  /**
   * Creates a failed StoreDetails object for error cases.
   *
   * @param appId the app ID that failed
   * @param reason the error reason
   * @return StoreDetails with success=false and minimal data
   */
  private StoreDetails createFailedStoreDetails(int appId, String reason) {
    return new StoreDetails(
        appId,
        "error",
        "Failed to fetch details: " + reason,
        0,
        false,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        new StoreDetails.Platforms(false, false, false),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        new StoreDetails.ReleaseDate(false, "Unknown"),
        Optional.empty(),
        Optional.empty(),
        false);
  }

  // Helper parsing methods

  private Optional<String> optString(JSONObject obj, String key) {
    if (obj.has(key) && !obj.isNull(key)) {
      String value = obj.getString(key);
      return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }
    return Optional.empty();
  }

  private Optional<List<Integer>> optIntList(JSONObject obj, String key) {
    if (obj.has(key) && !obj.isNull(key)) {
      JSONArray arr = obj.getJSONArray(key);
      List<Integer> list = new ArrayList<>();
      for (int i = 0; i < arr.length(); i++) {
        list.add(arr.getInt(i));
      }
      return list.isEmpty() ? Optional.empty() : Optional.of(list);
    }
    return Optional.empty();
  }

  private Optional<StoreDetails.PriceOverview> parsePriceOverview(JSONObject data) {
    if (!data.has("price_overview") || data.isNull("price_overview")) {
      return Optional.empty();
    }

    JSONObject po = data.getJSONObject("price_overview");
    return Optional.of(
        new StoreDetails.PriceOverview(
            po.optString("currency", "USD"),
            po.optInt("initial", 0),
            po.optInt("final", 0),
            po.optInt("discount_percent", 0),
            optString(po, "final_formatted")));
  }

  private Optional<List<StoreDetails.Screenshot>> parseScreenshots(JSONObject data) {
    if (!data.has("screenshots") || data.isNull("screenshots")) {
      return Optional.empty();
    }

    JSONArray arr = data.getJSONArray("screenshots");
    List<StoreDetails.Screenshot> screenshots = new ArrayList<>();
    for (int i = 0; i < arr.length(); i++) {
      JSONObject ss = arr.getJSONObject(i);
      screenshots.add(
          new StoreDetails.Screenshot(
              ss.optInt("id", i),
              ss.optString("path_thumbnail", ""),
              ss.optString("path_full", "")));
    }
    return screenshots.isEmpty() ? Optional.empty() : Optional.of(screenshots);
  }

  private Optional<List<StoreDetails.Movie>> parseMovies(JSONObject data) {
    if (!data.has("movies") || data.isNull("movies")) {
      return Optional.empty();
    }

    JSONArray arr = data.getJSONArray("movies");
    List<StoreDetails.Movie> movies = new ArrayList<>();
    for (int i = 0; i < arr.length(); i++) {
      JSONObject movie = arr.getJSONObject(i);
      JSONObject webm = movie.optJSONObject("webm");
      JSONObject mp4 = movie.optJSONObject("mp4");

      if (webm != null) {
        movies.add(
            new StoreDetails.Movie(
                movie.optInt("id", i),
                movie.optString("name", ""),
                movie.optString("thumbnail", ""),
                new StoreDetails.Movie.Webm(webm.optString("480", ""), webm.optString("max", "")),
                mp4 != null
                    ? Optional.of(
                        new StoreDetails.Movie.Mp4(
                            mp4.optString("480", ""), mp4.optString("max", "")))
                    : Optional.empty()));
      }
    }
    return movies.isEmpty() ? Optional.empty() : Optional.of(movies);
  }

  private Optional<List<StoreDetails.Category>> parseCategories(JSONObject data) {
    if (!data.has("categories") || data.isNull("categories")) {
      return Optional.empty();
    }

    JSONArray arr = data.getJSONArray("categories");
    List<StoreDetails.Category> categories = new ArrayList<>();
    for (int i = 0; i < arr.length(); i++) {
      JSONObject cat = arr.getJSONObject(i);
      categories.add(
          new StoreDetails.Category(cat.optInt("id", 0), cat.optString("description", "")));
    }
    return categories.isEmpty() ? Optional.empty() : Optional.of(categories);
  }

  private Optional<List<StoreDetails.Genre>> parseGenres(JSONObject data) {
    if (!data.has("genres") || data.isNull("genres")) {
      return Optional.empty();
    }

    JSONArray arr = data.getJSONArray("genres");
    List<StoreDetails.Genre> genres = new ArrayList<>();
    for (int i = 0; i < arr.length(); i++) {
      JSONObject genre = arr.getJSONObject(i);
      genres.add(
          new StoreDetails.Genre(genre.optString("id", ""), genre.optString("description", "")));
    }
    return genres.isEmpty() ? Optional.empty() : Optional.of(genres);
  }

  private Optional<List<String>> parseStringList(JSONObject data, String key) {
    if (!data.has(key) || data.isNull(key)) {
      return Optional.empty();
    }

    JSONArray arr = data.getJSONArray(key);
    List<String> list = new ArrayList<>();
    for (int i = 0; i < arr.length(); i++) {
      list.add(arr.getString(i));
    }
    return list.isEmpty() ? Optional.empty() : Optional.of(list);
  }

  private StoreDetails.Platforms parsePlatforms(JSONObject data) {
    if (!data.has("platforms") || data.isNull("platforms")) {
      return new StoreDetails.Platforms(false, false, false);
    }

    JSONObject platforms = data.getJSONObject("platforms");
    return new StoreDetails.Platforms(
        platforms.optBoolean("windows", false),
        platforms.optBoolean("mac", false),
        platforms.optBoolean("linux", false));
  }

  private Optional<StoreDetails.Requirements> parseRequirements(JSONObject data, String key) {
    if (!data.has(key) || data.isNull(key)) {
      return Optional.empty();
    }

    Object reqObj = data.get(key);
    // Sometimes requirements is an empty array instead of an object
    if (reqObj instanceof JSONArray) {
      return Optional.empty();
    }

    JSONObject req = (JSONObject) reqObj;
    Optional<String> minimum = optString(req, "minimum");
    Optional<String> recommended = optString(req, "recommended");

    if (minimum.isEmpty() && recommended.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new StoreDetails.Requirements(minimum, recommended));
  }

  private Optional<StoreDetails.Metacritic> parseMetacritic(JSONObject data) {
    if (!data.has("metacritic") || data.isNull("metacritic")) {
      return Optional.empty();
    }

    JSONObject mc = data.getJSONObject("metacritic");
    return Optional.of(new StoreDetails.Metacritic(mc.optInt("score", 0), mc.optString("url", "")));
  }

  private Optional<StoreDetails.Recommendations> parseRecommendations(JSONObject data) {
    if (!data.has("recommendations") || data.isNull("recommendations")) {
      return Optional.empty();
    }

    JSONObject rec = data.getJSONObject("recommendations");
    return Optional.of(new StoreDetails.Recommendations(rec.optInt("total", 0)));
  }

  private Optional<StoreDetails.Achievements> parseAchievements(JSONObject data) {
    if (!data.has("achievements") || data.isNull("achievements")) {
      return Optional.empty();
    }

    JSONObject ach = data.getJSONObject("achievements");
    int total = ach.optInt("total", 0);

    Optional<List<StoreDetails.Achievements.Highlighted>> highlighted = Optional.empty();
    if (ach.has("highlighted") && !ach.isNull("highlighted")) {
      JSONArray hlArray = ach.getJSONArray("highlighted");
      List<StoreDetails.Achievements.Highlighted> hlList = new ArrayList<>();
      for (int i = 0; i < hlArray.length(); i++) {
        JSONObject hl = hlArray.getJSONObject(i);
        hlList.add(
            new StoreDetails.Achievements.Highlighted(
                hl.optString("name", ""), hl.optString("path", "")));
      }
      if (!hlList.isEmpty()) {
        highlighted = Optional.of(hlList);
      }
    }

    return Optional.of(new StoreDetails.Achievements(total, highlighted));
  }

  private StoreDetails.ReleaseDate parseReleaseDate(JSONObject data) {
    if (!data.has("release_date") || data.isNull("release_date")) {
      return new StoreDetails.ReleaseDate(false, "Unknown");
    }

    JSONObject rd = data.getJSONObject("release_date");
    return new StoreDetails.ReleaseDate(
        rd.optBoolean("coming_soon", false), rd.optString("date", "Unknown"));
  }
}
