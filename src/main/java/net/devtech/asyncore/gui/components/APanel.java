package net.devtech.asyncore.gui.components;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.devtech.asyncore.gui.graphics.InventoryGraphics;
import net.devtech.asyncore.gui.graphics.NestedGraphics;
import net.devtech.asyncore.util.Size2i;
import org.bukkit.inventory.ItemStack;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * a compound of components, can paint multiple components
 */
public class APanel implements AComponent {
	protected final List<AComponent> components = new ArrayList<>();
	protected final List<Point> locations = new ArrayList<>();
	protected final Size2i size;

	public APanel(Size2i size2i) {
		this.size = size2i;
	}

	@Override
	public void draw(InventoryGraphics inventory) {
		for (int i = 0; i < this.components.size(); i++) {
			AComponent component = this.components.get(i);
			Point bounds = this.locations.get(i);
			InventoryGraphics graphics = new NestedGraphics(inventory, bounds.x, bounds.y);
			component.draw(graphics);
		}
	}

	@Override
	public boolean attemptAdd(Point point, ItemStack add) {
		return this.actionComponent(point, (c, p) -> c.attemptAdd(p, add));
	}

	@Override
	public boolean attemptTake(Point point, ItemStack stack) {
		return this.actionComponent(point, (c, p) -> c.attemptTake(p, stack));
	}

	@Override
	public boolean attemptSwap(Point point, ItemStack add, ItemStack take) {
		return this.actionComponent(point, (c, p) -> c.attemptSwap(p, add, take));
	}

	@Override
	public Size2i getSize() {
		return this.size;
	}

	public void addComponent(Point location, AComponent component) {
		this.components.add(component);
		this.locations.add(location);
	}

	public void removeComponent(AComponent point) {
		int index = this.components.indexOf(point);
		this.locations.remove(index);
		this.components.remove(index);
	}

	@Override
	public void resync(InventoryGraphics graphics) {
		for (int i = 0; i < this.components.size(); i++) {
			Point relative = this.locations.get(i);
			AComponent component = this.components.get(i);
			NestedGraphics nested = new NestedGraphics(graphics, relative.x, relative.y);
			component.resync(nested);
		}
	}

	private boolean actionComponent(Point point, BiPredicate<AComponent, Point> predicate) {
		boolean noComponent = true; // non-component slots are immutable by default
		for (int index : this.getComponentsAt(point)) {
			Point coordinate = this.locations.get(index);
			int xDist = point.x - coordinate.x;
			int yDist = point.y - coordinate.y;
			AComponent component = this.components.get(index);
			noComponent = predicate.test(component, new Point(xDist, yDist));
			if(noComponent) // if cancelled at any point
				return true; // cancell the event
		}
		return noComponent;
	}
	private IntList getComponentsAt(Point point) {
		IntList components = new IntArrayList();
		for (int i = 0; i < this.locations.size(); i++) {
			Point coordinate = this.locations.get(i);
			int xDist = point.x - coordinate.x;
			int yDist = point.y - coordinate.y;
			if(xDist >= 0 && yDist >= 0) {
				AComponent component = this.components.get(i);
				Size2i size = component.getSize();
				if(xDist < size.getWidth() && yDist < size.getHeight()) {
					components.add(i);
				}
			}
		}
		return components;
	}
}
