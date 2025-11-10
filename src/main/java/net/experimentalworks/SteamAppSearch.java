package net.experimentalworks;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;

import com.lukaspradel.steamapi.core.exception.SteamApiException;

/**
 * Service for searching Steam applications by name using fuzzy matching.
 *
 * <p>Caches the complete Steam app list in memory and provides fast fuzzy search capabilities using
 * Levenshtein distance for similarity scoring.
 */
public class SteamAppSearch {
  private static final Duration CACHE_TTL = Duration.ofDays(1);
  private static final int DEFAULT_LIMIT = 5;
  private static final LevenshteinDistance LEVENSHTEIN = new LevenshteinDistance();

  private final SteamGames steamGames;

  private volatile List<AppInfo> cachedAppList;
  private volatile Instant lastFetch;

  public SteamAppSearch(SteamGames steamGames) {
    this.steamGames = steamGames;
    this.cachedAppList = null;
    this.lastFetch = null;
  }

  /**
   * Searches for Steam apps by name using fuzzy matching.
   *
   * @param query the search query (game name)
   * @param limit maximum number of results to return (default 5)
   * @return list of matching apps sorted by similarity score (highest first)
   * @throws SteamApiException if the Steam API call fails
   */
  public List<AppSearchResult> searchApps(String query, int limit) throws SteamApiException {
    if (query == null || query.isBlank()) {
      return List.of();
    }

    ensureAppListLoaded();

    String normalizedQuery = query.toLowerCase().trim();
    int queryLength = normalizedQuery.length();

    // Calculate similarity scores for all apps
    List<AppSearchResult> results =
        cachedAppList.stream()
            .map(
                app -> {
                  String normalizedName = app.name().toLowerCase();
                  double score = calculateSimilarity(normalizedQuery, normalizedName, queryLength);
                  return new AppSearchResult(app.appId(), app.name(), score);
                })
            .filter(result -> result.getScore() > 0.0) // Only return matches with some similarity
            .sorted(Comparator.comparingDouble(AppSearchResult::getScore).reversed())
            .limit(limit > 0 ? limit : DEFAULT_LIMIT)
            .collect(Collectors.toList());

    return results;
  }

  /**
   * Calculates similarity score between query and app name.
   *
   * <p>Uses a combination of: - Exact substring match (highest priority) - Levenshtein distance for
   * fuzzy matching - Normalized by string length
   *
   * @param query normalized query string
   * @param name normalized app name
   * @param queryLength length of query for normalization
   * @return similarity score between 0.0 and 1.0 (higher is better)
   */
  private double calculateSimilarity(String query, String name, int queryLength) {
    // Exact match gets perfect score
    if (name.equals(query)) {
      return 1.0;
    }

    // Substring match gets high score
    if (name.contains(query)) {
      return 0.9;
    }

    // Fuzzy match using Levenshtein distance
    int maxLength = Math.max(query.length(), name.length());
    Integer distance = LEVENSHTEIN.apply(query, name);

    if (distance == null || distance < 0) {
      return 0.0;
    }

    // Normalize to 0-1 range (1.0 = identical, 0.0 = completely different)
    double similarity = 1.0 - ((double) distance / maxLength);

    // Apply threshold to filter out poor matches
    return similarity > 0.4 ? similarity : 0.0;
  }

  /**
   * Ensures the app list is loaded and refreshes if needed.
   *
   * @throws SteamApiException if the Steam API call fails
   */
  private void ensureAppListLoaded() throws SteamApiException {
    if (cachedAppList == null || isCacheExpired()) {
      synchronized (this) {
        // Double-check after acquiring lock
        if (cachedAppList == null || isCacheExpired()) {
          refreshCache();
        }
      }
    }
  }

  /**
   * Checks if the cache has expired based on TTL.
   *
   * @return true if cache is expired or never loaded
   */
  private boolean isCacheExpired() {
    return lastFetch == null || Instant.now().isAfter(lastFetch.plus(CACHE_TTL));
  }

  /**
   * Refreshes the app list cache by fetching from Steam API.
   *
   * @throws SteamApiException if the Steam API call fails
   */
  private void refreshCache() throws SteamApiException {
    List<AppInfo> apps = steamGames.getAppList();
    this.cachedAppList = apps;
    this.lastFetch = Instant.now();
  }

  /**
   * Gets cache statistics for debugging/monitoring.
   *
   * @return string with cache info
   */
  public String getCacheInfo() {
    if (cachedAppList == null) {
      return "Cache not loaded";
    }
    return String.format(
        "Cache loaded: %d apps, last fetch: %s, expired: %s",
        cachedAppList.size(), lastFetch, isCacheExpired());
  }
}
