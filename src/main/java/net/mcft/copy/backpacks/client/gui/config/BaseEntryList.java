package net.mcft.copy.backpacks.client.gui.config;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiContainer;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.client.gui.GuiLayout;
import net.mcft.copy.backpacks.client.gui.config.IConfigEntry;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Status.Severity;

@SideOnly(Side.CLIENT)
public abstract class BaseEntryList<T> extends GuiLayout implements IConfigEntry {
	
	protected final List<T> defaultValue;
	protected final List<T> previousValue;
	
	public final GuiLayout layoutList;
	public final GuiButton buttonAdd;
	
	
	public BaseEntryList(int width, List<T> defaultValue, List<T> previousValue) {
		super(Direction.VERTICAL);
		setCenteredHorizontal(width);
		
		this.defaultValue  = defaultValue;
		this.previousValue = previousValue;
		
		layoutList = new GuiLayout(Direction.VERTICAL);
		layoutList.setFillHorizontal();
		
		buttonAdd = new GuiButton(0, DEFAULT_ENTRY_HEIGHT, TextFormatting.GREEN + "+");
		buttonAdd.setFill();
		buttonAdd.setAction(this::addButtonPressed);
		
		addFixed(layoutList);
		addFixed(buttonAdd);
		
		setValue(previousValue);
	}
	
	
	protected abstract Entry<T> createListEntry();
	
	protected void addButtonPressed() { addEntry(); }
	
	
	public void addEntry(T value)
		{ addEntry().setValue(value); }
	public Entry<T> addEntry() {
		Entry<T> entry = createListEntry();
		layoutList.addFixed(entry);
		return entry;
	}
	
	public Stream<Entry<T>> getEntries() {
		return layoutList.getChildren().stream()
			.filter(Entry.class::isInstance)
			.map(Entry.class::cast);
	}
	public Stream<T> getValueAsStream()
		{ return getEntries().map(Entry::getValue); }
	public List<T> getValue()
		{ return getValueAsStream().collect(Collectors.toList()); }
	
	
	public void setValue(List<T> value) {
		layoutList.clear();
		value.forEach(this::addEntry);
	}
	
	
	public abstract static class Entry<T> extends GuiLayout {
		
		public final MoveButton buttonMove;
		public final GuiButton buttonRemove;
		
		public Entry(BaseEntryList<T> owningList) {
			super(Direction.HORIZONTAL);
			setFillHorizontal();
			
			buttonMove = new MoveButton();
			
			buttonRemove = new GuiButton(DEFAULT_ENTRY_HEIGHT, DEFAULT_ENTRY_HEIGHT, TextFormatting.RED + "x");
			buttonRemove.setAction(() -> owningList.layoutList.remove(this));
		}
		
		public abstract T getValue();
		public abstract void setValue(T value);
		
		public abstract List<Status> getStatus();
		
		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, buttonMove.renderYOffset, 0);
			
			Severity severity = Status.getSeverity(getStatus());
			enableBlendAlphaStuffs();
			drawColoredRectARGB(-4, -1, getWidth() + 8, getHeight() + 2, severity.backgroundColor);
			disableBlendAlphaStuffs();
			super.draw(mouseX, mouseY, partialTicks);
			
			GlStateManager.popMatrix();
		}
		
		public class MoveButton extends GuiButton {
			
			public static final int WIDTH = 12;
			protected int yOffset = 0;
			protected float renderYOffset = 0.0F;
			
			public MoveButton() {
				super(WIDTH, DEFAULT_ENTRY_HEIGHT - 2, "=");
				setCenteredVertical();
			}
			
			@Override
			public boolean canDrag() { return true; }
			
			@Override
			@SuppressWarnings("unchecked")
			public void onDragged(int mouseX, int mouseY, int deltaX, int deltaY, int startX, int startY) {
				GuiContainer parent = Entry.this.getParent();
				List<GuiElementBase> elements = parent.getChildren();
				int currentIndex  = elements.indexOf(Entry.this);
				int elementY      = parent.getChildY(Entry.this);
				int elementHeight = Entry.this.getHeight();
				
				int elementCenter = elementY + elementHeight / 2;
				yOffset = Math.max(Math.min(mouseY - startY, parent.getHeight() - elementCenter), -elementCenter);
				
				int sign = Integer.signum(yOffset);
				while ((sign != 0) && (sign == Integer.signum(yOffset))) {
					int targetIndex = currentIndex + sign;
					if ((yOffset < 0) ? (targetIndex < 0)
					                  : (targetIndex > elements.size() - 1)) return;
					Entry<T> target = (Entry<T>)elements.get(targetIndex);
					if (!target.buttonMove.isEnabled()) return;
					
					int targetY      = parent.getChildY(target);
					int targetHeight = target.getHeight();
					if ((yOffset < 0) ? (elementY + yOffset > targetY + targetHeight / 4)
					                  : (elementY + elementHeight + yOffset < targetY + targetHeight * 3 / 4)) return;
					
					int diff = targetY - elementY;
					elementY += diff; yOffset -= diff; renderYOffset -= diff;
					target.buttonMove.renderYOffset += diff;
					parent.move(currentIndex, targetIndex);
					currentIndex = targetIndex;
				}
			}
			
			@Override
			public void onMouseUp(int mouseButton, int mouseX, int mouseY) { yOffset = 0; }
			
			@Override
			public void draw(int mouseX, int mouseY, float partialTicks) {
				float speed = 0.25F;
				renderYOffset = renderYOffset * (1 - speed) + yOffset * speed;
				super.draw(mouseX, mouseY, partialTicks);
			}
			
		}
		
	}
	
	
	// IConfigEntry implementation
	
	@Override
	public boolean isChanged()
		{ return !iteratorEquals(getValueAsStream().iterator(), previousValue.iterator()); }
	@Override
	public boolean isDefault()
		{ return iteratorEquals(getValueAsStream().iterator(), defaultValue.iterator()); }
	@Override
	public final boolean isValid()
		{ return getEntries().allMatch(entry -> (Status.getSeverity(entry.getStatus()) != Severity.ERROR)); }
	
	@Override
	public void undoChanges() { setValue(previousValue); }
	@Override
	public void setToDefault() { setValue(defaultValue); }
	
	
	private static boolean iteratorEquals(Iterator<?> it1, Iterator<?> it2) {
		while (it1.hasNext() && it2.hasNext())
			if (!Objects.equals(it1.next(), it2.next())) return false;
		return (!it1.hasNext() && !it2.hasNext());
	}
	
}
