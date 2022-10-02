package database;

import graphics.Point;
import java.util.*;
import java.io.Serializable;
import java.net.*;

public class Database implements Serializable {
	public static final long serialVersionUID = 1L;
	protected List<DatabaseEntry> database;									// Store entries in an ArrayList
	
	public Database() {
		database = new ArrayList<DatabaseEntry>();
	}
	
	synchronized public DatabaseEntry get(URL url, boolean serversOnly) {	// Get entry with the specified URL
		if (serversOnly) {													// If we are in Servers only mode...
			for (DatabaseEntry de : database)
				if (de.url.getAuthority().equals(url.getAuthority()))		// Return entry if the authority parts of their URLs are equal
					return de;
		}
		else {																// If we are not...
			for (DatabaseEntry de : database)
				if (de.url.equals(url))										// Return entry if the whole URLs match
					return de;
		}
		
		return null;														// If no such entry is found, return null
	}
	
	synchronized public DatabaseEntry getNonProcessedEntry() {				// Return a non-processed entry
		for (DatabaseEntry de : database)
			if (de.state.equals(DatabaseEntryState.NOT_PROCESSED))			// If a non-processed entry is found...
			{
				de.state = DatabaseEntryState.PROCESSING;					// Set its state to PROCESSING
				return de;													// and return it.
			}
		
		return null;														// If no such entry is found, return null
	}
	
	synchronized public List<DatabaseEntry> getDatabase() {					// Returns the List which stores the entries
		return Collections.unmodifiableList(database);
	}
	
	synchronized public boolean put(DatabaseEntry entry, boolean serversOnly) {	// Put an entry to the database
		if (serversOnly)														// If we are in Servers only mode...
		{
			for (DatabaseEntry de : database)
				if (de.url.getAuthority().equals(entry.url.getAuthority()))		// Search for an entry with identical URL authority part
					return false;												// If found, return false, so no duplicate entries will be added to the database
		}
		else																	// If we are not in Servers only mode...
		{
			for (DatabaseEntry de : database)
				if (de.url.equals(entry.url))									// Search for an entry with identical URL
					return false;												// If found, return false, so no duplicate entries will be added to the database
		}
		
		return database.add(entry);												// If no matching entry is found, add the new entry to the database
	}
	
	synchronized public int entryStateCount(DatabaseEntryState state) {			// Count state with specified state
		int count = 0;
		
		for (DatabaseEntry de : database)
			if (de.state.equals(state))
				count++;
		
		return count;
	}
	
	synchronized public int entryCount() {										// Return the number of entries in the database
		return database.size();
	}
	
	synchronized public void clear() {											// Clear the database
		database.clear();
	}
	
	public static class DatabaseEntry implements Serializable {					// Specification of an entry in the database
		public static final long serialVersionUID = 1L;
		public URL url;															// Webpage's URL
		public volatile DatabaseEntryState state;								// Entry's state, volatile because of multi-threading
		public long speed;														// Download speed in Bpms
		public Set<URL> links;													// HashSet of links or connections
		public Point coord;														// Coordinate, used when painting the graph
		
		public DatabaseEntry(URL url, DatabaseEntryState state, int speed, Set<URL> links, Point coord) {	// Constructors
			this.url = url;
			this.state = state;
			this.speed = speed;
			this.links = links;
			this.coord = coord;
		}
		
		public DatabaseEntry(URL url, DatabaseEntryState state, int speed, Set<URL> links) {
			this(url, state, speed, links, null);
		}
		
		public DatabaseEntry(URL url, DatabaseEntryState state) {
			this(url, state, 0, null, null);
		}
	}
	
	public static enum DatabaseEntryState {					// Entry states
		NOT_PROCESSED, PROCESSING, PROCESSED, UNREACHABLE
	}
}
