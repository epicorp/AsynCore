package net.devtech.asyncore.util;

import java.awt.Dimension;

public class Size2i {
	private final int width;
	private final int height;

	public Size2i(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public Dimension toDimension() {
		return new Dimension(this.width, this.height);
	}
}
