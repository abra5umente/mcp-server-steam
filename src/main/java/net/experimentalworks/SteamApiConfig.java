package net.experimentalworks;

/**
 * Configuration class for Steam API settings. Loads and validates configuration from environment
 * variables.
 */
public class SteamApiConfig {

  private final String steamApiKey;
  private final String steamId;
  private final String toolPrefix;

  /**
   * Creates a new SteamApiConfig by loading values from environment variables.
   *
   * @throws IllegalStateException if required environment variables are missing
   * @throws IllegalArgumentException if Steam ID format is invalid
   */
  public SteamApiConfig() {
    this.steamApiKey = getRequiredEnv("STEAM_API_KEY");
    String id = getRequiredEnv("STEAM_ID");
    validateSteamId(id);
    this.steamId = id;
    this.toolPrefix = getEnvOrDefault("TOOL_PREFIX", "");
  }

  /**
   * Creates a new SteamApiConfig with explicit values (primarily for testing).
   *
   * @param steamApiKey the Steam API key
   * @param steamId the Steam user ID
   * @param toolPrefix the prefix for tool names
   * @throws IllegalArgumentException if parameters are invalid
   */
  public SteamApiConfig(String steamApiKey, String steamId, String toolPrefix) {
    if (steamApiKey == null || steamApiKey.isBlank()) {
      throw new IllegalArgumentException("steamApiKey cannot be null or blank");
    }
    if (steamId == null || steamId.isBlank()) {
      throw new IllegalArgumentException("steamId cannot be null or blank");
    }
    validateSteamId(steamId);
    this.steamApiKey = steamApiKey;
    this.steamId = steamId;
    this.toolPrefix = toolPrefix != null ? toolPrefix : "";
  }

  private static String getRequiredEnv(String key) {
    String value = System.getenv(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
          String.format(
              "Required environment variable '%s' is not set. "
                  + "Please set it before starting the server.",
              key));
    }
    return value;
  }

  private static String getEnvOrDefault(String key, String defaultValue) {
    String value = System.getenv(key);
    return value != null ? value : defaultValue;
  }

  private static void validateSteamId(String steamId) {
    // Steam accepts both SteamID64 (numeric) and custom vanity URLs (alphanumeric)
    // Just ensure it's not unreasonably long (Steam custom URLs are max 32 chars)
    if (steamId.length() > 32) {
      throw new IllegalArgumentException(
          "Invalid Steam ID format. Steam IDs should be at most 32 characters.");
    }
  }

  public String getSteamApiKey() {
    return steamApiKey;
  }

  public String getSteamId() {
    return steamId;
  }

  public String getToolPrefix() {
    return toolPrefix;
  }
}
