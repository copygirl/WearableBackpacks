package net.mcft.copy.backpacks.client.gui.config.custom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.client.gui.GuiLayout;
import net.mcft.copy.backpacks.client.gui.config.IConfigValue;
import net.mcft.copy.backpacks.config.Status;

public class EntryValueMulti<T> extends GuiLayout implements IConfigValue<List<T>> {
	
	public EntryValueMulti(int count, Class<? extends IConfigValue<T>> elementClass) {
		super(Direction.HORIZONTAL);
		if (count < 2) throw new IllegalArgumentException("count must be at least 2");
		try {
			for (int i = 0; i < count; i++)
				addWeighted((GuiElementBase)elementClass.newInstance());
		} catch (ReflectiveOperationException ex)
			{ throw new RuntimeException(ex); }
	}
	
	@Override
	public Optional<List<T>> getValue() {
		List<T> values = children.stream()
			.map(IConfigValue.class::cast).map(IConfigValue<T>::getValue)
			.filter(Optional::isPresent).map(Optional::get)
			.collect(Collectors.toList());
		return (values.size() == children.size())
			? Optional.of(values) : Optional.empty();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setValue(List<T> value) {
		if (value.size() != children.size()) throw new IllegalArgumentException();
		for (int i = 0; i < children.size(); i++)
			((IConfigValue<T>)children.get(i)).setValue(value.get(i));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (children.get(0) instanceof IConfigValue.ShowsStatus)
		for (GuiElementBase child : children) {
			Optional<T> value = ((IConfigValue<T>)child).getValue();
			((IConfigValue.ShowsStatus)child).setStatus(value.isPresent()
				? Collections.emptyList() : Arrays.asList(Status.INVALID));
		}
		super.draw(mouseX, mouseY, partialTicks);
	}
	
}
