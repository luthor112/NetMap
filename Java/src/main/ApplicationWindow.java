package main;

import graphics.Graph;

import database.Database;
import database.Database.DatabaseEntry;
import database.Database.DatabaseEntryState;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.util.*;
import java.io.*;
import java.net.*;

public class ApplicationWindow extends JFrame {		// Main wondow of the application
	public static final long serialVersionUID = 1L;
	private WorkerThreadCollection wtc;				// This reference will point to the collection for threads
	private Database database;						// This reference will point to the database
	private Graph graph;							// This reference will point to the Graph object
	
	private JFileChooser fileChooser;				// Objects used later
	private Timer timer;
	private Options options;
	private WorkerThread workerThread;
	
	// Components used in the window, which need to be accessed from different methods
	private JLabel dbStat;							// Concerning databases
	private JProgressBar progressBar;
	private JButton dbSerialize;
	private JButton dbLoad;
	private JButton dbClear;
	private JButton dbDraw;
	private JTextField entryToAdd;
	private JButton dbAdd;
	
	private JLabel thStat;							// Concerning threads
	private JTextField thNum;
	private JButton thPause;
	private JButton thResume;
	private JButton thDelete;
	private JTextField thAmount;
	private JButton thAddAmount;
	private JButton thPauseAmount;
	private JButton thResumeAmount;
	private JButton thDeleteAmount;
	private JButton thPauseAll;
	private JButton thResumeAll;
	private JButton thDeleteAll;
	
	private JCheckBox optLinksOnly;					// Concerning options
	private JCheckBox optServersOnly;
	private JCheckBox optDontLeaveServer;
	private JTextField optBoundTo;
	
	private JTextField refreshInterval;				// Component used to change the refresh interval
	
	public ApplicationWindow() {							// Constructor
		super("NetMap");									// Call superclass's constructor, set window title
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);			// Set default close operation
		setSize(500, 500);									// Set default size
		
		JPanel panel = new JPanel(new GridBagLayout());		// Left JPanel
		
