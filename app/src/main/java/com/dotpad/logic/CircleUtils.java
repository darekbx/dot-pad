package com.dotpad.logic;

public class CircleUtils {

	public static int calculateXSegment(int offset, int size, int value, int segments) {
		return (int)(offset + size * Math.sin(value * 2 * Math.PI / segments));
	}
	
	public static int calculateYSegment(int offset, int size, int value, int segments) {
		return (int)(offset + size * Math.cos(value * 2 * Math.PI / segments));
	}
}