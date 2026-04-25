import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.ImageWriteParam;
import java.io.File;
import java.util.Iterator;

public class CreateAnimatedTowerGif {
    
    public static void main(String[] args) throws Exception {
        System.out.println("Creating animated tower GIF...");
        
        // =============================================
        // CHANGE THESE VALUES FOR YOUR DESIRED SIZE:
        int gifWidth = 70;    // Width of GIF frames
        int gifHeight = 70;   // Height of GIF frames
        // =============================================
        
        int frameCount = 8;   // Number of animation frames
        int frameDelay = 100; // Delay between frames (ms)
        
        createAnimatedTowerGif("basic_tower.gif", gifWidth, gifHeight, frameCount, frameDelay);
        System.out.println("Created: basic_tower.gif (" + gifWidth + "x" + gifHeight + ")");
    }
    
    public static void createAnimatedTowerGif(String filename, int width, int height, 
                                              int frameCount, int delay) throws Exception {
        // Get an ImageWriter for GIF format
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        ImageWriter writer = writers.next();
        
        // Create output stream
        File file = new File(filename);
        ImageOutputStream output = ImageIO.createImageOutputStream(file);
        writer.setOutput(output);
        
        // Get default write parameters
        ImageWriteParam params = writer.getDefaultWriteParam();
        
        // Configure for animated GIF
        writer.prepareWriteSequence(null);
        
        // Create each frame
        for (int i = 0; i < frameCount; i++) {
            BufferedImage frame = createTowerFrame(i, frameCount, width, height);
            
            // Get metadata for this frame
            IIOMetadata metadata = writer.getDefaultImageMetadata(
                new javax.imageio.ImageTypeSpecifier(frame),
                params
            );
            
            // Configure metadata for GIF
            String metaFormatName = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);
            
            // Set delay for this frame
            IIOMetadataNode gce = getNode(root, "GraphicControlExtension");
            gce.setAttribute("delayTime", Integer.toString(delay / 10)); // Convert to 1/100ths of second
            gce.setAttribute("disposalMethod", "none");
            gce.setAttribute("userInputFlag", "FALSE");
            gce.setAttribute("transparentColorFlag", "FALSE");
            
            // Update metadata
            metadata.setFromTree(metaFormatName, root);
            
            // Create IIOImage with metadata
            IIOImage iioImage = new IIOImage(frame, null, metadata);
            
            // Write the frame with null parameters (use default)
            writer.writeToSequence(iioImage, null);
        }
        
        // Finish and clean up
        writer.endWriteSequence();
        output.close();
        writer.dispose();
    }
    
    // Helper method to get or create a node
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        IIOMetadataNode node = null;
        for (int i = 0; i < rootNode.getLength(); i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                node = (IIOMetadataNode) rootNode.item(i);
            }
        }
        
        if (node == null) {
            node = new IIOMetadataNode(nodeName);
            rootNode.appendChild(node);
        }
        
        return node;
    }
    
    private static BufferedImage createTowerFrame(int frameNum, int totalFrames, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        
        // Transparent background
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, width, height);
        
        // Draw blue tower base (stationary)
        int baseMargin = width / 6; // 1/6 of width for margin
        g.setColor(new Color(100, 149, 237));
        g.fillRect(baseMargin, baseMargin, width - 2*baseMargin, height - 2*baseMargin);
        
        // Draw darker blue turret
        int turretMargin = width / 5; // 1/5 of width for turret margin
        g.setColor(new Color(65, 105, 225));
        g.fillOval(turretMargin, turretMargin, width - 2*turretMargin, height - 2*turretMargin);
        
        // Calculate rotation angle for the gun
        double angle = (frameNum * 2 * Math.PI) / totalFrames;
        
        // Draw rotating gun
        g.setColor(Color.DARK_GRAY);
        int centerX = width / 2;
        int centerY = height / 2;
        int gunLength = Math.min(width, height) / 3;
        
        // Set line thickness based on size
        float lineThickness = Math.max(1.0f, width / 15.0f);
        g.setStroke(new java.awt.BasicStroke(lineThickness));
        
        int endX = centerX + (int)(Math.cos(angle) * gunLength);
        int endY = centerY + (int)(Math.sin(angle) * gunLength);
        
        g.drawLine(centerX, centerY, endX, endY);
        
        // Draw gun muzzle
        int muzzleSize = Math.max(2, width / 10);
        g.setColor(Color.BLACK);
        g.fillOval(endX - muzzleSize/2, endY - muzzleSize/2, muzzleSize, muzzleSize);
        
        g.dispose();
        return img;
    }
}


