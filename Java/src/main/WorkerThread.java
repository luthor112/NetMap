package main;

import database.Database;
import database.Database.DatabaseEntry;
import database.Database.DatabaseEntryState;

import java.io.*;
import java.net.*;
import java.util.*;

public class WorkerThread extends Thread {						// Thread that discovers the connections
	protected volatile boolean shutDownCommanded;				// Should the thread stop
	protected volatile boolean runPermission;					// Should the thread work
	protected volatile boolean working;							// Is the thread working
	
	public static final String quot = String.valueOf((char)34); // String consisting of a single quotation mark
	
	protected Database database;								// Reference to the database
	protected Options options;									// Reference to the options
	
	public WorkerThread(Database database, Options options) {	// Constructor
		this.database = database;
		this.options = options;
		
		shutDownCommanded = false;								// Do not stop,
		runPermission = false;									// but do not work
		working = false;										// Not working
	}
	
	public void endWork() {										// Stop thread
		runPermission = false;
		shutDownCommanded = true;
	}
	
	public void resumeWork() {									// Resume work
		runPermission = true;
	}
	
	public void pauseWork() {									// Pause work
		runPermission = false;
	}
	
	public boolean isWorking() {								// Is the thread working?
		return working;
	}
	
	private void sendURLToDatabase(URL newURL) {	// Put a new entry in the database
		// If we can leave the server or we can't, but the new URL is on the server we can't leave; and the file the URL points to is another webpage...
		if ((!options.dontLeaveServer || (options.dontLeaveServer && newURL.getAuthority().equals(options.boundTo))) && newURL.getPath().matches(".*(htm|html|xhtm|xhtml|shtm|shtml|php|asp|aspx|cgi|jsp)"))
			database.put(new DatabaseEntry(newURL, DatabaseEntryState.NOT_PROCESSED), options.serversOnly);	// Put it in the database and set the entry's state to NOT_PROCESSED
		else																								// Else...
			database.put(new DatabaseEntry(newURL, DatabaseEntryState.PROCESSED), options.serversOnly);		// Put it in the database and set the entry's state to PROCESSED, so we won't deal with it anymore
	}
	
	public void run() {								// What does the thread do
		DatabaseEntry workOn = null;				// Declare and initialize used variables
		BufferedReader incoming = null;
		String line = null;
		String backupLine = null;
		String addr = null;
		URL newURL = null;
		long size = 0;
		long time = 0;
		long currTime = 0;
		
		while (!shutDownCommanded) {				// If the thread doesn't have to stop
			while (runPermission) {					// and it has permission to work
				working = true;						// Record that it's working
				if ((workOn = database.getNonProcessedEntry()) != null) {								// Get an entry that hasn't been processed yet
					try {
						incoming = new BufferedReader(new InputStreamReader(workOn.url.openStream()));	// Open a stream to its URL
						workOn.links = new HashSet<URL>();												// New HashSet for URLs
						size = 0;																		// Initialize variables to measure speed
						time = 0;
						currTime = System.currentTimeMillis();											// Current time
						
						while ((line = incoming.readLine()) != null) {
							time += System.currentTimeMillis() - currTime;								// Add elapsed time
							size += line.length();														// Add size
							backupLine = new String(line);												// Create a backup of the line
							
							while (!line.isEmpty()) {													// While the line's not empty
								if (line.matches(".*<a .*href=[" + quot + "'].*[" + quot + "'].*")) {	// If it finds a links
									addr = line.substring(line.indexOf("href=") + 6, line.indexOf(line.charAt(line.indexOf("href=") + 5), line.indexOf("href=") + 6)); // Get the URL
									if (addr.matches(".*#.*"))
										addr = addr.substring(0, addr.indexOf("#"));																				   // Trim part after #
									line = line.substring(line.indexOf(line.charAt(line.indexOf("href=") + 5), line.indexOf("href=") + 6) + 1);						   // Trim line
									newURL = new URL(workOn.url, addr);																								   // Add the new URL to the database
									sendURLToDatabase(newURL);
									workOn.links.add(newURL);																										   // and to the links of the current entry
								}
								else {
									line = "";		// If no matches are found, end search
								}
							}
							
							if (!options.linksOnly)	// If we are not searching just for links
								while (!backupLine.isEmpty()) {																															// While the line's not empty
									newURL = null;
									
									if (backupLine.matches(".*" + quot + ".*://.*" + quot + ".*")) {																					// If we find an address
										addr = backupLine.substring(backupLine.lastIndexOf(quot, backupLine.indexOf("://")) + 1, backupLine.indexOf(quot, backupLine.indexOf("://")));	// Get the URL
										if (addr.matches(".*#.*"))																														// Trim part after #
											addr = addr.substring(0, addr.indexOf("#"));
										backupLine = backupLine.substring(backupLine.indexOf(quot, backupLine.indexOf("://")) + 1);														// Trim line
										newURL = new URL(workOn.url, addr);
									}
									else if (backupLine.matches(".*'.*://.*'.*")) {																										// If we find an address
										addr = backupLine.substring(backupLine.lastIndexOf("'", backupLine.indexOf("://")) + 1, backupLine.indexOf("'", backupLine.indexOf("://")));	// Get the URL
										if (addr.matches(".*#.*"))																														// Trim part after #
											addr = addr.substring(0, addr.indexOf("#"));
										backupLine = backupLine.substring(backupLine.indexOf("'", backupLine.indexOf("://")) + 1);														// Trim line
										newURL = new URL(workOn.url, addr);
									}
									else {
										backupLine = "";			// If nothing's found, stop searching
									}
									
									if (newURL != null) {			// If a URL was found
										sendURLToDatabase(newURL);	// Add it to the database
										workOn.links.add(newURL);	// and to the current entry's connection list
									}
								}
							
							currTime = System.currentTimeMillis();	// Query current time for speed measurment
						}
						
						if (time != 0)								// If time's not 0. calculate speed
							workOn.speed = size/time;
						workOn.state = DatabaseEntryState.PROCESSED;	// Set state to processed
					} catch (IOException ex) {							// If there was an error reading the page,
						workOn.state = DatabaseEntryState.UNREACHABLE;	// set its state to UNREACHABLE
					} finally {
						if (incoming != null)							// If the page's BufferedReader was initialized
							try {
								incoming.close();						// Close the connection
							} catch (IOException ex) { }
					}
				}
				else													// If there was no non-processed entry
				{
					try {
						sleep(3000);									// Wait 3 seconds
					} catch (InterruptedException ex) { }
				}
			}
			
			working = false;											// If no permission to work, record that it's not working
		}
	}
}
