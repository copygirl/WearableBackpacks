package net.mcft.copy.backpacks.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.GuiTextureResource;

@SideOnly(Side.CLIENT)
public class GuiImage extends GuiElementBase {
	
	private GuiTextureResource _texture;
	private int _texU, _texV;
	
	public GuiImage(int width, int height, GuiTextureResource texture)
		{ this(width, height, texture, 0, 0); }
	public GuiImage(int width, int height, GuiTextureResource texture, int u, int v)
		{ this(0, 0, width, height, texture, u, v); }
	public GuiImage(int x, int y, int width, int height,
	                GuiTextureResource texture, int u, int v) {
		setPosition(x, y);
		setSize(width, height);
		setTexture(texture, u, v);
		setTextureUV(u, v);
	}
	
	public GuiTextureResource getTexture() { return _texture; }
	public void setTexture(GuiTextureResource value) { _texture = value; }
	
	public int getTextureU() { return _texU; }
	public int getTextureV() { return _texV; }
	public void setTextureUV(int u, int v) { _texU = u; _texV = v; }
	
	public void setTexture(GuiTextureResource texture, int u, int v)
		{ setTexture(texture); setTextureUV(u, v); }
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		GuiTextureResource texture = getTexture();
		if (texture == null) return;
		setRenderColorARGB(Color.WHITE);
		texture.bind();
		texture.drawQuad(0, 0, getTextureU(), getTextureV(), getWidth(), getHeight());
	}
	
}
