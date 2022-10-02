package main;

import java.util.*;

public class WorkerThreadCollection extends ArrayList<WorkerThread> {	// Collection which holds WorkerThreads
	public static final long serialVersionUID = 1L;
	
	public void resumeAllWork() {		// Resume the work of all the threads in the list
		for (WorkerThread wt : this)
			wt.resumeWork();
	}
	
	public void pauseAllWork() {		// Pause the work of all the threads in the list
		for (WorkerThread wt : this)
			wt.pauseWork();
	}
	
	public void endAllWork() {			// Stop all the threads in the list
		for (WorkerThread wt : this)
			wt.endWork();
	}
	
	public int countWorking() {			// Count working threads
		int working = 0;
		
		for (WorkerThread wt : this)
			if (wt.isWorking())
				working++;
		
		return working;
	}
	
	public int countAlive() {
		int alive = 0;
		
		for (WorkerThread wt : this)
			if (wt.isAlive())
				alive++;
		
		return alive;
	}
	
	public int countState(Thread.State tstate) {	// Count threads with the set state
		int number = 0;
		
		for (WorkerThread wt : this)
			if (wt.getState().equals(tstate))
				number++;
		
		return number;
	}
}
