package net.experimentalworks;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Immutable data model representing complete Steam Store details for a game.
 *
 * <p>Contains all information available from the Steam Store API including basic info, pricing,
 * descriptions, media, categorization, platform support, and more.
 */
public class StoreDetails implements Serializable {
  private static final long serialVersionUID = 1L;

  // Basic Information
  private final int appId;
  private final String type;
  private final String name;
  private final int requiredAge;
  private final boolean isFree;
  private final Optional<String> controllerSupport;
  private final Optional<List<Integer>> dlc;

  // Descriptions
  private final Optional<String> detailedDescription;
  private final Optional<String> aboutTheGame;
  private final Optional<String> shortDescription;

  // Pricing
  private final Optional<PriceOverview> priceOverview;

  // Media
  private final Optional<String> headerImage;
  private final Optional<List<Screenshot>> screenshots;
  private final Optional<List<Movie>> movies;

  // Categorization
  private final Optional<List<Category>> categories;
  private final Optional<List<Genre>> genres;
  private final Optional<List<String>> developers;
  private final Optional<List<String>> publishers;
  private final Optional<String> supportedLanguages;

  // Platform Support
  private final Platforms platforms;
  private final Optional<Requirements> pcRequirements;
  private final Optional<Requirements> macRequirements;
  private final Optional<Requirements> linuxRequirements;

  // Additional Info
  private final Optional<Metacritic> metacritic;
  private final Optional<Recommendations> recommendations;
  private final Optional<Achievements> achievements;
  private final ReleaseDate releaseDate;
  private final Optional<String> website;
  private final Optional<String> legalNotice;

  // Success flag
  private final boolean success;

  public StoreDetails(
      int appId,
      String type,
      String name,
      int requiredAge,
      boolean isFree,
      Optional<String> controllerSupport,
      Optional<List<Integer>> dlc,
      Optional<String> detailedDescription,
      Optional<String> aboutTheGame,
      Optional<String> shortDescription,
      Optional<PriceOverview> priceOverview,
      Optional<String> headerImage,
      Optional<List<Screenshot>> screenshots,
      Optional<List<Movie>> movies,
      Optional<List<Category>> categories,
      Optional<List<Genre>> genres,
      Optional<List<String>> developers,
      Optional<List<String>> publishers,
      Optional<String> supportedLanguages,
      Platforms platforms,
      Optional<Requirements> pcRequirements,
      Optional<Requirements> macRequirements,
      Optional<Requirements> linuxRequirements,
      Optional<Metacritic> metacritic,
      Optional<Recommendations> recommendations,
      Optional<Achievements> achievements,
      ReleaseDate releaseDate,
      Optional<String> website,
      Optional<String> legalNotice,
      boolean success) {
    this.appId = appId;
    this.type = type;
    this.name = name;
    this.requiredAge = requiredAge;
    this.isFree = isFree;
    this.controllerSupport = controllerSupport;
    this.dlc = dlc;
    this.detailedDescription = detailedDescription;
    this.aboutTheGame = aboutTheGame;
    this.shortDescription = shortDescription;
    this.priceOverview = priceOverview;
    this.headerImage = headerImage;
    this.screenshots = screenshots;
    this.movies = movies;
    this.categories = categories;
    this.genres = genres;
    this.developers = developers;
    this.publishers = publishers;
    this.supportedLanguages = supportedLanguages;
    this.platforms = platforms;
    this.pcRequirements = pcRequirements;
    this.macRequirements = macRequirements;
    this.linuxRequirements = linuxRequirements;
    this.metacritic = metacritic;
    this.recommendations = recommendations;
    this.achievements = achievements;
    this.releaseDate = releaseDate;
    this.website = website;
    this.legalNotice = legalNotice;
    this.success = success;
  }

