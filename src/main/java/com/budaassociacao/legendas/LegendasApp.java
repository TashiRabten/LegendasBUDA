package com.budaassociacao.legendas;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * LegendasBUDA - Visualizador de legendas para vídeos do YouTube
 *
 * Permite carregar legendas de um arquivo DOCX e navegar entre elas
 * enquanto assiste vídeos do YouTube.
 */
public class LegendasApp extends JFrame {

    private JTextArea legendaArea;
    private JButton uploadButton;
    private JButton previousButton;
    private JButton nextButton;
    private JCheckBox alwaysOnTopCheckbox;
    private AutoUpdater autoUpdater;
    private JComboBox<Integer> fontSizeCombo;

    private List<String> legendas = new ArrayList<>();
    private int currentIndex = 0;
    private int currentFontSize = 16;

    private static final Integer[] FONT_SIZES = {12, 14, 16, 18, 20, 24, 28, 32, 36, 40, 48};
    private static final int DEFAULT_FONT_SIZE = 16;

    private static final Color MAIN_BLUE = new Color(0x88A9CC);
    private static final Color LIGHT_BLUE = new Color(0xA8C4DC);
    private static final Color DARK_BLUE = new Color(0x3E7EBE);
    private static final Color TEXT_DARK = new Color(0x2C3E50);
    private static final Color BUTTON_GREEN = new Color(0x4CAF50);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Remove default UI styling for custom appearance
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

