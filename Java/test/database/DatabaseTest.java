package database;

import static org.junit.Assert.*;
import org.junit.Test;

import java.net.*;
import java.util.*;

import junit.framework.Assert;
import database.Database.DatabaseEntry;
import database.Database.DatabaseEntryState;

public class DatabaseTest {

	@Test
	public void testPutNoServersOnly() {
		try {
			Database database = new Database();
			DatabaseEntry de = new DatabaseEntry(new URL("http://www.mywebpage.net/"), DatabaseEntryState.NOT_PROCESSED);
			database.put(de, false);
			List<DatabaseEntry> list = database.getDatabase();
			Assert.assertEquals(list.get(0).url.equals(de.url), true);
			Assert.assertEquals(list.get(0).state.equals(de.state), true);
		} catch (MalformedURLException ex) { }
	}
	
	@Test
	public void testEntryStateCountNoServersOnly() {
		try {
			Database database = new Database();
			database.put(new DatabaseEntry(new URL("http://www.mywebpage.net/"), DatabaseEntryState.NOT_PROCESSED), false);
			database.put(new DatabaseEntry(new URL("http://www.mywebpage2.net/"), DatabaseEntryState.PROCESSED), false);
			database.put(new DatabaseEntry(new URL("http://www.mywebpage3.net/"), DatabaseEntryState.UNREACHABLE), false);
			database.put(new DatabaseEntry(new URL("http://www.mywebpage4.net/"), DatabaseEntryState.NOT_PROCESSED), false);
			database.put(new DatabaseEntry(new URL("http://www.mywebpage5.net/"), DatabaseEntryState.NOT_PROCESSED), false);
			Assert.assertEquals(database.entryStateCount(DatabaseEntryState.NOT_PROCESSED), 3);
		} catch (MalformedURLException ex) { }
	}
}
