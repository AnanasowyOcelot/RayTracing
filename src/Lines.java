import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;


public class Lines {

    private TileMap tileMap;
    private Player player;
    private java.util.List<Point2D> points = new ArrayList<Point2D>();
    private java.util.List<Rectangle2D> rects = new ArrayList<Rectangle2D>();
    private java.util.List<Line2D> mapOutline = new ArrayList<Line2D>();
    private Line2D line = new Line2D.Float();


    public Lines(TileMap tm, Player pl) {
        tileMap = tm;
        player = pl;
        this.createPoints();
        this.createMapOutline();
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.WHITE);

        for (Point2D point : points) {
            line.setLine(200, 200, (int) point.getX() + getMapStartX() - 150, (int) point.getY() + getMapStartY() - 150);

            int intersections = 0;
            for(Line2D outline : mapOutline){
                Line2D dynamicOutline = new Line2D.Float((int)outline.getX1()+ getMapStartX() - 150,(int)outline.getY1()+ getMapStartY() - 150,(int)outline.getX2()+ getMapStartX() - 150,(int)outline.getY2()+ getMapStartY() - 150);

                if(line.intersectsLine(dynamicOutline)){
                    intersections++;
                }
            }
            if(intersections < 3){
                g.draw(line);
            }

//            g.drawRect((int) point.getX() + getMapStartX() - 150, (int) point.getY() + getMapStartY() - 150, 3, 3);
        }
        for (Line2D line : mapOutline) {
            Line2D dynamicLine = new Line2D.Float((int)line.getX1()+ getMapStartX() - 150,(int)line.getY1()+ getMapStartY() - 150,(int)line.getX2()+ getMapStartX() - 150,(int)line.getY2()+ getMapStartY() - 150);
            g.draw(dynamicLine);
        }
    }

    public void createPoints() {
        java.util.List<Point2D> pointsTmp = new ArrayList<Point2D>();

        for (int row = 0; row < tileMap.getMapHeight(); row++) {
            for (int col = 0; col < tileMap.getMapWidth(); col++) {

                int rowSized = row * tileMap.getTileSize();
                int colSized = col * tileMap.getTileSize();
                int tileSize = tileMap.getTileSize();


                Point2D RightBottom = new Point2D.Float(getMapStartX() + colSized + tileSize, getMapStartY() + rowSized + tileSize);
                Point2D LeftTop = new Point2D.Float(getMapStartX() + colSized, getMapStartY() + rowSized);
                Point2D RightTop = new Point2D.Float(getMapStartX() + colSized + tileSize, getMapStartY() + rowSized);
                Point2D LeftBottom = new Point2D.Float(getMapStartX() + colSized, getMapStartY() + rowSized + tileSize);


                pointsTmp.add(RightTop);
                pointsTmp.add(RightBottom);
                pointsTmp.add(LeftTop);
                pointsTmp.add(LeftBottom);
//                }
            }
            for (Point2D point : pointsTmp) {
                boolean threeIsBlocked = tileMap.isBlocked((int) ((point.getY() - 150) / tileMap.getTileSize()), (int) ((point.getX() - 150) / tileMap.getTileSize()));
                boolean fourIsBlocked = tileMap.isBlocked((int) ((point.getY() - 150) / tileMap.getTileSize()), (int) ((point.getX() - 150) / tileMap.getTileSize()) - 1);
                boolean twoIsBlocked = tileMap.isBlocked((int) ((point.getY() - 150) / tileMap.getTileSize() - 1), (int) ((point.getX() - 150) / tileMap.getTileSize()));
                boolean oneIsBlocked = tileMap.isBlocked((int) ((point.getY() - 150) / tileMap.getTileSize() - 1), (int) ((point.getX() - 150) / tileMap.getTileSize()) - 1);

                if (!((oneIsBlocked == twoIsBlocked && threeIsBlocked == fourIsBlocked) || (oneIsBlocked == fourIsBlocked && twoIsBlocked == threeIsBlocked))) {
                    points.add(point);
                }
            }
        }
    }

    private void createMapOutline() {
        java.util.List<Line2D> mapOutlineTmp = new ArrayList<Line2D>();

        for (int row = 0; row < tileMap.getMapHeight(); row++) {
            for (int col = 0; col < tileMap.getMapWidth(); col++) {
                if (tileMap.isBlocked(row, col)) {
                    int rowSized = row * tileMap.getTileSize();
                    int colSized = col * tileMap.getTileSize();
                    int tileSize = tileMap.getTileSize();

                    Point2D RightBottom = new Point2D.Float(getMapStartX() + colSized + tileSize, getMapStartY() + rowSized + tileSize);
                    Point2D LeftTop = new Point2D.Float(getMapStartX() + colSized, getMapStartY() + rowSized);
                    Point2D RightTop = new Point2D.Float(getMapStartX() + colSized + tileSize, getMapStartY() + rowSized);
                    Point2D LeftBottom = new Point2D.Float(getMapStartX() + colSized, getMapStartY() + rowSized + tileSize);

                    Line2D topLine = new Line2D.Float(LeftTop, RightTop);
                    Line2D bottomLine = new Line2D.Float(LeftBottom, RightBottom);
                    Line2D rightLine = new Line2D.Float(RightTop, RightBottom);
                    Line2D leftLine = new Line2D.Float(LeftTop, LeftBottom);

                    if(!tileMap.isBlocked(row-1, col)){
                        mapOutlineTmp.add(topLine);
                    }
                    if(!tileMap.isBlocked(row+1, col)){
                        mapOutlineTmp.add(bottomLine);
                    }
                    if(!tileMap.isBlocked(row, col+1)){
                        mapOutlineTmp.add(rightLine);
                    }
                    if(!tileMap.isBlocked(row, col-1)){
                        mapOutlineTmp.add(leftLine);
                    }
                }
            }
        }
        System.out.println(mapOutlineTmp.size());

        for(Line2D outline: mapOutlineTmp){
            for(Line2D outline2: mapOutlineTmp){
                connectLines(outline, outline2);
            }
        }
        System.out.println(mapOutline.size());

//        java.util.List<Line2D> mapOutlineTmp2 = new ArrayList<Line2D>();


//        for(Line2D outline: mapOutlineTmp2){
//            mapOutline.add(outline);
//        }
    }

    private void connectLines(Line2D outline, Line2D outline2) {
        if( (outline.getX2() == outline2.getX1() && outline.getY2() == outline2.getY1()) && (outline.getX1() == outline2.getX2() || outline.getY1() == outline2.getY2())){
            Line2D newOutline = new Line2D.Float((int)outline.getX1(), (int)outline.getY1(), (int)outline2.getX2(), (int)outline2.getY2());
            if((newOutline.getX2() == outline2.getX1() && newOutline.getY2() == outline2.getY1()) && (newOutline.getX1() == outline2.getX2() || newOutline.getY1() == outline2.getY2())){
                Line2D newOutline2 = new Line2D.Float((int)newOutline.getX1(), (int)newOutline.getY1(), (int)outline2.getX2(), (int)outline2.getY2());
                connectLines(newOutline2, outline2);
            }else{
                mapOutline.add(newOutline);
            }
        }
    }


    private int getMapEndY() {
        return getMapStartY() + tileMap.getMapHeight() * tileMap.getTileSize();
    }

    private int getMapEndX() {
        return getMapStartX() + tileMap.getMapWidth() * tileMap.getTileSize();

    }

    private int getMapStartY() {
        return 200 - (int) player.getY();
    }

    private int getMapStartX() {
        return 200 - (int) player.getX();
    }

    public void update() {

    }
}
