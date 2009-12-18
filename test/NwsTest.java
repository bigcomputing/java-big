package test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.sca.nws.NetWorkSpace;
import com.sca.nws.NwsException;
import com.sca.nws.NwsOperationException;
import com.sca.nws.NwsServer;
import com.sca.nws.NwsVariable;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class NwsTest extends TestCase {
	protected static NetWorkSpace nws;
	protected static NwsServer nwss;

	protected void setUp() throws NwsException {
		try {
			NwsTest.nws = new NetWorkSpace("JUnit WorkSpace");
		} catch (NwsException e) {
			e.printStackTrace();
		}
		NwsTest.nwss = NwsTest.nws.getNwsServer();
	}	

	public static Test suite() {
		return new TestSuite(NwsTest.class);
	}

	public void testDeclare() throws NwsException {
		boolean exceptionRaised = false;

		nws.declare("x", NetWorkSpace.SINGLE);

		// only catch the second exception
		try {
			nws.declare("x", NetWorkSpace.FIFO);
		} catch (NwsOperationException noe) {
			exceptionRaised = true;
		}
		assertTrue(exceptionRaised);
	}

	public void testSingleValue() throws NwsException {
		nws.store("x", 1);
		int exp = 2;
		nws.store("x", exp);
		int act = (Integer) nws.fetch("x");
		assertTrue(act == exp);
	}

	/**
	 * test both blocking and nonblocking version of fetch
	 */
	public void testFetch() throws NwsException {
		for (int i = 0; i < 10; i++)
			nws.store("y", i);

		for (int j = 0; j < 10; j++) {
			int act = (Integer) nws.fetch("y");
			assertTrue(act == j);
		}

		for (int j = 0; j < 10; j++) {
			Object act = nws.fetchTry("y");
			assertTrue(act == null);
		}
	}

	/**
	 * test both blocking and nonblocking version of find
	 */
	public void testFind() throws NwsException {
		double exp = 1.23456;
		nws.store("z", exp);
		double act = (Double) nws.find("z");
		assertTrue(act == exp);

		act = (Double) nws.findTry("z");
		assertTrue(act == exp);

		act = (Double) nws.fetch("z");
		assertTrue(act == exp);
	}

	public void testDeleteVar() throws NwsException {
		nws.deleteVar("x");
		nws.deleteVar("y");
		nws.deleteVar("z");
	}

	public void testLIFO() throws NwsException {
		nws.declare("y", NetWorkSpace.LIFO);

		for (int i = 1; i <= 10; i++)
			nws.store("y", i);

		for (int j = 10; j > 0; j--) {
			int k = (Integer) nws.fetch("y");
			assertTrue(k == j);
		}

		for (int j = 10; j > 0; j--) {
			Object obj = nws.fetchTry("y");
			assertTrue(obj == null);
		}
	}

	public void testMulti() throws NwsException {
		nws.declare("z", NetWorkSpace.MULTI);

		for (int i = 0; i < 10; i++)
			nws.store("z", i);

		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int j = 0; j < 10; j++)
			result.add((Integer) nws.fetch("z"));

		assertTrue(result.size() == 10);

		Collections.sort(result);

		for (int j = 0; j < 10; j++) {
			int act = result.get(j);
			assertTrue(act == j);
		}
	}

	public void testIterator() throws NwsException {
		for (int i = 0; i < 10; i++)
			nws.store("iterator", i);

		for (int k = 0; k < 3; k++) {
			Iterator<Integer> it = nws.ifindTry("iterator");
			for (int i = 0; i < 10; i++) {
				assertTrue(it.hasNext());
				int j = it.next();
				assertTrue(j == i);
			}
			assertTrue(!it.hasNext());
		}

		Iterator it = nws.ifetchTry("iterator");
		for (int i = 0; i < 10; i++) {
			assertTrue(it.hasNext());
			Integer j = (Integer) it.next();
			assertTrue(j.intValue() == i);
		}
		assertTrue(!it.hasNext());

		it = nws.ifindTry("iterator");
		assertTrue(!it.hasNext());
	}

	public void testForEach() throws NwsException {
		for (int i = 0; i < 10; i++)
			nws.store("v", i);

		NwsVariable<Integer> v = nws.ifindTry("v");

		for (int j = 0; j < 4; j++) {
			Iterator<Integer> it = v.iterator();
			int n = 0;
			while (it.hasNext()) {
				assertTrue(n == it.next());
				n++;
			}
			assertTrue(n == 10);

			n = 0;
			for (int i: v) {
				assertTrue(n == i);
				n++;
			}
			assertTrue(n == 10);
		}
	}
}
