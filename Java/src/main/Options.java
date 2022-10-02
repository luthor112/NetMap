package main;

public class Options {						// Options for the threads
	public final boolean linksOnly;			// Search for links only
	public final boolean serversOnly;		// Search for servers only
	public final boolean dontLeaveServer;	// Don't leave the server...
	public final String boundTo;			// ... specified here
	
	public Options(boolean linksOnly, boolean serversOnly, boolean dontLeaveServer, String boundTo) {	// Constructor
		this.linksOnly = linksOnly;
		this.serversOnly = serversOnly;
		this.dontLeaveServer = dontLeaveServer;
		this.boundTo = boundTo;
	}
}
