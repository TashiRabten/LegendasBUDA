package com.budaassociacao.legendas;

import com.budaassociacao.legendas.update.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;

/**
 * AutoUpdater for LegendasBUDA
 *
 * Features:
 * - Automatic update checking on startup
 * - Download progress tracking
 * - Release notes display
 * - Cross-platform support (Windows, macOS)
 */
public class AutoUpdater {
    private final JFrame parentFrame;
    private static boolean manualCheck = false;
    private static final String CURRENT_VERSION = VersionUtils.getCurrentVersion();
    private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/tashirabten/LegendasBUDA/releases/latest";

    public AutoUpdater(JFrame parent) {
        this.parentFrame = parent;
    }

    /**
     * Check for updates in blocking mode (runs before app initialization).
     */
    public void checkForUpdatesBlocking(Runnable onComplete) {
        manualCheck = false;
        System.out.println("[AutoUpdater] Current version: " + CURRENT_VERSION);

        SwingWorker<UpdateResult, Void> worker = new SwingWorker<>() {
            @Override
            protected UpdateResult doInBackground() throws Exception {
                return fetchUpdateInfo();
            }

            @Override
            protected void done() {
                try {
                    UpdateResult result = get();
                    if (result.hasUpdate) {
                        System.out.println("[AutoUpdater] Update available: " + result.latestVersion);
                        SwingUtilities.invokeLater(() ->
                            showUpdateDialog(result, onComplete));
                    } else {
                        System.out.println("[AutoUpdater] No update needed - continuing startup");
                        onComplete.run();
                    }
                } catch (Exception e) {
                    System.err.println("[AutoUpdater] Update check failed: " + e.getMessage());
                    e.printStackTrace();
                    onComplete.run();
                }
            }
        };

        worker.execute();
    }

    /**
     * Fetch update information from GitHub releases API
     */
    private UpdateResult fetchUpdateInfo() {
        try {
            URL url = new URL(GITHUB_RELEASES_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("[AutoUpdater] GitHub API returned: " + responseCode);
                return new UpdateResult(false, null, null, null, null);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {

                JsonObject release = JsonParser.parseReader(reader).getAsJsonObject();
                String tagName = release.get("tag_name").getAsString();
                String latestVersion = VersionUtils.parseVersionFromTag(tagName);
                String releaseUrl = release.get("html_url").getAsString();
                String releaseNotes = release.has("body") ? release.get("body").getAsString() : "";

                // Get download URL for appropriate platform
                String downloadUrl = getDownloadUrl(release);

                boolean hasUpdate = VersionUtils.isNewerVersion(latestVersion, CURRENT_VERSION);

                System.out.println("[AutoUpdater] Latest version: " + latestVersion);
                System.out.println("[AutoUpdater] Has update: " + hasUpdate);

                return new UpdateResult(hasUpdate, latestVersion, downloadUrl, releaseUrl, releaseNotes);
            }

        } catch (Exception e) {
            System.err.println("[AutoUpdater] Failed to check for updates: " + e.getMessage());
            return new UpdateResult(false, null, null, null, null);
        }
    }

    /**
     * Get the appropriate download URL based on OS
     */
    private String getDownloadUrl(JsonObject release) {
        String os = System.getProperty("os.name").toLowerCase();
        JsonArray assets = release.getAsJsonArray("assets");

        for (JsonElement assetElem : assets) {
            JsonObject asset = assetElem.getAsJsonObject();
            String name = asset.get("name").getAsString().toLowerCase();
            String downloadUrl = asset.get("browser_download_url").getAsString();

            if (os.contains("win") && name.endsWith(".exe")) {
                return downloadUrl;
            } else if (os.contains("mac") && (name.endsWith(".pkg") || name.endsWith(".dmg"))) {
                return downloadUrl;
            }
        }

        return null;
    }

    /**
     * Show update dialog to user
     */
    private void showUpdateDialog(UpdateResult result, Runnable onComplete) {
        int choice = JOptionPane.showConfirmDialog(
            parentFrame,
            "Uma nova versão está disponível!\n\n" +
            "Versão atual: " + CURRENT_VERSION + "\n" +
            "Nova versão: " + result.latestVersion + "\n\n" +
            "Deseja baixar a atualização?",
            "Atualização Disponível",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (result.downloadUrl != null) {
                downloadUpdate(result, onComplete);
            } else {
                // Open release page in browser
                try {
                    Desktop.getDesktop().browse(new URI(result.releaseUrl));
                    onComplete.run();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(parentFrame,
                        "Erro ao abrir o navegador.\n" +
                        "Acesse manualmente: " + result.releaseUrl,
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                    onComplete.run();
                }
            }
        } else {
            onComplete.run();
        }
    }

    /**
     * Download update file
     */
    private void downloadUpdate(UpdateResult result, Runnable onComplete) {
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        JDialog progressDialog = new JDialog(parentFrame, "Baixando atualização", true);
        progressDialog.setLayout(new BorderLayout(10, 10));
        progressDialog.add(new JLabel("Baixando " + result.latestVersion + "..."), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(400, 100);
        progressDialog.setLocationRelativeTo(parentFrame);

        SwingWorker<File, Integer> downloadWorker = new SwingWorker<>() {
            @Override
            protected File doInBackground() throws Exception {
                URL url = new URL(result.downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                long fileSize = conn.getContentLengthLong();

                String fileName = Paths.get(new URI(result.downloadUrl).getPath()).getFileName().toString();
                Path downloadPath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);

                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(downloadPath.toFile())) {

                    byte[] buffer = new byte[8192];
                    long totalRead = 0;
                    int bytesRead;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                        int progress = (int) ((totalRead * 100) / fileSize);
                        publish(progress);
                    }
                }

                return downloadPath.toFile();
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int latestProgress = chunks.get(chunks.size() - 1);
                progressBar.setValue(latestProgress);
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    File installerFile = get();
                    JOptionPane.showMessageDialog(parentFrame,
                        "Download concluído!\n\n" +
                        "O instalador será aberto automaticamente.\n" +
                        "Por favor, feche este aplicativo antes de instalar a atualização.",
                        "Download Concluído",
                        JOptionPane.INFORMATION_MESSAGE);

                    // Launch installer
                    Desktop.getDesktop().open(installerFile);
                    onComplete.run();

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(parentFrame,
                        "Erro ao baixar atualização:\n" + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                    onComplete.run();
                }
            }
        };

        downloadWorker.execute();
        progressDialog.setVisible(true);
    }
}
