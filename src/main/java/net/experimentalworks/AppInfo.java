package net.experimentalworks;

/**
 * Simple record to hold app ID and name from the Steam API.
 *
 * @param appId the Steam app ID
 * @param name the app name
 */
public record AppInfo(int appId, String name) {}
