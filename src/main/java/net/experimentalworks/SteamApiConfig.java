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
   */
  public SteamApiConfig() {
    this.steamApiKey = getRequiredEnv("STEAM_API_KEY");
    this.steamId = getRequiredEnv("STEAM_ID");
    this.toolPrefix = getEnvOrDefault("TOOL_PREFIX", "");
  }

  /**
   * Creates a new SteamApiConfig with explicit values (primarily for testing).
   *
   * @param steamApiKey the Steam API key
   * @param steamId the Steam user ID
   * @param toolPrefix the prefix for tool names
   */
  public SteamApiConfig(String steamApiKey, String steamId, String toolPrefix) {
    if (steamApiKey == null || steamApiKey.isBlank()) {
      throw new IllegalArgumentException("steamApiKey cannot be null or blank");
    }
    if (steamId == null || steamId.isBlank()) {
      throw new IllegalArgumentException("steamId cannot be null or blank");
    }
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
