package main;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

import database.Database;

public class MainTest {

	@Test
	public void testWorkerThreadCreation() {
		Database database = new Database();
		Options options = new Options(true, false, false, null);
		WorkerThread wt = new WorkerThread(database, options);
		wt.start();
		Assert.assertEquals(wt.isAlive(), true);
		wt.endWork();
	}
	
	@Test
	public void testWorkerThreadResumePause() {
		Database database = new Database();
		Options options = new Options(true, false, false, null);
		WorkerThread wt = new WorkerThread(database, options);
		wt.start();
		
		wt.resumeWork();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ex) { }
		Assert.assertEquals(wt.isWorking(), true);
		
		wt.pauseWork();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ex) { }
		Assert.assertEquals(wt.isWorking(), false);
		
		wt.endWork();
	}
}