                // Disable default button hover effects
                UIManager.put("Button.select", new Color(0, 0, 0, 0));
                UIManager.put("Button.focus", new Color(0, 0, 0, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }

            LegendasApp app = new LegendasApp();
            app.setVisible(true);
        });
    }

    public LegendasApp() {
        super(getApplicationTitle());
        loadWindowIcon();
        initializeUI();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Check for updates on startup
        autoUpdater = new AutoUpdater(this);
        checkForUpdatesBeforeStart();
    }

    private void checkForUpdatesBeforeStart() {
        uploadButton.setEnabled(false);
        uploadButton.setText("Verificando atualizações...");

        autoUpdater.checkForUpdatesBlocking(() -> {
            uploadButton.setEnabled(true);
            uploadButton.setText("Carregar Legendas (.docx)");
        });
    }

    private static String getApplicationTitle() {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(LegendasApp.class.getResourceAsStream("/version.properties"));
            String version = props.getProperty("application.version", "1.0.0");
            return "LegendasBUDA v" + version;
        } catch (Exception e) {
            return "LegendasBUDA v1.0.0";
        }
    }

    private void loadWindowIcon() {
        try {
            URL iconUrl = getClass().getResource("/icons/BUDA.png");
            if (iconUrl != null) {
                ImageIcon windowIcon = new ImageIcon(iconUrl);
                setIconImage(windowIcon.getImage());
            }
        } catch (Exception e) {
            System.err.println("Não foi possível carregar o ícone: " + e.getMessage());
        }
    }

    private void initializeUI() {
        // Main panel with blue background
        JPanel mainPanel = new JPanel(new BorderLayout(0, 4));
        mainPanel.setBackground(MAIN_BLUE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 6, 4, 6));

        // Top panel with upload button, font size slider, and always on top checkbox
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBackground(MAIN_BLUE);

        uploadButton = new JButton("Carregar Legendas (.docx)");
        uploadButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        uploadButton.setBackground(BUTTON_GREEN);
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFocusPainted(false);
        uploadButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_GREEN.darker(), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        uploadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadButton.addActionListener(e -> loadDocxFile());

        // Custom hover effect
        uploadButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                uploadButton.setBackground(BUTTON_GREEN.darker());
            }
            public void mouseExited(MouseEvent e) {
                uploadButton.setBackground(BUTTON_GREEN);
            }
        });

        // Font size controls
        JPanel fontSizePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        fontSizePanel.setBackground(MAIN_BLUE);

        JLabel fontSizeLabel = new JLabel("Tamanho:");
        fontSizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fontSizeLabel.setForeground(TEXT_DARK);

        fontSizeCombo = new JComboBox<>(FONT_SIZES);
        fontSizeCombo.setSelectedItem(DEFAULT_FONT_SIZE);
        fontSizeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fontSizeCombo.setBackground(Color.WHITE);
        fontSizeCombo.setFocusable(false);
        fontSizeCombo.addActionListener(e -> {
            currentFontSize = (Integer) fontSizeCombo.getSelectedItem();
            updateLegendaFont();
            resizeToFitContent();
        });

        fontSizePanel.add(fontSizeLabel);
        fontSizePanel.add(fontSizeCombo);

        alwaysOnTopCheckbox = new JCheckBox("Sempre visível", true);
        alwaysOnTopCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        alwaysOnTopCheckbox.setBackground(MAIN_BLUE);
        alwaysOnTopCheckbox.setForeground(TEXT_DARK);
        alwaysOnTopCheckbox.setFocusPainted(false);
        alwaysOnTopCheckbox.addActionListener(e -> setAlwaysOnTop(alwaysOnTopCheckbox.isSelected()));
        setAlwaysOnTop(true);

        JPanel topLeftPanel = new JPanel(new BorderLayout(10, 0));
        topLeftPanel.setBackground(MAIN_BLUE);
        topLeftPanel.add(uploadButton, BorderLayout.WEST);
        topLeftPanel.add(fontSizePanel, BorderLayout.CENTER);

        topPanel.add(topLeftPanel, BorderLayout.CENTER);
        topPanel.add(alwaysOnTopCheckbox, BorderLayout.EAST);

        // Center panel with legend display and side buttons
        JPanel centerPanel = new JPanel(new BorderLayout(6, 0));
        centerPanel.setBackground(MAIN_BLUE);

        legendaArea = new JTextArea();
        legendaArea.setFont(getUnicodeFontWithSize(currentFontSize));
        legendaArea.setLineWrap(true);
        legendaArea.setWrapStyleWord(true);
        legendaArea.setEditable(false);
        legendaArea.setBackground(Color.WHITE);
        legendaArea.setForeground(TEXT_DARK);
        legendaArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BLUE, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        legendaArea.setColumns(30);
        legendaArea.setText("Carregar legendas\n\nEspaço = Avançar | Backspace = Voltar");
        legendaArea.setRows(2);

        // Side navigation buttons (no fixed height, will match text area)
        previousButton = new JButton("◄");
        previousButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        previousButton.setBackground(DARK_BLUE);
        previousButton.setForeground(Color.WHITE);
        previousButton.setFocusPainted(false);
        previousButton.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        previousButton.setEnabled(false);
        previousButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        previousButton.addActionListener(e -> previousLegenda());

        // Custom hover effect
        previousButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (previousButton.isEnabled()) {
                    previousButton.setBackground(DARK_BLUE.darker());
                }
            }
            public void mouseExited(MouseEvent e) {
                previousButton.setBackground(DARK_BLUE);
            }
        });

        nextButton = new JButton("►");
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nextButton.setBackground(DARK_BLUE);
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        nextButton.setEnabled(false);
        nextButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextButton.addActionListener(e -> nextLegenda());

        // Custom hover effect
        nextButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (nextButton.isEnabled()) {
                    nextButton.setBackground(DARK_BLUE.darker());
                }
            }
            public void mouseExited(MouseEvent e) {
                nextButton.setBackground(DARK_BLUE);
            }
        });

        centerPanel.add(legendaArea, BorderLayout.CENTER);
        centerPanel.add(previousButton, BorderLayout.WEST);
        centerPanel.add(nextButton, BorderLayout.EAST);

        // Add all panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Add keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        // Add key listener to the frame
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_SPACE:
                        case KeyEvent.VK_RIGHT:
                            if (nextButton.isEnabled()) {
                                nextLegenda();
                                return true;
                            }
                            break;
                        case KeyEvent.VK_BACK_SPACE:
                        case KeyEvent.VK_LEFT:
                            if (previousButton.isEnabled()) {
                                previousLegenda();
                                return true;
                            }
                            break;
                    }
                }
                return false;
            }
        });
    }

    private void loadDocxFile() {
        // Temporarily disable always-on-top so user can interact with both windows
        boolean wasAlwaysOnTop = isAlwaysOnTop();
        setAlwaysOnTop(false);

        // Use native file dialog (independent window)
        FileDialog fileDialog = new FileDialog((Frame) null, "Selecione o arquivo de legendas", FileDialog.LOAD);

        // Set file filter for .docx files
        fileDialog.setFile("*.docx");

        // Set initial directory to Documents
        String documentsPath = System.getProperty("user.home") + File.separator + "Documents";
        fileDialog.setDirectory(documentsPath);

        fileDialog.setVisible(true);

        String filename = fileDialog.getFile();
        String directory = fileDialog.getDirectory();

        // Restore previous always-on-top state
        setAlwaysOnTop(wasAlwaysOnTop);

        if (filename != null && directory != null) {
            File selectedFile = new File(directory, filename);
            loadLegendas(selectedFile);
        }
    }

    private void loadLegendas(File file) {
        try {

            // Parse DOCX file
            DocxParser parser = new DocxParser();
            legendas = parser.extractLegendas(file);

            if (legendas.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Nenhuma legenda encontrada no arquivo.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Reset to first legend
            currentIndex = 0;
            updateLegendaDisplay();

            // Enable navigation buttons
            previousButton.setEnabled(false);
            nextButton.setEnabled(legendas.size() > 1);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar o arquivo:\n" + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void nextLegenda() {
        if (currentIndex < legendas.size() - 1) {
            currentIndex++;
            updateLegendaDisplay();
        }
    }

    private void previousLegenda() {
        if (currentIndex > 0) {
            currentIndex--;
            updateLegendaDisplay();
        }
    }

    private void updateLegendaDisplay() {
        if (legendas.isEmpty()) {
            return;
        }

        String text = legendas.get(currentIndex);
        legendaArea.setText(text);
        legendaArea.setCaretPosition(0); // Scroll to top

        // Adjust rows based on content (most subtitles are 2 lines, max 4-6)
        int lineCount = text.split("\n").length;
        // Start with 2 rows, grow only if needed
        int estimatedRows = Math.max(2, Math.min(lineCount, 8));
        legendaArea.setRows(estimatedRows);

        // Update button states
        previousButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < legendas.size() - 1);

        // Resize to fit content
        resizeToFitContent();
    }

    /**
     * Get Unicode font with specified size for Tibetan and Chinese support
     */
    private Font getUnicodeFontWithSize(int size) {
        // Try fonts in order of preference for Unicode support
        String[] fontNames = {"Arial Unicode MS", "Noto Sans", "Microsoft YaHei", Font.SANS_SERIF};

        for (String fontName : fontNames) {
            Font font = new Font(fontName, Font.PLAIN, size);
            if (font.getFamily().equals(fontName) || fontName.equals(Font.SANS_SERIF)) {
                return font;
            }
        }

        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    /**
     * Update the legend area font size
     */
    private void updateLegendaFont() {
        legendaArea.setFont(getUnicodeFontWithSize(currentFontSize));

        // Recalculate rows if we have content
        if (!legendas.isEmpty()) {
            String text = legendas.get(currentIndex);
            int lineCount = text.split("\n").length;
            int estimatedRows = Math.max(2, Math.min(lineCount, 8));
            legendaArea.setRows(estimatedRows);
        }

        legendaArea.revalidate();
        legendaArea.repaint();
    }

    /**
     * Resize window to fit content dynamically
     */
    private void resizeToFitContent() {
        pack();

        // Ensure reasonable minimum width only (no minimum height!)
        Dimension currentSize = getSize();
        int minWidth = 400;

        if (currentSize.width < minWidth) {
            setSize(minWidth, currentSize.height);
        }

        revalidate();
        repaint();
    }
}
