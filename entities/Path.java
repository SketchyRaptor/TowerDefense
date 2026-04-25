package entities;

import utils.Vector2D;
import java.io.Serializable;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.List;
import java.util.ArrayList;

public class Path implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<Vector2D> pathPoints;
    private Color pathColor;
    private Color nodeColor;
    
    public Path(List<Vector2D> pathPoints) {
        this.pathPoints = new ArrayList<>(pathPoints);
        this.pathColor = new Color(139, 69, 19); // Brown
        this.nodeColor = new Color(160, 82, 45); // Lighter brown
    }
    
    public Path() {
        this.pathPoints = new ArrayList<>();
        this.pathColor = new Color(139, 69, 19);
        this.nodeColor = new Color(160, 82, 45);
        createDefaultPath();
    }
    
    private void createDefaultPath() {
        // Create a winding path that covers the screen
        pathPoints.clear();
        
        // Start off-screen left
        pathPoints.add(new Vector2D(-50, 300));
        
        // First segment: horizontal
        pathPoints.add(new Vector2D(100, 300));
        
        // Second segment: down
        pathPoints.add(new Vector2D(100, 500));
        
        // Third segment: right
        pathPoints.add(new Vector2D(300, 500));
        
        // Fourth segment: up
        pathPoints.add(new Vector2D(300, 200));
        
        // Fifth segment: right
        pathPoints.add(new Vector2D(500, 200));
        
        // Sixth segment: down
        pathPoints.add(new Vector2D(500, 400));
        
        // Seventh segment: right
        pathPoints.add(new Vector2D(700, 400));
        
        // Eighth segment: up
        pathPoints.add(new Vector2D(700, 100));
        
        // Ninth segment: right
        pathPoints.add(new Vector2D(900, 100));
        
        // Tenth segment: down
        pathPoints.add(new Vector2D(900, 300));
        
        // End off-screen right
        pathPoints.add(new Vector2D(1250, 300));
    }
    
    public List<Vector2D> getPathPoints() {
        return new ArrayList<>(pathPoints);
    }
    
    public Vector2D getStartPoint() {
        if (pathPoints.isEmpty()) {
            return new Vector2D(0, 0);
        }
        return pathPoints.get(0);
    }
    
    public Vector2D getEndPoint() {
        if (pathPoints.isEmpty()) {
            return new Vector2D(0, 0);
        }
        return pathPoints.get(pathPoints.size() - 1);
    }
    
    public void render(Graphics2D g2d) {
        if (pathPoints.size() < 2) return;
        
        // Save original stroke
        java.awt.Stroke originalStroke = g2d.getStroke();
        
        // Draw path lines
        g2d.setColor(pathColor);
        g2d.setStroke(new BasicStroke(40, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            Vector2D current = pathPoints.get(i);
            Vector2D next = pathPoints.get(i + 1);
            
            g2d.drawLine(
                (int)current.getX(), (int)current.getY(),
                (int)next.getX(), (int)next.getY()
            );
        }
        
        // Draw path nodes (circles at each point)
        g2d.setColor(nodeColor);
        for (Vector2D point : pathPoints) {
            int nodeSize = 20;
            g2d.fillOval(
                (int)point.getX() - nodeSize/2,
                (int)point.getY() - nodeSize/2,
                nodeSize, nodeSize
            );
            
            // Draw outline
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(
                (int)point.getX() - nodeSize/2,
                (int)point.getY() - nodeSize/2,
                nodeSize, nodeSize
            );
            g2d.setColor(nodeColor);
        }
        
        // Draw start and end indicators
        if (!pathPoints.isEmpty()) {
            // Start point (green)
            Vector2D start = pathPoints.get(0);
            g2d.setColor(Color.GREEN);
            g2d.fillRect((int)start.getX() - 15, (int)start.getY() - 15, 30, 30);
            g2d.setColor(Color.BLACK);
            g2d.drawString("START", (int)start.getX() - 20, (int)start.getY() - 20);
            
            // End point (red)
            Vector2D end = pathPoints.get(pathPoints.size() - 1);
            g2d.setColor(Color.RED);
            g2d.fillRect((int)end.getX() - 15, (int)end.getY() - 15, 30, 30);
            g2d.setColor(Color.BLACK);
            g2d.drawString("END", (int)end.getX() - 15, (int)end.getY() - 20);
        }
        
        // Restore original stroke
        g2d.setStroke(originalStroke);
    }
    
    public boolean isPointOnPath(Vector2D point, double tolerance) {
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            Vector2D start = pathPoints.get(i);
            Vector2D end = pathPoints.get(i + 1);
            
            if (distanceToSegment(point, start, end) <= tolerance) {
                return true;
            }
        }
        return false;
    }
    
    private double distanceToSegment(Vector2D point, Vector2D start, Vector2D end) {
        double lineLength = start.distanceTo(end);
        if (lineLength == 0) return point.distanceTo(start);
        
        double t = Math.max(0, Math.min(1, 
            ((point.getX() - start.getX()) * (end.getX() - start.getX()) +
             (point.getY() - start.getY()) * (end.getY() - start.getY())) /
            (lineLength * lineLength)));
        
        Vector2D projection = new Vector2D(
            start.getX() + t * (end.getX() - start.getX()),
            start.getY() + t * (end.getY() - start.getY())
        );
        
        return point.distanceTo(projection);
    }
}