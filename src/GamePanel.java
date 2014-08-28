import org.omg.CORBA.TIMEOUT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class GamePanel extends JPanel implements Runnable, KeyListener {

    public static final int WIDTH = 400;
    public static final int HEIGHT = 400;

    private Thread thread;
    private boolean running;

    private BufferedImage image;
    private Graphics2D g;

    private int FPS = 30;
    private int targetTime = 1000 / FPS;

    private TileMap tileMap;
    private Player player;
    private Lines lines;
    private List<Point> points = new ArrayList<Point>();
    private int pointsCollected = 0;

    private boolean paused = false;

    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
    }

    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        addKeyListener(this);
    }

    public void reset() {
        player.setX(50);
        player.setY(50);
        pointsCollected = 0;
        addPoints();

    }

    public void pause() {
        if (!paused) {
            paused = true;
        } else {
            paused = false;
        }
    }


    public void run() {
        init();

        long startTime;
        long urdTime;
        long waitTime;

        while (running) {

            startTime = System.nanoTime();

            update();
            render();
            draw();

            urdTime = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - urdTime;

            if(waitTime < 0){
                waitTime = 0;
                System.out.println(urdTime);
            }
            try {
                Thread.sleep(waitTime);
            } catch (Exception ups) {
                ups.printStackTrace();
            }
        }
    }

    private void init() {
        running = true;

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();

        tileMap = new TileMap("src\\res\\testMap2.txt", 32);
        tileMap.loadTiles("src\\res\\Graphs\\tileset.gif");
        player = new Player(tileMap);
        player.setX(50);
        player.setY(50);

        addPoints();
        lines = new Lines(tileMap, player);
    }

    private void addPoints() {
        int pointX = 0;

        for (int i = 0; i < 4; i++) {
            Point point = new Point(tileMap, player);
            point.setX(300 + pointX);
            point.setY(230);
            points.add(point);

            pointX += 20;
        }
        points.get(points.size() - 1).setX(125);
        points.get(points.size() - 1).setY(155);
    }

    private void update() {
        if (paused) {
            return;
        }
        tileMap.update();
        player.update();
        for (int i = 0; i < points.size(); i++) {
            points.get(i).update();
            if (!points.get(i).isVisible()) {
                pointsCollected++;
                points.remove(i);
            }
        }

        Menubar.labelka.setText("Liczba punktów: " + Integer.toString(getPointsCollected()) + " ");
    }

    private void addWinLabel() {
        add(new JLabel("pac"));
    }

    private void render() {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        tileMap.draw(g);
        player.draw(g);
        lines.draw(g);
        for(Point point: points){
            point.draw(g);
        }
    }

    private void draw() {
        Graphics g2 = getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        g2.setColor(Color.WHITE);
        g2.drawLine(0, 0, 100, 100);

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_LEFT) {
            player.setLeft(true);
        }
        if (code == KeyEvent.VK_RIGHT) {
            player.setRight(true);
        }
        if (code == KeyEvent.VK_SPACE) {
            player.setJumping(true);
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_LEFT) {
            player.setLeft(false);
        }
        if (code == KeyEvent.VK_RIGHT) {
            player.setRight(false);
        }

    }

    public int getPointsCollected() {
        return pointsCollected;
    }
}
