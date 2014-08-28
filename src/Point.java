import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Point {

    private int width;
    private int height;
    private double x;
    private double y;
    private TileMap tileMap;
    private Player player;
    private boolean visible = true;


    private Animation animation;
    private BufferedImage[] Sprite;

    public Point(TileMap tm, Player pl) {
        tileMap = tm;
        player = pl;

        width = 15;
        height = 25;

        try {
            Sprite = new BufferedImage[2];
            BufferedImage image = ImageIO.read(new File("src\\res\\Graphs\\Sprajty\\point.gif"));

            for (int i = 0; i < Sprite.length; i++) {
                Sprite[i] = image.getSubimage(
                        i * 57,
                        0,
                        56,
                        114
                );
            }
        } catch (Exception ups) {
            ups.printStackTrace();
        }

        animation = new Animation();
    }

    public void draw(Graphics2D g) {

        int tx = tileMap.getX();
        int ty = tileMap.getY();
        if (visible) {
            g.drawImage(
                    animation.getImage(),
                    (int) (tx + x - width / 2),
                    (int) (ty + y - height / 2),
                    width, height, null
            );
        }
    }

    public void update() {

        if (visible) {
            if ((Math.round(x) > Math.round(player.getX()) - 5) &&
                    (Math.round(x) < Math.round(player.getX() + 15)) &&
                    (Math.round(y) > Math.round(player.getY()) - 5) &&
                    (Math.round(y) < Math.round(player.getY() + 15))
                    ) {
                visible = false;
            }
            animation.setFrames(Sprite);
            animation.setDelay(400);
            animation.update();
        }
    }


    public void setY(double y) {
        this.y = y;
    }


    public void setX(double x) {
        this.x = x;
    }

    public boolean isVisible() {
        return visible;
    }
}
