import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SimplePaint {
    private static List<Point> points = new ArrayList<>();
    private static Color currentColor = Color.BLACK;
    private static int brushSize = 5;
    private static boolean isEraser = false;
    private static int red = 0;
    private static int green = 0;
    private static int blue = 0;
    private static JPanel colorPreview;
    private static void updateCurrentColor() {
        currentColor = new Color(red, green, blue);
        if(colorPreview != null) {
            colorPreview.setBackground(currentColor);
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
                    Point p1 = points.get(i-1);
                    Point p2 = points.get(i);
                    if (p1 == null || p2 == null) continue;
                    try {
                    boolean isEraserPoint = p1.getClass().getDeclaredField("isEraser").getBoolean(p1);
                    g2d.setColor(isEraserPoint ? Color.WHITE : currentColor);
                    } catch (Exception e) {
                        g2d.setColor(currentColor);
                    }
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
        JSlider redSlider =  new JSlider(0, 255, 0);
        redSlider.addChangeListener(e -> {
            red = redSlider.getValue();
            updateCurrentColor();
        });
        JSlider greenSlider = new JSlider(0, 255, 0);
        greenSlider.addChangeListener(e -> {
            green = greenSlider.getValue();
            updateCurrentColor();
        });
        JSlider blueSlider = new JSlider(0, 255, 0);
        blueSlider.addChangeListener(e -> {
            blue = blueSlider.getValue();
            updateCurrentColor();
        });
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(50, 50));
        colorPreview.setBackground(currentColor);
        toolPanel.add(new JLabel("R:"));
        toolPanel.add(redSlider);
        toolPanel.add(new JLabel("G:"));
        toolPanel.add(greenSlider);
        toolPanel.add(new JLabel("B:"));
        toolPanel.add(blueSlider);
        toolPanel.add(colorPreview);
        drawPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEraser) {
                    points.add(new Point(e.getPoint()) {
                        public boolean isEraser = true;
                    });
                } else {
                    points.add(e.getPoint());
                }
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
                if (isEraser) {
                    points.add(new Point(e.getPoint()) {
                        public boolean isEraser = true;
                    });
                } else {
                    points.add(e.getPoint());
                }
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