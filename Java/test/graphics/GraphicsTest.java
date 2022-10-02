package graphics;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

public class GraphicsTest {

	@Test
	public void testPow2() {
		Assert.assertEquals(Graph.pow2(3), 8);
		Assert.assertEquals(Graph.pow2(5), 32);
		Assert.assertEquals(Graph.pow2(10), 1024);
	}
}
