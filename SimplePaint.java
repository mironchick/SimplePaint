import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SimplePaint {
    private static List<ColoredPoint> points = new ArrayList<>();
    private static Stack<List<ColoredPoint>> undoStack = new Stack<>();
    private static Color currentColor = Color.BLACK;
    private static int brushSize = 5;
    private static boolean isEraser = false;
    private static JPanel colorPreview;
    private static JPanel drawPanel;

    private static class ColoredPoint extends Point {
        Color color;
        boolean isEraser;
        public ColoredPoint(Point p, Color color, boolean isEraser) {
            super(p);
            this.color = color;
            this.isEraser = isEraser;
        }
    }

    private static void saveState() {
        List<ColoredPoint> state = new ArrayList<>(points);
        undoStack.push(state);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SimplePaint");
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(new BasicStroke(brushSize));
                for (int i = 1; i < points.size(); i++) {
                    ColoredPoint p1 = points.get(i-1);
                    ColoredPoint p2 = points.get(i);
                    if (p1 == null || p2 == null) continue;
                    g2d.setColor(p1.isEraser ? Color.WHITE : p1.color);
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        };

        JButton eraserButton = new JButton("Eraser");
        eraserButton.addActionListener(e -> {
            isEraser = !isEraser;
            eraserButton.setBackground(isEraser ? Color.GRAY : null);
        });

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            saveState();
            points.clear();
            drawPanel.repaint();
        });

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveImage());

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> {
            if (!undoStack.isEmpty()) {
                points = undoStack.pop();
                drawPanel.repaint();
            }
        });

        JPanel toolPanel = new JPanel(new BorderLayout());
        toolPanel.setBackground(Color.LIGHT_GRAY);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(eraserButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(undoButton);

        JColorChooser colorChooser = new JColorChooser(currentColor);
        AbstractColorChooserPanel[] panels = colorChooser.getChooserPanels();
        for (AbstractColorChooserPanel panel : panels) {
            String displayName = panel.getDisplayName();
            if (!displayName.equals("Swatches")) {
                colorChooser.removeChooserPanel(panel);
            }
        }
        colorChooser.setPreviewPanel(new JPanel());
        
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(40, 40));
        colorPreview.setBackground(currentColor);
        
        JTextField colorTextField = new JTextField(8);
        colorTextField.setText(currentColor.getRed() + " " + currentColor.getGreen() + " " + currentColor.getBlue());
        
        JButton applyColorButton = new JButton("Apply");
        applyColorButton.addActionListener(e -> {
            try {
                String[] rgb = colorTextField.getText().split(" ");
                if (rgb.length == 3) {
                    int r = Math.max(0, Math.min(255, Integer.parseInt(rgb[0])));
                    int g = Math.max(0, Math.min(255, Integer.parseInt(rgb[1])));
                    int b = Math.max(0, Math.min(255, Integer.parseInt(rgb[2])));
                    currentColor = new Color(r, g, b);
                    colorChooser.setColor(currentColor);
                    colorPreview.setBackground(currentColor);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Введите 3 числа от 0 до 255 через пробел", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        colorChooser.getSelectionModel().addChangeListener(e -> {
            currentColor = colorChooser.getColor();
            colorPreview.setBackground(currentColor);
            colorTextField.setText(
                currentColor.getRed() + " " +
                currentColor.getGreen() + " " +
                currentColor.getBlue()
            );
        });
        
        JPanel colorControlPanel = new JPanel();
        colorControlPanel.add(new JLabel("RGB:"));
        colorControlPanel.add(colorTextField);
        colorControlPanel.add(applyColorButton);
        colorControlPanel.add(colorPreview);

        toolPanel.add(buttonPanel, BorderLayout.WEST);
        toolPanel.add(colorChooser, BorderLayout.CENTER);
        toolPanel.add(colorControlPanel, BorderLayout.SOUTH);

        drawPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                saveState();
                points.add(new ColoredPoint(e.getPoint(), currentColor, isEraser));
                drawPanel.repaint();
            }
        
            @Override
            public void mouseReleased(MouseEvent e) {
                points.add(null);
            }
        });

        drawPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                points.add(new ColoredPoint(e.getPoint(), currentColor, isEraser));
                drawPanel.repaint();
            }
        });
        
        drawPanel.setBackground(Color.WHITE);
        frame.setLayout(new BorderLayout());
        frame.add(drawPanel, BorderLayout.CENTER);
        frame.add(toolPanel, BorderLayout.SOUTH);
        toolPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));   
        frame.setVisible(true);
    }

    private static void saveImage() {
        BufferedImage image = new BufferedImage(
            drawPanel.getWidth(), 
            drawPanel.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g2d = image.createGraphics();
        drawPanel.paint(g2d);
        g2d.dispose();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        fileChooser.setSelectedFile(new File("drawing.png"));
        
        int userSelection = fileChooser.showSaveDialog(drawPanel);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".png")) {
                fileToSave = new File(filePath + ".png");
            }
            
            try {
                ImageIO.write(image, "png", fileToSave);
                JOptionPane.showMessageDialog(drawPanel, 
                    "Изображение успешно сохранено!", 
                    "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(drawPanel, 
                    "Ошибка при сохранении изображения: " + ex.getMessage(), 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}