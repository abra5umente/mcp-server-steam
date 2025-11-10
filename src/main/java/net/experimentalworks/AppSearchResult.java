package net.experimentalworks;

import java.io.Serializable;

import org.json.JSONObject;

/**
 * Immutable data model representing a Steam app search result.
 *
 * <p>Contains the app ID, name, and similarity score from fuzzy matching.
 */
public class AppSearchResult implements Serializable {
  private static final long serialVersionUID = 1L;

  private final int appId;
  private final String name;
  private final double score;

  public AppSearchResult(int appId, String name, double score) {
    this.appId = appId;
    this.name = name;
    this.score = score;
  }

  public int getAppId() {
    return appId;
  }

  public String getName() {
    return name;
  }

  public double getScore() {
    return score;
  }

  /** Converts this AppSearchResult to a JSONObject for MCP response. */
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("app_id", appId);
    json.put("name", name);
    json.put("score", score);
    return json;
  }

  @Override
  public String toString() {
    return "AppSearchResult{appId=" + appId + ", name='" + name + "', score=" + score + "}";
  }
}
