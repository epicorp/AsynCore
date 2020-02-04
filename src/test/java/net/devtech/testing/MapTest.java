package net.devtech.testing;

public class MapTest {
	public static void main(String[] args) {
		int rx = 0, y = 0, rz = 0;
		short pack = (short) (rx & 15 | (rz & 15) << 4 | y << 8);

		//int upcast = 0xffff & pack;
		System.out.printf("%d %d %d", pack & 15, (pack >> 4) & 15, ((pack & 0xffff) >> 8));
	}
}
