package dark_platform;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;


public class Lines {

    private TileMap tileMap;
    private Player player;
    private ArrayList<Point2D> visibleCornerPoints = new ArrayList<Point2D>();
    private Set<Line2D> mapOutline = new HashSet<Line2D>();
    private ArrayList<Line2D> linesToDisplay = new ArrayList<Line2D>();


    public Lines(TileMap tm, Player pl) {
        tileMap = tm;
        player = pl;
        this.createVisibleCornerPoints();
        this.createMapOutline();
    }

    public void draw(Graphics2D g) {
        int mapXModifier = (int) (50 - player.getX());
        int mapYModifier = (int) (50 - player.getY());

        linesToDisplay.clear();


        for (Point2D visibleCornerPoint : visibleCornerPoints) {

            Line2D cornerLine = new Line2D.Float(GamePanel.WIDTH / 2, GamePanel.HEIGHT / 2, (int) (visibleCornerPoint.getX() + mapXModifier), (int) visibleCornerPoint.getY() + mapYModifier);

            int intersections = 0;
            for (Line2D outline : mapOutline) {
                Line2D outlineModified = new Line2D.Float((int) outline.getX1() + mapXModifier, (int) outline.getY1() + mapYModifier, (int) outline.getX2() + mapXModifier, (int) outline.getY2() + mapYModifier);
                g.setColor(Color.WHITE);
                g.draw(outlineModified);

                if (cornerLine.intersectsLine(outlineModified)) {
                    intersections++;
                }
            }
            if (intersections < 3) {
                linesToDisplay.add(cornerLine);
            }
//            g.drawRect((int) visibleCornerPoint.getX() + mapXModifier, (int) visibleCornerPoint.getY() + mapYModifier, 4, 4);
        }

        sortLines(linesToDisplay);
        ArrayList<Line2D> middleLines = createLinesBetween(linesToDisplay);

        int j = 0;
        for (Line2D line : linesToDisplay) {
            if (j == 0) {
                g.setColor(Color.GREEN);
            }else if(j == 2){
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.WHITE);
            }
            g.draw(line);
            j++;
        }

        int i = 0;
        for (Line2D line : middleLines) {
            g.setColor(new Color(255, i, 0));
            i += 55;
            if (i > 255) {
                i = 0;
            }
            g.draw(line);
        }
    }


    private ArrayList<Line2D> createLinesBetween(ArrayList<Line2D> lines) {
        ArrayList<Line2D> middleLines = new ArrayList<Line2D>();

        Line2D prev = lines.get(lines.size() - 1);
        for (Line2D line : lines) {
            Line2D current = line;
            double angle = angleBetween2Lines(current, prev);

            Line2D middleLine = new Line2D.Float(
                    (int) prev.getX1(),
                    (int) prev.getY1(),
                    (int) (prev.getX1() + 500 * Math.cos(angle)),
                    (int) (prev.getY1() + 500 * Math.sin(angle))
            );

            middleLines.add(middleLine);
            prev = current;
        }
        return middleLines;
    }

    private double angleBetween2Lines(Line2D line1, Line2D line2) {
        double angle1 = getAngle(line1);
        double angle2 = getAngle(line2);

        if (Math.abs(angle1 - angle2) > Math.PI) {
            return (angle1 + angle2) / 2 + Math.PI;
        } else {
            return (angle1 + angle2) / 2;
        }
    }

    private double getAngle(Line2D line) {
        return Math.atan2(line.getY2() - line.getY1(),
                line.getX2() - line.getX1());
    }


    // TODO: to źle działa!!!
    private void sortLines(ArrayList<Line2D> lines) {
        Collections.sort(lines, new Comparator<Line2D>() {
            @Override
            public int compare(Line2D o1, Line2D o2) {
                return (int)((o1.getX2() - o1.getX1()) * (o2.getY2() - o1.getY1()) - (o2.getX2() - o1.getX1()) * (o1.getY2() - o1.getY1()));
            }
        });
    }

    public void createVisibleCornerPoints() {

        for (int row = 0; row < tileMap.getMapHeight(); row++) {
            for (int col = 0; col < tileMap.getMapWidth(); col++) {

                boolean threeIsBlocked = tileMap.isBlocked(row, col);
                boolean fourIsBlocked = tileMap.isBlocked(row - 1, col);
                boolean twoIsBlocked = tileMap.isBlocked(row, col - 1);
                boolean oneIsBlocked = tileMap.isBlocked(row - 1, col - 1);

                if (!((oneIsBlocked == twoIsBlocked && threeIsBlocked == fourIsBlocked) || (oneIsBlocked == fourIsBlocked && twoIsBlocked == threeIsBlocked))) {
                    visibleCornerPoints.add(new Point2D.Float(getMapStartX() + col * tileMap.getTileSize(), getMapStartY() + row * tileMap.getTileSize()));
                }
            }
        }
    }


    private void createMapOutline() {
        ArrayList<Line2D> mapOutlineTmp = new ArrayList<Line2D>();
        ArrayList<Point2D> tileCorners;
        int tileSize = tileMap.getTileSize();

        for (int row = 0; row < tileMap.getMapHeight(); row++) {
            for (int col = 0; col < tileMap.getMapWidth(); col++) {
                if (tileMap.isBlocked(row, col)) {

                    tileCorners = getTileCornersArray(col, row, tileSize);

                    int rowOffset[] = new int[]{-1, 0, 1, 0};
                    int colOffset[] = new int[]{0, 1, 0, -1};
                    Point2D prevCorner = tileCorners.get(tileCorners.size() - 1);
                    int i = 0;
                    for (Point2D corner : tileCorners) {
                        if (!tileMap.isBlocked(row + rowOffset[i], col + colOffset[i])) {
                            mapOutlineTmp.add(new Line2D.Float(prevCorner, corner));
                        }
                        prevCorner = corner;
                        i++;
                    }
                }
            }
        }
        connectLines(mapOutlineTmp);
        for (Line2D line : mapOutlineTmp) {
            mapOutline.add(line);
        }
    }

    private ArrayList<Point2D> getTileCornersArray(int col, int row, int tileSize) {
        int Y = getMapStartY() + row * tileSize;
        int X = getMapStartX() + col * tileSize;
        ArrayList<Point2D> corners = new ArrayList<Point2D>();
        corners.add(new Point2D.Float(X + tileSize, Y));
        corners.add(new Point2D.Float(X + tileSize, Y + tileSize));
        corners.add(new Point2D.Float(X, Y + tileSize));
        corners.add(new Point2D.Float(X, Y));
        return corners;
    }


    private void connectLines(ArrayList<Line2D> lines) {
        for (int i = 0; i < 4; i++) {
            Line2D line1 = lines.get(0);
            Line2D line2 = null;
            lines.remove(line1);

            for (Line2D outline : lines) {
                if ((line1.getX2() == outline.getX1() && line1.getY2() == outline.getY1()) && (line1.getX1() == outline.getX2() || line1.getY1() == outline.getY2())) {
                    line2 = outline;
                }
            }
            if (line2 != null) {
                Line2D newLine = new Line2D.Float((int) line1.getX1(), (int) line1.getY1(), (int) line2.getX2(), (int) line2.getY2());
                lines.remove(line2);
                lines.add(newLine);
            } else {
                lines.add(line1);
            }
        }
    }


    private int getMapStartY() {
        return 200 - (int) player.getY();

    }

    private int getMapStartX() {
        return 200 - (int) player.getX();
    }

}
