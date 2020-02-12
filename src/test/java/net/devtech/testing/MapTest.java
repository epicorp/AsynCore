package net.devtech.testing;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Iterator;

public class MapTest {
	public static void main(String[] args) {
		int rx = 0, y = 0, rz = 0;
		short pack = (short) (rx & 15 | (rz & 15) << 4 | y << 8);

		//int upcast = 0xffff & pack;
		System.out.printf("%d %d %d\n", pack & 15, (pack >> 4) & 15, ((pack & 0xffff) >> 8));

		Int2IntMap map = new Int2IntOpenHashMap();
		map.put(0, 0);
		map.put(1, 1);
		map.put(2, 2);
		System.out.println(map);
		map.forEach(map::remove);
		System.out.println(map);
	}
}