  // Getters
  public int getAppId() {
    return appId;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public int getRequiredAge() {
    return requiredAge;
  }

  public boolean isFree() {
    return isFree;
  }

  public Optional<String> getControllerSupport() {
    return controllerSupport;
  }

  public Optional<List<Integer>> getDlc() {
    return dlc;
  }

  public Optional<String> getDetailedDescription() {
    return detailedDescription;
  }

  public Optional<String> getAboutTheGame() {
    return aboutTheGame;
  }

  public Optional<String> getShortDescription() {
    return shortDescription;
  }

  public Optional<PriceOverview> getPriceOverview() {
    return priceOverview;
  }

  public Optional<String> getHeaderImage() {
    return headerImage;
  }

  public Optional<List<Screenshot>> getScreenshots() {
    return screenshots;
  }

  public Optional<List<Movie>> getMovies() {
    return movies;
  }

  public Optional<List<Category>> getCategories() {
    return categories;
  }

  public Optional<List<Genre>> getGenres() {
    return genres;
  }

  public Optional<List<String>> getDevelopers() {
    return developers;
  }

  public Optional<List<String>> getPublishers() {
    return publishers;
  }

  public Optional<String> getSupportedLanguages() {
    return supportedLanguages;
  }

  public Platforms getPlatforms() {
    return platforms;
  }

  public Optional<Requirements> getPcRequirements() {
    return pcRequirements;
  }

  public Optional<Requirements> getMacRequirements() {
    return macRequirements;
  }

  public Optional<Requirements> getLinuxRequirements() {
    return linuxRequirements;
  }

  public Optional<Metacritic> getMetacritic() {
    return metacritic;
  }

  public Optional<Recommendations> getRecommendations() {
    return recommendations;
  }

  public Optional<Achievements> getAchievements() {
    return achievements;
  }

  public ReleaseDate getReleaseDate() {
    return releaseDate;
  }

  public Optional<String> getWebsite() {
    return website;
  }

  public Optional<String> getLegalNotice() {
    return legalNotice;
  }

  public boolean isSuccess() {
    return success;
  }

  /** Converts this StoreDetails object to a JSONObject for MCP response. */
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("app_id", appId);
    json.put("type", type);
    json.put("name", name);
    json.put("required_age", requiredAge);
    json.put("is_free", isFree);
    json.put("success", success);

    controllerSupport.ifPresent(cs -> json.put("controller_support", cs));
    dlc.ifPresent(
        d -> {
          JSONArray dlcArray = new JSONArray();
          d.forEach(dlcArray::put);
          json.put("dlc", dlcArray);
        });

    detailedDescription.ifPresent(dd -> json.put("detailed_description", dd));
    aboutTheGame.ifPresent(atg -> json.put("about_the_game", atg));
    shortDescription.ifPresent(sd -> json.put("short_description", sd));

    priceOverview.ifPresent(po -> json.put("price_overview", po.toJson()));

    headerImage.ifPresent(hi -> json.put("header_image", hi));
    screenshots.ifPresent(
        ss -> {
          JSONArray ssArray = new JSONArray();
          ss.forEach(s -> ssArray.put(s.toJson()));
          json.put("screenshots", ssArray);
        });
    movies.ifPresent(
        ms -> {
          JSONArray msArray = new JSONArray();
          ms.forEach(m -> msArray.put(m.toJson()));
          json.put("movies", msArray);
        });

    categories.ifPresent(
        cats -> {
          JSONArray catsArray = new JSONArray();
          cats.forEach(c -> catsArray.put(c.toJson()));
          json.put("categories", catsArray);
        });
    genres.ifPresent(
        gens -> {
          JSONArray gensArray = new JSONArray();
          gens.forEach(g -> gensArray.put(g.toJson()));
          json.put("genres", gensArray);
        });
    developers.ifPresent(
        devs -> {
          JSONArray devsArray = new JSONArray();
          devs.forEach(devsArray::put);
          json.put("developers", devsArray);
        });
    publishers.ifPresent(
        pubs -> {
          JSONArray pubsArray = new JSONArray();
          pubs.forEach(pubsArray::put);
          json.put("publishers", pubsArray);
        });
    supportedLanguages.ifPresent(sl -> json.put("supported_languages", sl));

    json.put("platforms", platforms.toJson());
    pcRequirements.ifPresent(pcr -> json.put("pc_requirements", pcr.toJson()));
    macRequirements.ifPresent(macr -> json.put("mac_requirements", macr.toJson()));
    linuxRequirements.ifPresent(linr -> json.put("linux_requirements", linr.toJson()));

    metacritic.ifPresent(mc -> json.put("metacritic", mc.toJson()));
    recommendations.ifPresent(rec -> json.put("recommendations", rec.toJson()));
    achievements.ifPresent(ach -> json.put("achievements", ach.toJson()));

    json.put("release_date", releaseDate.toJson());
    website.ifPresent(w -> json.put("website", w));
    legalNotice.ifPresent(ln -> json.put("legal_notice", ln));

    return json;
  }

  // Nested classes for structured data

  public static class PriceOverview implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String currency;
    private final int initial;
    private final int finalPrice;
    private final int discountPercent;
    private final Optional<String> finalFormatted;

    public PriceOverview(
        String currency,
        int initial,
        int finalPrice,
        int discountPercent,
        Optional<String> finalFormatted) {
      this.currency = currency;
      this.initial = initial;
      this.finalPrice = finalPrice;
      this.discountPercent = discountPercent;
      this.finalFormatted = finalFormatted;
    }

    public String getCurrency() {
      return currency;
    }

    public int getInitial() {
      return initial;
    }

    public int getFinalPrice() {
      return finalPrice;
    }

    public int getDiscountPercent() {
      return discountPercent;
    }

