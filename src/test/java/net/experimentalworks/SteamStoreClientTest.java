package net.experimentalworks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import reactor.test.StepVerifier;

class SteamStoreClientTest {

  @Mock private HttpClient mockHttpClient;

  @Mock private HttpResponse<String> mockResponse;

  private SteamStoreClient client;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    client = new SteamStoreClient(mockHttpClient);
  }

  @Test
  void testGetStoreDetailsSuccess() {
    String mockResponseBody =
        """
            {
              "10": {
                "success": true,
                "data": {
                  "type": "game",
                  "name": "Counter-Strike",
                  "steam_appid": 10,
                  "required_age": 0,
                  "is_free": false,
                  "detailed_description": "Play the world's number 1 online action game.",
                  "short_description": "Play CS.",
                  "header_image": "https://cdn.akamai.steamstatic.com/steam/apps/10/header.jpg",
                  "platforms": {
                    "windows": true,
                    "mac": true,
                    "linux": true
                  },
                  "price_overview": {
                    "currency": "USD",
                    "initial": 999,
                    "final": 999,
                    "discount_percent": 0,
                    "final_formatted": "$9.99"
                  },
                  "categories": [
                    {"id": 1, "description": "Multi-player"}
                  ],
                  "genres": [
                    {"id": "1", "description": "Action"}
                  ],
                  "release_date": {
                    "coming_soon": false,
                    "date": "1 Nov, 2000"
                  }
                }
              }
            }
            """;

    when(mockResponse.body()).thenReturn(mockResponseBody);
    when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));

    StepVerifier.create(client.getStoreDetails(List.of(10), Optional.empty(), Optional.empty()))
        .assertNext(
            detailsList -> {
              assertEquals(1, detailsList.size());
              StoreDetails details = detailsList.get(0);
              assertTrue(details.isSuccess());
              assertEquals(10, details.getAppId());
              assertEquals("Counter-Strike", details.getName());
              assertEquals("game", details.getType());
              assertFalse(details.isFree());
              assertTrue(details.getDetailedDescription().isPresent());
              assertTrue(details.getPriceOverview().isPresent());
              assertEquals("USD", details.getPriceOverview().get().getCurrency());
              assertEquals(999, details.getPriceOverview().get().getFinalPrice());
            })
        .verifyComplete();

    verify(mockHttpClient, times(1))
        .sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  @Test
  void testGetStoreDetailsMultipleApps() {
    String mockResponse1 =
        """
            {
              "10": {
                "success": true,
                "data": {
                  "type": "game",
                  "name": "Counter-Strike",
                  "steam_appid": 10,
                  "required_age": 0,
                  "is_free": false,
                  "platforms": {"windows": true, "mac": true, "linux": true},
                  "release_date": {"coming_soon": false, "date": "1 Nov, 2000"}
                }
              }
            }
            """;

    String mockResponse2 =
        """
            {
              "20": {
                "success": true,
                "data": {
                  "type": "game",
                  "name": "Team Fortress Classic",
                  "steam_appid": 20,
                  "required_age": 0,
                  "is_free": false,
                  "platforms": {"windows": true, "mac": true, "linux": true},
                  "release_date": {"coming_soon": false, "date": "1 Apr, 1999"}
                }
              }
            }
            """;

    when(mockResponse.body()).thenReturn(mockResponse1, mockResponse2);
    when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));

    StepVerifier.create(client.getStoreDetails(List.of(10, 20), Optional.empty(), Optional.empty()))
        .assertNext(
            detailsList -> {
              assertEquals(2, detailsList.size());
              assertTrue(detailsList.stream().allMatch(StoreDetails::isSuccess));
              assertTrue(
                  detailsList.stream()
                      .anyMatch(d -> d.getName().equals("Counter-Strike") && d.getAppId() == 10));
              assertTrue(
                  detailsList.stream()
                      .anyMatch(
                          d -> d.getName().equals("Team Fortress Classic") && d.getAppId() == 20));
            })
        .verifyComplete();

    verify(mockHttpClient, times(2))
        .sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  @Test
  void testGetStoreDetailsWithCountryAndLanguage() {
    String mockResponseBody =
        """
            {
              "10": {
                "success": true,
                "data": {
                  "type": "game",
                  "name": "Counter-Strike",
                  "steam_appid": 10,
                  "required_age": 0,
                  "is_free": false,
                  "platforms": {"windows": true, "mac": true, "linux": true},
                  "price_overview": {
                    "currency": "EUR",
                    "initial": 899,
                    "final": 899,
                    "discount_percent": 0
                  },
                  "release_date": {"coming_soon": false, "date": "1 Nov, 2000"}
                }
              }
            }
            """;

    when(mockResponse.body()).thenReturn(mockResponseBody);
    when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));

    StepVerifier.create(client.getStoreDetails(List.of(10), Optional.of("DE"), Optional.of("de")))
        .assertNext(
            detailsList -> {
              assertEquals(1, detailsList.size());
              StoreDetails details = detailsList.get(0);
              assertTrue(details.isSuccess());
              assertTrue(details.getPriceOverview().isPresent());
              assertEquals("EUR", details.getPriceOverview().get().getCurrency());
            })
        .verifyComplete();
  }

  @Test
  void testGetStoreDetailsApiReturnsFalse() {
    String mockResponseBody =
        """
            {
              "999999": {
                "success": false
              }
            }
            """;

    when(mockResponse.body()).thenReturn(mockResponseBody);
    when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));

    StepVerifier.create(client.getStoreDetails(List.of(999999), Optional.empty(), Optional.empty()))
        .assertNext(
            detailsList -> {
              assertEquals(1, detailsList.size());
              StoreDetails details = detailsList.get(0);
              assertFalse(details.isSuccess());
              assertEquals(999999, details.getAppId());
              assertTrue(details.getName().contains("Failed to fetch details"));
            })
        .verifyComplete();
  }

  @Test
  void testGetStoreDetailsHttpError() {
    when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Network error occurred")));

    StepVerifier.create(client.getStoreDetails(List.of(10), Optional.empty(), Optional.empty()))
        .assertNext(
            detailsList -> {
              assertEquals(1, detailsList.size());
              StoreDetails details = detailsList.get(0);
              assertFalse(details.isSuccess());
              assertEquals(10, details.getAppId());
              assertTrue(details.getName().contains("HTTP request failed"));
            })
        .verifyComplete();
  }

  @Test
  void testGetStoreDetailsInvalidJson() {
    String invalidJson = "{ invalid json }";

    when(mockResponse.body()).thenReturn(invalidJson);
    when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));

    StepVerifier.create(client.getStoreDetails(List.of(10), Optional.empty(), Optional.empty()))
        .assertNext(
            detailsList -> {
              assertEquals(1, detailsList.size());
              StoreDetails details = detailsList.get(0);
              assertFalse(details.isSuccess());
              assertTrue(details.getName().contains("Failed to parse response"));
            })
        .verifyComplete();
  }

  @Test
  void testGetStoreDetailsEmptyList() {
    StepVerifier.create(client.getStoreDetails(List.of(), Optional.empty(), Optional.empty()))
        .assertNext(detailsList -> assertTrue(detailsList.isEmpty()))
        .verifyComplete();

    verify(mockHttpClient, never())
        .sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  @Test
  void testGetStoreDetailsNullList() {
    StepVerifier.create(client.getStoreDetails(null, Optional.empty(), Optional.empty()))
        .assertNext(detailsList -> assertTrue(detailsList.isEmpty()))
        .verifyComplete();

    verify(mockHttpClient, never())
        .sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  @Test
  void testParseCompleteStoreDetails() {
    String mockResponseBody =
        """
            {
              "440": {
                "success": true,
                "data": {
                  "type": "game",
                  "name": "Team Fortress 2",
                  "steam_appid": 440,
                  "required_age": 0,
                  "is_free": true,
                  "controller_support": "full",
                  "detailed_description": "Team Fortress 2 is a team-based multiplayer FPS.",
                  "about_the_game": "Nine distinct classes provide a broad range of tactical abilities.",
                  "short_description": "Team-based FPS game.",
                  "supported_languages": "English, French, German",
                  "header_image": "https://cdn.akamai.steamstatic.com/steam/apps/440/header.jpg",
                  "website": "https://www.teamfortress.com/",
                  "platforms": {
                    "windows": true,
                    "mac": true,
                    "linux": true
                  },
                  "metacritic": {
                    "score": 92,
                    "url": "https://www.metacritic.com/game/pc/team-fortress-2"
                  },
                  "categories": [
                    {"id": 1, "description": "Multi-player"},
                    {"id": 9, "description": "Co-op"}
                  ],
                  "genres": [
                    {"id": "1", "description": "Action"}
                  ],
                  "screenshots": [
                    {
                      "id": 0,
                      "path_thumbnail": "https://cdn.akamai.steamstatic.com/steam/apps/440/ss_thumb.jpg",
                      "path_full": "https://cdn.akamai.steamstatic.com/steam/apps/440/ss_full.jpg"
                    }
                  ],
                  "recommendations": {
                    "total": 500000
                  },
                  "achievements": {
                    "total": 520,
                    "highlighted": [
                      {
                        "name": "First Blood",
                        "path": "https://cdn.akamai.steamstatic.com/steamcommunity/public/images/apps/440/achievement.jpg"
                      }
                    ]
                  },
                  "release_date": {
                    "coming_soon": false,
                    "date": "10 Oct, 2007"
                  },
                  "developers": ["Valve"],
                  "publishers": ["Valve"],
                  "pc_requirements": {
                    "minimum": "Minimum: CPU 1.7 GHz, RAM 512MB",
                    "recommended": "Recommended: CPU 3 GHz, RAM 1GB"
                  },
                  "dlc": [123, 456, 789]
                }
              }
            }
            """;

    when(mockResponse.body()).thenReturn(mockResponseBody);
    when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));

    StepVerifier.create(client.getStoreDetails(List.of(440), Optional.empty(), Optional.empty()))
        .assertNext(
            detailsList -> {
              assertEquals(1, detailsList.size());
              StoreDetails details = detailsList.get(0);

              assertTrue(details.isSuccess());
              assertEquals(440, details.getAppId());
              assertEquals("Team Fortress 2", details.getName());
              assertTrue(details.isFree());
              assertTrue(details.getControllerSupport().isPresent());
              assertEquals("full", details.getControllerSupport().get());

              assertTrue(details.getMetacritic().isPresent());
              assertEquals(92, details.getMetacritic().get().getScore());

              assertTrue(details.getCategories().isPresent());
              assertEquals(2, details.getCategories().get().size());

              assertTrue(details.getScreenshots().isPresent());
              assertEquals(1, details.getScreenshots().get().size());

              assertTrue(details.getRecommendations().isPresent());
              assertEquals(500000, details.getRecommendations().get().getTotal());

              assertTrue(details.getAchievements().isPresent());
              assertEquals(520, details.getAchievements().get().getTotal());
              assertTrue(details.getAchievements().get().getHighlighted().isPresent());

              assertTrue(details.getDevelopers().isPresent());
              assertEquals(List.of("Valve"), details.getDevelopers().get());

              assertTrue(details.getPcRequirements().isPresent());
              assertTrue(details.getPcRequirements().get().getMinimum().isPresent());

              assertTrue(details.getDlc().isPresent());
              assertEquals(List.of(123, 456, 789), details.getDlc().get());

              assertTrue(details.getPlatforms().isWindows());
              assertTrue(details.getPlatforms().isMac());
              assertTrue(details.getPlatforms().isLinux());
            })
        .verifyComplete();
  }
}
