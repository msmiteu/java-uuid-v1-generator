package eu.msmit.uuid.v1.test;

import junit.framework.TestCase;

import org.junit.Test;

import eu.msmit.uuid.v1.Node;

public class TestNode extends TestCase {

	@Test
	public void testEquality() {
		Node node1 = new Node();
		Node node2 = new Node();

		assertFalse(node1.equals(node2));
	}
}
