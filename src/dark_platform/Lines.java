package dark_platform;

import java.awt.*;
import java.awt.GradientPaint;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

public class Lines {

    private TileMap tileMap;
    private Player player;
    private ArrayList<Point2D> visibleCornerPoints = new ArrayList<Point2D>();
    private ArrayList<Line2D> mapOutline = new ArrayList<Line2D>();


    public Lines(TileMap tm, Player pl) {
        tileMap = tm;
        player = pl;
        this.createVisibleCornerPoints();
        this.createMapOutline();
    }

    public void draw(Graphics2D g) {
        int mapXModifier = (int) (50 - player.getX());
        int mapYModifier = (int) (50 - player.getY());

        ArrayList<Line2D> mapOutLineMOD = new ArrayList<Line2D>();
        for (Line2D outline : mapOutline) {
            Line2D outlineMOD = new Line2D.Float((int) outline.getX1() + mapXModifier, (int) outline.getY1() + mapYModifier, (int) outline.getX2() + mapXModifier, (int) outline.getY2() + mapYModifier);
            mapOutLineMOD.add(outlineMOD);
        }

        ArrayList<Line2D> linesToDisplayMOD = new ArrayList<Line2D>();
        for (Point2D visibleCornerPoint : visibleCornerPoints) {
            Line2D cornerLineMOD = new Line2D.Float(GamePanel.WIDTH / 2, GamePanel.HEIGHT / 2, (int) (visibleCornerPoint.getX() + mapXModifier), (int) visibleCornerPoint.getY() + mapYModifier);
            int intersections = 0;
            for (Line2D outlineMOD : mapOutLineMOD) {
                if (cornerLineMOD.intersectsLine(outlineMOD)) {
                    intersections++;
                }
            }
            if (intersections < 3) {
                linesToDisplayMOD.add(cornerLineMOD);
            }
        }

        sortLines(linesToDisplayMOD);

        Line2D prevMOD = linesToDisplayMOD.get(linesToDisplayMOD.size() - 1);
        ArrayList<Integer> visiblePointsX = new ArrayList<Integer>();
        ArrayList<Integer> visiblePointsY = new ArrayList<Integer>();
        for (Line2D currentMOD : linesToDisplayMOD) {
            Triangle visibleTriangle = createTriangleFromLines(prevMOD, currentMOD, mapOutLineMOD);

            if (visibleTriangle != null) {
                visiblePointsX.add((int) visibleTriangle.getC().getX());
                visiblePointsY.add((int) visibleTriangle.getC().getY());
                visiblePointsX.add((int) visibleTriangle.getB().getX());
                visiblePointsY.add((int) visibleTriangle.getB().getY());
            }

            prevMOD = currentMOD;
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, new Color(7, 14, 54), 0, tileMap.getMapHeight()*tileMap.getTileSize(), Color.BLACK));
        g.setClip(new Polygon(convertIntegers(visiblePointsX), convertIntegers(visiblePointsY), visiblePointsX.size()));
        g.fillPolygon(convertIntegers(visiblePointsX), convertIntegers(visiblePointsY), visiblePointsX.size());
    }

    public static int[] convertIntegers(ArrayList<Integer> integers) {
        int[] simpleArray = new int[integers.size()];
        for (int i = 0; i < simpleArray.length; i++) {
            simpleArray[i] = integers.get(i);
        }
        return simpleArray;
    }

    private Triangle createTriangleFromLines(Line2D line1, Line2D line2, ArrayList<Line2D> outline) {
        Line2D middleLine = createLineBetween(line1, line2);
        Line2D partOfOutlineIntersectingWithMiddleLine = findFirstIntersectionLine(middleLine, outline);
        return extendLinesToCreateTriangle(line1, line2, partOfOutlineIntersectingWithMiddleLine);
    }

    private Triangle extendLinesToCreateTriangle(Line2D line1, Line2D line2, Line2D line3) {
        int maxLineLength = tileMap.getMapWidth() * tileMap.getTileSize();
        Line2D newLine1 = new Line2D.Double(setLineLength(line1, maxLineLength).getP2(), setLineLength(new Line2D.Double(line1.getP2(), line1.getP1()), maxLineLength).getP2());
        Line2D newLine2 = new Line2D.Double(setLineLength(line2, maxLineLength).getP2(), setLineLength(new Line2D.Double(line2.getP2(), line2.getP1()), maxLineLength).getP2());
        Line2D newLine3 = new Line2D.Double(setLineLength(line3, maxLineLength).getP2(), setLineLength(new Line2D.Double(line3.getP2(), line3.getP1()), maxLineLength).getP2());
        if (getIntersectionPoint(newLine1, newLine2) != null && getIntersectionPoint(newLine2, newLine3) != null && getIntersectionPoint(newLine3, newLine1) != null) {
            return new Triangle(getIntersectionPoint(newLine1, newLine2), getIntersectionPoint(newLine2, newLine3), getIntersectionPoint(newLine3, newLine1));
        } else {
            return null;
        }
    }

    private Line2D findFirstIntersectionLine(final Line2D line1, ArrayList<Line2D> lines) {
        ArrayList<Line2D> intersectionLines = new ArrayList<Line2D>();
        for (Line2D line : lines) {
            if (line.intersectsLine(line1)) {
                intersectionLines.add(line);
            }
        }
        Collections.sort(intersectionLines, new Comparator<Line2D>() {
            @Override
            public int compare(Line2D o1, Line2D o2) {
                double distance1 = Line2D.ptSegDist(o1.getX1(), o1.getY1(), o1.getX2(), o1.getY2(), line1.getX1(), line1.getY1());
                double distance2 = Line2D.ptSegDist(o2.getX1(), o2.getY1(), o2.getX2(), o2.getY2(), line1.getX1(), line1.getY1());
                return (int) ((distance1) - (distance2));
            }
        });
        if (intersectionLines.size() > 0) {
            return intersectionLines.get(0);
        } else {
            return new NullLine();
        }
    }

    public Line2D setLineLength(Line2D line, double length) {
        double aX = line.getX1();
        double aY = line.getY1();
        double bX = line.getX2();
        double bY = line.getY2();

        Line2D lineWithNewLength = line;

        double lenAB = Math.sqrt(((aX - bX) * (aX - bX)) + (aY - bY) * (aY - bY));

        if (lenAB != length) {
            double ratio = length / lenAB;

            double cX = aX + (bX - aX) * ratio;
            double cY = aY + (bY - aY) * ratio;

            lineWithNewLength = new Line2D.Double(line.getX1(), line.getX2(), Math.round(cX), Math.round(cY));
        }
        return lineWithNewLength;
    }

    private Line2D createLineBetween(Line2D line1, Line2D line2) {
        double angle = angleBetween2Lines(line2, line1);
        return new Line2D.Float(
                (int) line1.getX1(),
                (int) line1.getY1(),
                (int) (line1.getX1() + 500 * Math.cos(angle)),
                (int) (line1.getY1() + 500 * Math.sin(angle))
        );
    }

    private Point2D.Float getIntersectionPoint(Line2D line1, Line2D line2) {
        if (!line1.intersectsLine(line2)) {
            return null;
        }
        double px = line1.getX1(),
                py = line1.getY1(),
                rx = line1.getX2() - px,
                ry = line1.getY2() - py;
        double qx = line2.getX1(),
                qy = line2.getY1(),
                sx = line2.getX2() - qx,
                sy = line2.getY2() - qy;

        double det = sx * ry - sy * rx;
        if (det == 0) {
            return null;
        } else {
            double z = (sx * (qy - py) + sy * (px - qx)) / det;
            if (z == 0 || z == 1) return null;  // intersection at end point!
            return new Point2D.Float(
                    (float) (px + z * rx), (float) (py + z * ry));
        }
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

    private void sortLines(ArrayList<Line2D> lines) {
        Collections.sort(lines, new Comparator<Line2D>() {
            @Override
            public int compare(Line2D o1, Line2D o2) {
                return (int) ((o1.getX2() - o1.getX1()) * (o2.getY2() - o1.getY1()) - (o2.getX2() - o1.getX1()) * (o1.getY2() - o1.getY1()));
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
        System.out.println("linie przed poloczeniem: " + mapOutlineTmp.size());
        mapOutlineTmp = connectLines(mapOutlineTmp);
        System.out.println("linie po poloczeniu: " + mapOutlineTmp.size());

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

    private ArrayList<Line2D> findParallelConnectedLine(Line2D targetLine, ArrayList<Line2D> lines) {
        ArrayList<Line2D> parallelLines = new ArrayList<Line2D>();
        for (Line2D line : lines) {
            if ((targetLine.getX2() == line.getX1() && targetLine.getY2() == line.getY1()) && (targetLine.getX1() == line.getX2() || targetLine.getY1() == line.getY2())) {
                parallelLines.add(line);
                parallelLines.addAll(findParallelConnectedLine(line, lines));
            }
        }
        return parallelLines;
    }

    private ArrayList<Line2D> connectLines(ArrayList<Line2D> lines) {
        ArrayList<Line2D> linesTmp = new ArrayList<Line2D>(lines);
        Collections.sort(linesTmp, new Comparator<Line2D>() {
            @Override
            public int compare(Line2D o1, Line2D o2) {
                return (int) ((o1.getX1() + o1.getY1()) - (o2.getX1() + o2.getY1()));
            }
        });
        for (int i = 0; i < lines.size(); i++) {
            Line2D firstLineToReplace = linesTmp.get(0);
            ArrayList<Line2D> linesToReplace = new ArrayList<Line2D>();
            linesTmp.remove(firstLineToReplace);

            if (findParallelConnectedLine(firstLineToReplace, linesTmp).size() > 0) {
                linesToReplace = findParallelConnectedLine(firstLineToReplace, linesTmp);
            }
            if (linesToReplace.size() > 0) {
                Line2D lineReplacement = new Line2D.Float(firstLineToReplace.getP1(), linesToReplace.get(linesToReplace.size() - 1).getP2());
                for (Line2D lineToReplace : linesToReplace) {
                    linesTmp.remove(lineToReplace);
                }
                linesTmp.add(lineReplacement);
            } else {
                linesTmp.add(firstLineToReplace);
            }

        }
        return linesTmp;
    }

    private int getMapStartY() {
        return 200 - (int) player.getY();

    }

    private int getMapStartX() {
        return 200 - (int) player.getX();
    }

}
