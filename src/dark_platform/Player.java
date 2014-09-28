package dark_platform;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Player {

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    private double x;
    private double y;
    private double dx;
    private double dy;

    private int width;
    private int height;


    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }

    private boolean left = false;
    private boolean right = false;


    private boolean jumping;
    private boolean falling;

    private double moveSpeed;
    private double stopSpeed;
    private double maxSpeed;
    private double maxFallingSpeed;
    private double jumpStart;
    private double gravity;

    private TileMap tileMap;

    private boolean topLeft;
    private boolean topRight;
    private boolean bottomRight;
    private boolean bottomLeft;

    private Animation animation;
    private BufferedImage[] idleSprites;
    private BufferedImage[] walkingSprites;
    private BufferedImage[] jumpingSprites;
    private BufferedImage[] fallingSprites;
    private boolean facingLeft = true;



    public Player(TileMap tm) {

        tileMap = tm;

        width = 22;
        height = 22;

        moveSpeed = 0.6;
        maxSpeed = 4.5;
        maxFallingSpeed = 12;
        stopSpeed = 0.30;
        jumpStart = -11.0;
        gravity = 0.6;

        try {
            idleSprites = new BufferedImage[1];
            fallingSprites = new BufferedImage[1];
            jumpingSprites = new BufferedImage[1];
            walkingSprites = new BufferedImage[6];

            idleSprites[0] = ImageIO.read(new File("src\\res\\Graphs\\Sprajty\\kirbyidle.gif"));
            fallingSprites[0] = ImageIO.read(new File("src\\res\\Graphs\\Sprajty\\kirbyfall.gif"));
            jumpingSprites[0] = ImageIO.read(new File("src\\res\\Graphs\\Sprajty\\kirbyjump.gif"));

            BufferedImage image = ImageIO.read(new File("src\\res\\Graphs\\Sprajty\\kirbywalk.gif"));
            for(int i = 0; i < walkingSprites.length; i++){
                walkingSprites[i] = image.getSubimage(
                        i * width +  i,
                        0,
                        width,
                        height
                );
            }

        }catch (Exception ups){
            ups.printStackTrace();
        }

        animation = new Animation();
        facingLeft = false;


    }


    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setJumping(boolean jumping) {
        if (!falling) {
            this.jumping = jumping;
        }
    }

    public void update() {
        if (left) {
            dx -= moveSpeed;
            if (dx < -maxSpeed) {
                dx = -maxSpeed;
            }
        } else if (right) {
            dx += moveSpeed;
            if (dx > maxSpeed) {
                dx = maxSpeed;
            }
        }
        if (dx > 0) {
            dx -= stopSpeed;
            if (dx < 0) {
                dx = 0;
            }
        } else if (dx < 0) {
            dx += stopSpeed;
            if (dx > 0) {
                dx = 0;
            }
        }
        if (jumping) {
            dy = jumpStart;
            falling = true;
            jumping = false;
        }
        if (falling) {
            dy += gravity;
            if (dy > maxFallingSpeed) {
                dy = maxFallingSpeed;
            }
        } else {
            dy = 0;
        }

        int currentCol = tileMap.getColTile((int) x);
        int currentRow = tileMap.getRowTile((int) y);

        double toX = x + dx;
        double toY = y + dy;

        double tempX = x;
        double tempY = y;

        calculateCorners(x, toY);
        if (dy < 0) {
            if (topLeft || topRight) {
                dy = 0;
                tempY = currentRow * tileMap.getTileSize() + height / 2;
            } else {
                tempY += dy;
            }
        } else if (dy > 0) {
            if (bottomLeft || bottomRight) {
                dy = 0;
                falling = false;
                tempY = (currentRow + 1) * tileMap.getTileSize() - height / 2;
            } else {
                tempY += dy;
            }
        }

        calculateCorners(toX, y);
        if (dx < 0) {
            if (topLeft || bottomLeft) {
                dx = 0;
                tempX = currentCol * tileMap.getTileSize() + width / 2;
            } else {
                tempX += dx;
            }
        }
        if (dx > 0) {
            if (topRight || bottomRight) {
                dx = 0;
                tempX = (currentCol + 1) *
                        tileMap.getTileSize() - width / 2;
            } else {
                tempX += dx;
            }
        }

        if (!falling) {
            calculateCorners(x, y + 1);
            if (!bottomLeft & !bottomRight) {
                falling = true;
            }
        }

        x = tempX;
        y = tempY;

        // MAP MOVE
        tileMap.setX((int)(GamePanel.WIDTH/2 - x));
        tileMap.setY((int) (GamePanel.HEIGHT / 2 - y));

        //sprite animation
        if(left || right){
            animation.setFrames(walkingSprites);
            animation.setDelay(100);
        }else {
            animation.setFrames(idleSprites);
            animation.setDelay(-1);
        }
        if(dy < 0){
            animation.setFrames(jumpingSprites);
            animation.setDelay(-1);
        }
        if(dy > 0) {
            animation.setFrames(fallingSprites);
            animation.setDelay(-1);
        }
        animation.update();
        if(dx < 0){
            facingLeft = true;
        }else if(dx > 0){
            facingLeft = false;
        }

    }

    public void draw(Graphics2D g) {
        int tx = tileMap.getX();
        int ty = tileMap.getY();

        if(facingLeft){
            g.drawImage(
                    animation.getImage(),
                    (int)(tx + x - width / 2),
                    (int)(ty + y - height / 2),
                    null
            );
        }else{
            g.drawImage(
                    animation.getImage(),
                    (int)(tx + x - width / 2 + width),
                    (int)(ty + y - height / 2),
                    -width,
                    height,
                    null
            );
        }
    }

    private void calculateCorners(double x, double y) {
        int leftTile = tileMap.getColTile((int) (x - width / 2));
        int rightTile = tileMap.getColTile((int) (x + width / 2) - 1);
        int topTile = tileMap.getRowTile((int) (y - height / 2));
        int bottomTile = tileMap.getRowTile((int) (y + height / 2) - 1);

        topLeft = tileMap.isBlocked(topTile, leftTile);
        topRight = tileMap.isBlocked(topTile, rightTile);
        bottomRight = tileMap.isBlocked(bottomTile, rightTile);
        bottomLeft = tileMap.isBlocked(bottomTile, leftTile);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
