package graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.util.List;
import java.net.URL;

import javax.swing.JPanel;

import database.Database;
import database.Database.DatabaseEntry;
import database.Database.DatabaseEntryState;

public class Graph extends JPanel {					// Component which can paint graphs
	public static final long serialVersionUID = 1L;
	protected BufferedImage currentGraph;			// The last graph painted by the component
	protected volatile boolean working;				// Is the component painting a new graph at the moment
	
	public static int pow2(int num) {				// Calculate an integer power of 2
		int acc = 1;
		for (int i= 0; i<num; i++)
			acc *= 2;
		
		return acc;
	}
	
	public Graph() {															// Constructor
		super(true);															// Call superclass's constructor, request double buffering
		currentGraph = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);	// Set last painted graph to a clear 1x1 pixel image
		working = false;														// Record that the component is not painting a new graph at the moment
	}
	
	public void newGraph(Database database, boolean serversOnly) {				// Paint a new graph
		working = true;															// Record that the component is painting a new graph
		List<DatabaseEntry> list = database.getDatabase();						// Get the database's ArrayList
		
		DatabaseEntry otherEnd = null;											// used when drawing lines
		
		int circle = 0;															// In which circle are we
		int verticle = 0;														// Which verticle is it in the circle
		int x, y;																// Coordinates used below
		for (DatabaseEntry de : list) {											// Go through the list
			x = (int)((circle*30) * Math.sin(((2*Math.PI)/pow2(circle+1))*verticle));		// Slewing verticle to its position on the circle
			y = (int)((circle*30) * Math.cos(((2*Math.PI)/pow2(circle+1))*verticle));
			
			de.coord = new Point(x, y);											// Setting the coordinates
			
			if ((verticle + 1 == pow2(circle+1)) || (circle == 0)) {			// If this was the last verticle in the circle,
				verticle = 0;													// proceed with the next circle.
				circle++;
			} else {															// If it was not,
				verticle++;														// proceed with the next verticle.
			}
		}
		
		currentGraph = new BufferedImage(circle*60 + 11, circle*60 + 11, BufferedImage.TYPE_INT_ARGB);	// Create new image which is big enough for the graph
		Graphics2D graph = currentGraph.createGraphics();				// Get graphics
		graph.translate(circle*30 + 5, circle*30 + 5);					// Set the coordinate system's origo
		
		graph.setColor(Color.BLACK);															// Set color to black
		for (DatabaseEntry de : list) {															// Go through the list
			if (de.links != null)																// If the entry has connections
				for (URL url : de.links) {														// Go through the connections
					otherEnd = database.get(url, serversOnly);									// Get the other end of the connection
					graph.drawLine(de.coord.x, de.coord.y, otherEnd.coord.x, otherEnd.coord.y);	// Draw a line between the ends of the connection
				}
		}
		
		for (DatabaseEntry de : list) {									// Go through the list
			if (!de.state.equals(DatabaseEntryState.PROCESSED))			// Set color based on speed and processedness
				graph.setColor(Color.GRAY);
			else if (de.speed == 0)
				graph.setColor(Color.BLUE);
			else if (de.speed < 2500)
				graph.setColor(Color.RED);
			else if (de.speed < 5000)
				graph.setColor(Color.ORANGE);
			else if (de.speed < 7500)
				graph.setColor(Color.YELLOW);
			else if (de.speed < 10000)
				graph.setColor(Color.GREEN);
			else
				graph.setColor(Color.BLUE);
			
			graph.fillOval(de.coord.x - 5, de.coord.y - 5, 10, 10);		// Draw verticle
		}
		
		working = false;																		// Record that the component has finished drawing the new graph
	}
	
	public boolean isWorking() {						// Ask whether the component is drawing a new graph at the moment
		return working;
	}
	
	public void paint(Graphics g) {						// Request paint. This method overrides javax.swing.JComponent.paint
		Graphics2D g2d = (Graphics2D)g;					// Cast Graphics to Graphics2D, so we will be able to draw the image of the current graph on it
		super.paint(g2d);								// Clear the image
		if (!working)									// If the component's not painting a new graph at the moment,
			g2d.drawImage(currentGraph, 0, 0, null);	// draw the graph last painted on the Graphics object
	}
	
	public Dimension getPreferredSize() {											// Get (preferred) size of the component. This method overrides javax.swing.JComponent.getPreferredSize and is called by the enclosing JScrollPane
		if (!working)																	// If the component is not painting a new graph at the moment,
			return new Dimension(currentGraph.getWidth(), currentGraph.getHeight());	// return the size of the graph last drawn.
		else																			// If painting is in progress,
			return new Dimension(1, 1);													// signalise (almost) no need for space.
	}
}
