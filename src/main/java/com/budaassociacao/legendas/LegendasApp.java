package com.budaassociacao.legendas;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
    private JLabel statusLabel;
    private JLabel countLabel;
    private JButton uploadButton;
    private JButton previousButton;
    private JButton nextButton;
    private JCheckBox alwaysOnTopCheckbox;
    private AutoUpdater autoUpdater;

    private List<String> legendas = new ArrayList<>();
    private int currentIndex = 0;

    private static final Color MAIN_BLUE = new Color(0x88A9CC);
    private static final Color LIGHT_BLUE = new Color(0xA8C4DC);
    private static final Color DARK_BLUE = new Color(0x3E7EBE);
    private static final Color TEXT_DARK = new Color(0x2C3E50);
    private static final Color BUTTON_GREEN = new Color(0x4CAF50);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
        setSize(600, 400);
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
            statusLabel.setText("Pronto para carregar legendas");
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
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(MAIN_BLUE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top panel with upload button and always on top checkbox
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(MAIN_BLUE);

        uploadButton = new JButton("Carregar Legendas (.docx)");
        uploadButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        uploadButton.setBackground(BUTTON_GREEN);
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFocusPainted(false);
        uploadButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_GREEN.darker(), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        uploadButton.addActionListener(e -> loadDocxFile());

        alwaysOnTopCheckbox = new JCheckBox("Sempre visível", true);
        alwaysOnTopCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        alwaysOnTopCheckbox.setBackground(MAIN_BLUE);
        alwaysOnTopCheckbox.setForeground(TEXT_DARK);
        alwaysOnTopCheckbox.setFocusPainted(false);
        alwaysOnTopCheckbox.addActionListener(e -> setAlwaysOnTop(alwaysOnTopCheckbox.isSelected()));
        setAlwaysOnTop(true);

        topPanel.add(uploadButton, BorderLayout.WEST);
        topPanel.add(alwaysOnTopCheckbox, BorderLayout.EAST);

        // Center panel with legend display
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(MAIN_BLUE);

        countLabel = new JLabel("Nenhuma legenda carregada", SwingConstants.CENTER);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLabel.setForeground(TEXT_DARK);

        legendaArea = new JTextArea();
        legendaArea.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        legendaArea.setLineWrap(true);
        legendaArea.setWrapStyleWord(true);
        legendaArea.setEditable(false);
        legendaArea.setBackground(Color.WHITE);
        legendaArea.setForeground(TEXT_DARK);
        legendaArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BLUE, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        legendaArea.setText("Clique em 'Carregar Legendas' para começar\n\n" +
                           "Use:\n" +
                           "• ESPAÇO ou → para próxima legenda\n" +
                           "• BACKSPACE ou ← para legenda anterior");

        JScrollPane scrollPane = new JScrollPane(legendaArea);
        scrollPane.setBorder(null);

        centerPanel.add(countLabel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with navigation buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        bottomPanel.setBackground(MAIN_BLUE);

        previousButton = new JButton("← Anterior");
        previousButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        previousButton.setBackground(DARK_BLUE);
        previousButton.setForeground(Color.WHITE);
        previousButton.setFocusPainted(false);
        previousButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BLUE.darker(), 2),
            BorderFactory.createEmptyBorder(10, 30, 10, 30)
        ));
        previousButton.setEnabled(false);
        previousButton.addActionListener(e -> previousLegenda());

        nextButton = new JButton("Próxima →");
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nextButton.setBackground(DARK_BLUE);
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BLUE.darker(), 2),
            BorderFactory.createEmptyBorder(10, 30, 10, 30)
        ));
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> nextLegenda());

        bottomPanel.add(previousButton);
        bottomPanel.add(nextButton);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(TEXT_DARK);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add all panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(MAIN_BLUE);
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.PAGE_END);

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
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Documentos Word (.docx)", "docx"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadLegendas(selectedFile);
        }
    }

    private void loadLegendas(File file) {
        try {
            statusLabel.setText("Carregando legendas...");

            // Parse DOCX file
            DocxParser parser = new DocxParser();
            legendas = parser.extractLegendas(file);

            if (legendas.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Nenhuma legenda encontrada no arquivo.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
                statusLabel.setText("Nenhuma legenda encontrada");
                return;
            }

            // Reset to first legend
            currentIndex = 0;
            updateLegendaDisplay();

            // Enable navigation buttons
            previousButton.setEnabled(false);
            nextButton.setEnabled(legendas.size() > 1);

            statusLabel.setText("Legendas carregadas com sucesso!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar o arquivo:\n" + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Erro ao carregar legendas");
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

        legendaArea.setText(legendas.get(currentIndex));
        legendaArea.setCaretPosition(0); // Scroll to top

        countLabel.setText(String.format("Legenda %d de %d", currentIndex + 1, legendas.size()));

        // Update button states
        previousButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < legendas.size() - 1);
    }
}