    public Optional<String> getFinalFormatted() {
      return finalFormatted;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("currency", currency);
      json.put("initial", initial);
      json.put("final", finalPrice);
      json.put("discount_percent", discountPercent);
      finalFormatted.ifPresent(ff -> json.put("final_formatted", ff));
      return json;
    }
  }

  public static class Screenshot implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String pathThumbnail;
    private final String pathFull;

    public Screenshot(int id, String pathThumbnail, String pathFull) {
      this.id = id;
      this.pathThumbnail = pathThumbnail;
      this.pathFull = pathFull;
    }

    public int getId() {
      return id;
    }

    public String getPathThumbnail() {
      return pathThumbnail;
    }

    public String getPathFull() {
      return pathFull;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("id", id);
      json.put("path_thumbnail", pathThumbnail);
      json.put("path_full", pathFull);
      return json;
    }
  }

  public static class Movie implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String name;
    private final String thumbnail;
    private final Webm webm;
    private final Optional<Mp4> mp4;

    public Movie(int id, String name, String thumbnail, Webm webm, Optional<Mp4> mp4) {
      this.id = id;
      this.name = name;
      this.thumbnail = thumbnail;
      this.webm = webm;
      this.mp4 = mp4;
    }

    public int getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getThumbnail() {
      return thumbnail;
    }

    public Webm getWebm() {
      return webm;
    }

    public Optional<Mp4> getMp4() {
      return mp4;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("id", id);
      json.put("name", name);
      json.put("thumbnail", thumbnail);
      json.put("webm", webm.toJson());
      mp4.ifPresent(m -> json.put("mp4", m.toJson()));
      return json;
    }

    public static class Webm implements Serializable {
      private static final long serialVersionUID = 1L;
      private final String w480;
      private final String max;

      public Webm(String w480, String max) {
        this.w480 = w480;
        this.max = max;
      }

      public String getW480() {
        return w480;
      }

      public String getMax() {
        return max;
      }

      public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("480", w480);
        json.put("max", max);
        return json;
      }
    }

    public static class Mp4 implements Serializable {
      private static final long serialVersionUID = 1L;
      private final String w480;
      private final String max;

      public Mp4(String w480, String max) {
        this.w480 = w480;
        this.max = max;
      }

      public String getW480() {
        return w480;
      }

      public String getMax() {
        return max;
      }

      public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("480", w480);
        json.put("max", max);
        return json;
      }
    }
  }

  public static class Category implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String description;

    public Category(int id, String description) {
      this.id = id;
      this.description = description;
    }

    public int getId() {
      return id;
    }

    public String getDescription() {
      return description;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("id", id);
      json.put("description", description);
      return json;
    }
  }

  public static class Genre implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String description;

    public Genre(String id, String description) {
      this.id = id;
      this.description = description;
    }

    public String getId() {
      return id;
    }

    public String getDescription() {
      return description;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("id", id);
      json.put("description", description);
      return json;
    }
  }

  public static class Platforms implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean windows;
    private final boolean mac;
    private final boolean linux;

    public Platforms(boolean windows, boolean mac, boolean linux) {
      this.windows = windows;
      this.mac = mac;
      this.linux = linux;
    }

    public boolean isWindows() {
      return windows;
    }

    public boolean isMac() {
      return mac;
    }

    public boolean isLinux() {
      return linux;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("windows", windows);
      json.put("mac", mac);
      json.put("linux", linux);
      return json;
    }
  }

  public static class Requirements implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Optional<String> minimum;
    private final Optional<String> recommended;

    public Requirements(Optional<String> minimum, Optional<String> recommended) {
      this.minimum = minimum;
      this.recommended = recommended;
    }

    public Optional<String> getMinimum() {
      return minimum;
    }

    public Optional<String> getRecommended() {
      return recommended;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      minimum.ifPresent(min -> json.put("minimum", min));
      recommended.ifPresent(rec -> json.put("recommended", rec));
      return json;
    }
  }

  public static class Metacritic implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int score;
    private final String url;

    public Metacritic(int score, String url) {
      this.score = score;
      this.url = url;
    }

    public int getScore() {
      return score;
    }

    public String getUrl() {
      return url;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("score", score);
      json.put("url", url);
      return json;
    }
  }

  public static class Recommendations implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int total;

    public Recommendations(int total) {
      this.total = total;
    }

    public int getTotal() {
      return total;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("total", total);
      return json;
    }
  }

  public static class Achievements implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int total;
    private final Optional<List<Highlighted>> highlighted;

    public Achievements(int total, Optional<List<Highlighted>> highlighted) {
      this.total = total;
      this.highlighted = highlighted;
    }

    public int getTotal() {
      return total;
    }

    public Optional<List<Highlighted>> getHighlighted() {
      return highlighted;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("total", total);
      highlighted.ifPresent(
          hl -> {
            JSONArray hlArray = new JSONArray();
            hl.forEach(h -> hlArray.put(h.toJson()));
            json.put("highlighted", hlArray);
          });
      return json;
    }

    public static class Highlighted implements Serializable {
      private static final long serialVersionUID = 1L;
      private final String name;
      private final String path;

      public Highlighted(String name, String path) {
        this.name = name;
        this.path = path;
      }

      public String getName() {
        return name;
      }

      public String getPath() {
        return path;
      }

      public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("path", path);
        return json;
      }
    }
  }

  public static class ReleaseDate implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean comingSoon;
    private final String date;

    public ReleaseDate(boolean comingSoon, String date) {
      this.comingSoon = comingSoon;
      this.date = date;
    }

    public boolean isComingSoon() {
      return comingSoon;
    }

    public String getDate() {
      return date;
    }

    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("coming_soon", comingSoon);
      json.put("date", date);
      return json;
    }
  }
}
