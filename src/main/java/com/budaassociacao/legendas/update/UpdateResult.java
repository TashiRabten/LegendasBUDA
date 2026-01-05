package com.budaassociacao.legendas.update;

/**
 * Represents the result of an update check.
 */
public class UpdateResult {
    public final boolean hasUpdate;
    public final String latestVersion;
    public final String downloadUrl;
    public final String releaseUrl;
    public final String releaseNotes;

    public UpdateResult(boolean hasUpdate, String latestVersion, String downloadUrl,
                       String releaseUrl, String releaseNotes) {
        this.hasUpdate = hasUpdate;
        this.latestVersion = latestVersion;
        this.downloadUrl = downloadUrl;
        this.releaseUrl = releaseUrl;
        this.releaseNotes = releaseNotes;
    }
}
