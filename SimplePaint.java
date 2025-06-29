import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SimplePaint {
    private static List<ColoredPoint> points = new ArrayList<>();
    private static Color currentColor = Color.BLACK;
    private static int brushSize = 5;
    private static boolean isEraser = false;
    private static JPanel colorPreview;
    private static class ColoredPoint extends Point {
        Color color;
        boolean isEraser;
        public ColoredPoint(Point p, Color color, boolean isEraser) {
            super(p);
            this.color = color;
            this.isEraser = isEraser;
        }
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("SimplePaint");
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel drawPanel = new JPanel() {
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
            points.clear();
            drawPanel.repaint();
        });
        JPanel toolPanel = new JPanel();
        toolPanel.setBackground(Color.LIGHT_GRAY);
        toolPanel.add(eraserButton);
        toolPanel.add(clearButton);
        JPanel colorChooserPanel = new JPanel(new BorderLayout());
        JColorChooser colorChooser = new JColorChooser(currentColor);
        AbstractColorChooserPanel[] panels = colorChooser.getChooserPanels();
        for (AbstractColorChooserPanel panel : panels) {
            String displayName = panel.getDisplayName();
            if (!displayName.equals("Swatches") && !displayName.equals("HSV")) {
                colorChooser.removeChooserPanel(panel);
            }
        }
        JTextField colorTextField = new JTextField(
            currentColor.getRed() + " " + 
            currentColor.getGreen() + " " + 
            currentColor.getBlue(), 10);
        
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
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(50, 50));
        colorPreview.setBackground(currentColor);
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.add(colorChooser, BorderLayout.CENTER);
        JPanel textFieldPanel = new JPanel();
        textFieldPanel.add(new JLabel("RGB:"));
        textFieldPanel.add(colorTextField);
        textFieldPanel.add(applyColorButton);
        colorChooserPanel.add(colorChooser, BorderLayout.CENTER);
        colorChooserPanel.add(textFieldPanel, BorderLayout.SOUTH);

        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(50, 50));
        colorPreview.setBackground(currentColor);
        toolPanel.add(colorChooserPanel);
        toolPanel.add(colorPreview);
        drawPanel.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
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
        frame.setVisible(true);
    }
}