		panel.add(new JLabel("Database:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));	// Creating components concerning databases
		dbStat = new JLabel("0 entries, 0 non-processed");
		panel.add(dbStat, new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		progressBar = new JProgressBar(0, 0);
		progressBar.setString("0 / 0");
		progressBar.setStringPainted(true);
		panel.add(progressBar, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		dbSerialize = new JButton("Serialize");
		panel.add(dbSerialize, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		dbLoad = new JButton("Load");
		panel.add(dbLoad, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		dbClear = new JButton("Clear");
		panel.add(dbClear, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		dbDraw = new JButton("Draw");
		panel.add(dbDraw, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(new JLabel("Add "), new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		entryToAdd = new JTextField();
		panel.add(entryToAdd, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		dbAdd = new JButton("OK");
		panel.add(dbAdd, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(new JSeparator(), new GridBagConstraints(0, 5, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));	// Separator
		
		panel.add(new JLabel("Threads:"), new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));	// Creating components concerning threads
		thStat = new JLabel("0 threads, 0 working");
		panel.add(thStat, new GridBagConstraints(0, 7, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(new JLabel("Thread number "), new GridBagConstraints(0, 8, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		thNum = new JTextField("1");
		panel.add(thNum, new GridBagConstraints(2, 8, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		thPause = new JButton("Pause");
		panel.add(thPause, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		thResume = new JButton("Resume");
		panel.add(thResume, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		thDelete = new JButton("Delete");
		panel.add(thDelete, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		
		panel.add(new JLabel("Amount of threads "), new GridBagConstraints(0, 10, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));	// Creating components concerning groups of threads
		thAmount = new JTextField("1");
		panel.add(thAmount, new GridBagConstraints(2, 10, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		thPauseAmount = new JButton("Pause");
		panel.add(thPauseAmount, new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		thResumeAmount = new JButton("Resume");
		panel.add(thResumeAmount, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		thDeleteAmount = new JButton("Delete");
		panel.add(thDeleteAmount, new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		thAddAmount = new JButton("Add");
		panel.add(thAddAmount, new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		thPauseAll = new JButton("Pause all");
		panel.add(thPauseAll, new GridBagConstraints(0, 12, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		thResumeAll = new JButton("Resume all");
		panel.add(thResumeAll, new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		thDeleteAll = new JButton("Delete all");
		panel.add(thDeleteAll, new GridBagConstraints(2, 12, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(new JSeparator(), new GridBagConstraints(0, 13, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));				// Separator
		
		panel.add(new JLabel("Options used:"), new GridBagConstraints(0, 14, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));		// Creating components concerning the options used to create threads and draw new graphs
		optLinksOnly = new JCheckBox("Links only");
		panel.add(optLinksOnly, new GridBagConstraints(0, 15, 4, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		optServersOnly = new JCheckBox("Servers only");
		panel.add(optServersOnly, new GridBagConstraints(0, 16, 4, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		optDontLeaveServer = new JCheckBox("Don't leave server");
		panel.add(optDontLeaveServer, new GridBagConstraints(0, 17, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(new JLabel("Server "), new GridBagConstraints(0, 18, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		optBoundTo = new JTextField();
		panel.add(optBoundTo, new GridBagConstraints(1, 18, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(new JSeparator(), new GridBagConstraints(0, 19, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));				// Separator
		
		panel.add(new JLabel("Refresh interval "), new GridBagConstraints(0, 20, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));	// Creating components concerning the setting of the refresh interval
		refreshInterval = new JTextField("");
		panel.add(refreshInterval, new GridBagConstraints(2, 20, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		
		add(new JScrollPane(panel), BorderLayout.WEST);		// Add the JPanel to the left in a JScrollPane
		
		graph = new Graph();								// Graph painter (center JPanel)
		add(new JScrollPane(graph), BorderLayout.CENTER);	// Add the graph painter to the center in a JScrollPane
		
		wtc = new WorkerThreadCollection();					// Initialize the collection for threads
		database = new Database();							// Initialize the database
		fileChooser = new JFileChooser();					// Initialize the file chooser dialog
		
		dbSerialize.addActionListener(new ActionListener() {							// Clicking on the button "Serialize"
			public void actionPerformed(ActionEvent ae) {
				if (fileChooser.showSaveDialog(graph) == JFileChooser.APPROVE_OPTION) {	// If file is chosen
					try {
						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileChooser.getSelectedFile()));	// Create ObjectOutputStream
						
						synchronized (database) {										// Synchronize on the database
							for (DatabaseEntry de : database.getDatabase())
								de.coord = null;										// Set coordinates to null to compact database data 
							
							oos.writeObject(database);									// Serialize database
						}
						
						JOptionPane.showMessageDialog(graph, "Serialization completed successfully!");	// Show message
						oos.close();													// Close stream, close file
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(graph, "Serialization failed.", "Error", JOptionPane.ERROR_MESSAGE);		// Tell the user if an error occures
					}
				}
			}
		});
		
		dbLoad.addActionListener(new ActionListener() {									// Clicking on the button "Load"
			public void actionPerformed(ActionEvent ae) {
				if (fileChooser.showOpenDialog(graph) == JFileChooser.APPROVE_OPTION) {	// If file is chosen
					try {
						ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileChooser.getSelectedFile()));	// Create ObjectInputStream
						
						synchronized (database) {										// Synchronize on the database
							database = (Database)ois.readObject();						// Read database
						}
						
						JOptionPane.showMessageDialog(graph, "Loading completed successfully.");							// Tell the user the database's been loaded
						ois.close();
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(graph, "Loading database failed.", "Error", JOptionPane.ERROR_MESSAGE);	// Tell the user if a file reading error occured
					} catch (ClassNotFoundException ex) {
						JOptionPane.showMessageDialog(graph, "Database class not found.", "Error", JOptionPane.ERROR_MESSAGE);	// Tell the user if no database class could be found
					}
				}
			}
		});
		
		dbClear.addActionListener(new ActionListener() {	// Clicking on the button "Clear"
			public void actionPerformed(ActionEvent ae) {
				database.clear();							// Clear database -- synchronization occurs inside the database
			}
		});
		
		dbDraw.addActionListener(new ActionListener() {						// Clicking on the button "Draw"
			public void actionPerformed(ActionEvent ae) {					
				synchronized (database) {									// Synchronize on the database
					graph.newGraph(database, optServersOnly.isSelected());	// Request a new graph
				}
				graph.repaint();											// Repaint the component displaying the graph
			}
		});
		
		dbAdd.addActionListener(new ActionListener() {						// Clicking on the button "OK"
			public void actionPerformed(ActionEvent ae) {
				synchronized (database) {									// Synchronize on the database
					try {
						database.put(new DatabaseEntry(new URL(entryToAdd.getText()), DatabaseEntryState.NOT_PROCESSED), optServersOnly.isSelected());	// Put a new non-processed entry in the database using the URL specified by the user
					} catch (MalformedURLException ex) {
						JOptionPane.showMessageDialog(graph, "Bad URL given.", "Error", JOptionPane.ERROR_MESSAGE);										// Tell the user if there is a problem with the URL
					}
				}
			}
		});
		
		thPause.addActionListener(new ActionListener() {				// Clicking on the first button labeled "Pause"
			public void actionPerformed(ActionEvent ae) {
				try {
					int num = Integer.parseInt(thNum.getText()) - 1;	// Get thread number
					if ((num >= 0) && (num < wtc.size()))				// Check if it's OK
						wtc.get(num).pauseWork();						// Pause given thread
					else
						JOptionPane.showMessageDialog(graph, "Invalid thread number given.", "Error", JOptionPane.ERROR_MESSAGE);	// Tell the user if an invalid thread number is given
				} catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(graph, "Invalid thread number given.", "Error", JOptionPane.ERROR_MESSAGE);		// Tell the user if there is a problem with the given number (probably it's not a number)
				}
			}
		});
		
		thResume.addActionListener(new ActionListener() {				// Clicking on the first button labeled "Resume"
			public void actionPerformed(ActionEvent ae) {
				try {
					int num = Integer.parseInt(thNum.getText()) - 1;	// Get thread number
					if ((num >= 0) && (num < wtc.size()))				// Check if it's OK
						wtc.get(num).resumeWork();						// Resume given thread
					else
						JOptionPane.showMessageDialog(graph, "Invalid thread number given.", "Error", JOptionPane.ERROR_MESSAGE);	// Tell the user if an invalid thread number is given
				} catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(graph, "Invalid thread number given.", "Error", JOptionPane.ERROR_MESSAGE);		// Tell the user if there is a problem with the given number (probably it's not a number)
				}
			}
		});
		
		thDelete.addActionListener(new ActionListener() {				// Clicking on the first button labeled "Delete"
			public void actionPerformed(ActionEvent ae) {
				try {
					int num = Integer.parseInt(thNum.getText()) - 1;	// Get thread number
					if ((num >= 0) && (num < wtc.size())) {				// Check if it's OK
						wtc.get(num).endWork();							// Tell the thread to end its work
						
						while (wtc.get(num).isAlive()) {				// Wait while it's still alive
							try {
								Thread.sleep(100);
							} catch (InterruptedException ex) { }
						}
						
						wtc.remove(num);								// Remove it from the collection
					} else {
						JOptionPane.showMessageDialog(graph, "Invalid thread number given.", "Error", JOptionPane.ERROR_MESSAGE);	// Tell the user if an invalid thread number is given
					}
				} catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(graph, "Invalid thread number given.", "Error", JOptionPane.ERROR_MESSAGE);		// Tell the user if there is a problem with the given number (probably it's not a number)
				}
			}
		});
		
		thAddAmount.addActionListener(new ActionListener() {				// Clicking on the button "Add"
			public void actionPerformed(ActionEvent ae) {
				try {
					int amount = Integer.parseInt(thAmount.getText());		// Get amount
					
					if (optDontLeaveServer.isSelected())																			// If "Don't leave server" is selected,
						options = new Options(optLinksOnly.isSelected(), optServersOnly.isSelected(), true, optBoundTo.getText());	// create options telling the thread not to leave the given server
					else																											// If it's not,
						options = new Options(optLinksOnly.isSelected(), optServersOnly.isSelected(), false, null);					// create option telling the thread it can go anywhere
					
					for (int i=0; i<amount; i++) {							// Create the given amount of threads
						workerThread = new WorkerThread(database, options);	// using the created options,
						workerThread.start();								// start them
						wtc.add(workerThread);								// and add them to the collection
					}
				} catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(graph, "Invalid amount given.", "Error", JOptionPane.ERROR_MESSAGE);				// Tell the user if there is a problem with the given number (probably it's not a number)
				}
			}
		});
		
		thPauseAmount.addActionListener(new ActionListener() {				// Clicking on the second button labeled "Pause"
			public void actionPerformed(ActionEvent ae) {
				try {
					int amount = Integer.parseInt(thAmount.getText());		// Get amount
					
					for (WorkerThread wt : wtc) {
						if (amount > 0 && wt.isWorking()) {					// Pause the given amount of threads, or all if there's not enough
							wt.pauseWork();
							amount--;
						}
					}
				} catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(graph, "Invalid amount given.", "Error", JOptionPane.ERROR_MESSAGE);				// Tell the user if there is a problem with the given number (probably it's not a number)
				}
			}
		});
		
		thResumeAmount.addActionListener(new ActionListener() {				// Clicking on the second button labeled "Resume"
			public void actionPerformed(ActionEvent ae) {
				try {
					int amount = Integer.parseInt(thAmount.getText());		// Get amount
					
					for (WorkerThread wt : wtc) {
						if (amount > 0 && wt.isWorking()) {					// Resume the given amount of threads, or all if there's not enough
							wt.resumeWork();
							amount--;
						}
					}
				} catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(graph, "Invalid amount given.", "Error", JOptionPane.ERROR_MESSAGE);				// Tell the user if there is a problem with the given number (probably it's not a number)
				}
			}
		});
		
		thDeleteAmount.addActionListener(new ActionListener() {				// Clicking on the second button labeled "Delete"
			public void actionPerformed(ActionEvent ae) {
				try {
					int amount = Integer.parseInt(thAmount.getText());		// Get amount
					
					for (int i=0; i<amount; i++) {
						wtc.get(0).endWork();								// Tell the specified amount of threads to stop working
						while (wtc.get(0).isAlive()) {						// Wait while they're still alive
							try {
								Thread.sleep(100);
							} catch (InterruptedException ex) { }
						}
						wtc.remove(0);										// Remove them from the list
					}
				} catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(graph, "Invalid amount given.", "Error", JOptionPane.ERROR_MESSAGE);				// Tell the user if there is a problem with the given number (probably it's not a number)
				}
			}
		});
		
		thPauseAll.addActionListener(new ActionListener() {		// Clicking on the button "Pause all"
			public void actionPerformed(ActionEvent ae) {
				wtc.pauseAllWork();								// Tell the collection to pause all threads
			}
		});
		
		thResumeAll.addActionListener(new ActionListener() {	// Clicking on the button "Resume all"
			public void actionPerformed(ActionEvent ae) {
				wtc.resumeAllWork();							// Tell the collection to resume all threads
			}
		});
		
		thDeleteAll.addActionListener(new ActionListener() {	// Clicking on the button "Delete all"
			public void actionPerformed(ActionEvent ae) {
				wtc.endAllWork();								// Tell the collection to stop all threads
				while (wtc.countAlive() != 0) {					// Wait while they're still alive
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) { }
				}
				wtc.clear();									// Clear the collection
			}
		});
		
		timer = new Timer();									// Initialize timer
		
		refreshInterval.getDocument().addDocumentListener(new DocumentListener() {									// If refresh interval changes
			public void changedUpdate(DocumentEvent e) { }
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}
			public void insertUpdate(DocumentEvent e) {
				try {
					int interval = Integer.parseInt(refreshInterval.getText()) * 1000;								// Get number
					timer.cancel();																					// Cancel tasks of the current timer
					timer = new Timer();																			// initialize a new timer
					timer.schedule(new TimerTask() {																// Schedule a new task...
						public void run() {																			// ... which does...
							int entryCount = database.entryCount();													// Get the number of entries in the database
							int nonProcessedCount = database.entryStateCount(DatabaseEntryState.NOT_PROCESSED);		// Get the number of non-processed entries
							dbStat.setText(entryCount + " entries, " + nonProcessedCount + " non-processed");		// Update database info
							progressBar.setMaximum(entryCount);														// Update progress bar
							progressBar.setValue(entryCount - nonProcessedCount);
							progressBar.setString((entryCount - nonProcessedCount) + " / " + entryCount);			// Update progress bar's caption
							thStat.setText(wtc.size() + " threads, " + wtc.countWorking() + " working");			// Update thread info
						}
					}, interval, interval);																			// Start after the given interval, repeat in the given interval forever
				} catch (NumberFormatException ex) { }																// If there's a problem with the interval, do nothing. The user's probably just editing it.
			}
		});
		
		addWindowListener(new WindowListener() {				// Add window listener to the window...
			public void windowActivated(WindowEvent e) { }
			public void windowClosed(WindowEvent e) { }
			public void windowClosing(WindowEvent e) {			// ... to listen for window closing events. Before closing the window...
				timer.cancel();									// Cancel tasks of the current timer
				thDeleteAll.doClick();							// Stop all threads and delete them from the collection
			}
			public void windowDeactivated(WindowEvent e) { }
			public void windowDeiconified(WindowEvent e) { }
			public void windowIconified(WindowEvent e) { }
			public void windowOpened(WindowEvent e) { }
		});
		
		refreshInterval.setText("10");							// Update refresh interval. Default amount is 10 seconds
		
		setVisible(true);										// Set the window visible
	}
	
	public static void main(String[] args) {					// Main method, which
		new ApplicationWindow();								// creates an instance of the window.
	}
}
