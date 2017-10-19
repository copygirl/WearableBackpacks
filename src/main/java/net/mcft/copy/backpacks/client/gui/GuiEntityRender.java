package net.mcft.copy.backpacks.client.gui;

import java.io.File;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import net.mcft.copy.backpacks.client.gui.control.GuiField;

public class GuiEntityRender extends GuiElementBase {
	
	private static final int RESERVED_SPACE = 16;
	
	private Entity _entity = null;
	
	private float _yawDefault =    0.0F;
	private float _yaw        =    0.0F;
	private float _yawMin     = -180.0F;
	private float _yawMax     =  180.0F;
	
	private float _pitchDefault =   0.0F;
	private float _pitch        =  15.0F;
	private float _pitchMin     = -25.0F;
	private float _pitchMax     =  25.0F;
	
	private float _centerX = 0.5F;
	private float _centerY = 0.5F;
	private float _zoom = 1.0F;
	
	private int _colorBackground = Color.BLACK;
	private int _colorBorder     = GuiField.COLOR_BORDER_DEFAULT;
	
	public GuiEntityRender(Entity entity)
		{ this(80, 140, entity); }
	public GuiEntityRender(int width, int height)
		{ this(width, height, (Entity)null); }
	public GuiEntityRender(int width, int height, Entity entity)
		{ this(0, 0, width, height, entity); }
	public GuiEntityRender(int x, int y, int width, int height, Entity entity) {
		setPosition(x, y);
		setSize(width, height);
		setEntity(entity);
	}
	
	public GuiEntityRender(Class<? extends Entity> entityClass)
		{ this(80, 140, entityClass); }
	public GuiEntityRender(int width, int height, Class<? extends Entity> entityClass)
		{ this(0, 0, width, height, entityClass); }
	public GuiEntityRender(int x, int y, int width, int height, Class<? extends Entity> entityClass) {
		setPosition(x, y);
		setSize(width, height);
		setEntity(entityClass);
	}
		
	public Entity getEntity() { return _entity; }
	public void setEntity(Entity value) { _entity = value; }
	
	public void setEntity(Class<? extends Entity> value) {
		try {
			setEntity((value != null)
				? value.getConstructor(World.class)
				       .newInstance(DummyWorld.INSTANCE)
				: null);
		} catch (ReflectiveOperationException ex)
			{ throw new RuntimeException(ex); }
	}
	
	public void setYaw(float value, float min, float max)
		{ setYaw(value); setYawControl(min, max); }
	public void setYaw(float value) { _yawDefault = value; }
	public void setYawControl(float min, float max) { _yawMin = min; _yawMax = max; }
	
	public void setPitch(float value, float min, float max)
		{ setPitch(value); setPitchControl(min, max); }
	public void setPitch(float value) { _pitchDefault = value; }
	public void setPitchControl(float min, float max) { _pitchMin = min; _pitchMax = max; }
	
	public void setCenter(float x, float y) { _centerX = x; _centerY = y; }
	public void setZoom(float value) { _zoom = value; }
	
	public void setBackgroundColor(int value) { _colorBackground = value; }
	public void setBorderColor(int value) { _colorBorder = value; }
	
	@Override
	public boolean canDrag() { return true; }
	
	@Override
	public void onDragged(int mouseX, int mouseY, int deltaX, int deltaY, int startX, int startY) {
		_yaw   += deltaX * 2;
		_pitch -= deltaY;
		boolean yawWrap   = (_yawMax - _yawMin >= 360.0F);
		boolean pitchWrap = (_pitchMax - _pitchMin >= 360.0F);
		if (_yaw < _yawMin) _yaw = yawWrap ? _yaw + 360.0F : _yawMin;
		if (_yaw > _yawMax) _yaw = yawWrap ? _yaw - 360.0F : _yawMax;
		if (_pitch < _pitchMin) _pitch = pitchWrap ? _pitch + 360.0F : _pitchMin;
		if (_pitch > _pitchMax) _pitch = pitchWrap ? _pitch - 360.0F : _pitchMax;
	}
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		int w = getWidth();
		int h = getHeight();
		
		enableBlendAlphaStuffs();
		setRenderColorARGB(_colorBackground); drawRect(1, 1, w - 2, h - 2);
		setRenderColorARGB(_colorBorder);     drawOutline(0, 0, w, h);
		disableBlendAlphaStuffs();
		
		Entity entity = getEntity();
		if (entity == null) return;
		
		AxisAlignedBB bbox = entity.getRenderBoundingBox();
		float entityWidth  = (float)(bbox.maxX - bbox.minX);
		float entityHeight = (float)(bbox.maxY - bbox.minY);
		float scale = Math.min((w - RESERVED_SPACE) / entityWidth,
		                       (h - RESERVED_SPACE) / entityHeight) * _zoom;
		float yaw   = _yawDefault + _yaw;
		float pitch = _pitchDefault + _pitch;
		
		getContext().pushScissor(this, 1, 1, w - 2, h - 2);
		
		if (entity.getEntityWorld() == null)
			entity.setWorld(DummyWorld.INSTANCE);
		if (getMC().player == null) {
			World world = entity.getEntityWorld();
			if (!(world instanceof DummyWorld)) return;
			getMC().player = ((DummyWorld)world).player;
			getMC().player.setWorld(world);
		}
		
		setRenderColorARGB(Color.WHITE);
		GlStateManager.enableDepth();
		// From GuiInventory.drawEntityOnScreen
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(w * _centerX, h * _centerY, 100.0F);
		GlStateManager.scale(-scale, scale, scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.translate(0.0F, -(entityHeight / 2), 0.0F);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		
		RenderHelper.enableStandardItemLighting();
		
		RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
		rendermanager.setPlayerViewY(180.0F);
		rendermanager.setRenderShadow(false);
		rendermanager.doRenderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
		rendermanager.setRenderShadow(true);
		
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		
		GlStateManager.disableDepth();
		if (getMC().player instanceof DummyPlayerSP)
			getMC().player = null;
		getContext().popScissor();
	}
	
	
	public static class DummyWorld extends World {
		public static final DummyWorld INSTANCE = new DummyWorld();
		public final DummyPlayerSP player;
		
		public DummyWorld() {
			super(new DummySaveHandler(), new WorldInfo(new WorldSettings(
					0, GameType.SURVIVAL, false, false, WorldType.DEFAULT), "DummyWorld"
				), new WorldProviderSurface(), null, true);
			provider.setWorld(this);
			player = new DummyPlayerSP(this);
		}
		
		@Override protected IChunkProvider createChunkProvider() { return null; }
		@Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { return false; }
		
		// These use chunk provider
		@Override public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) { return null; }
		@Override public boolean isBlockNormalCube(BlockPos pos, boolean _default) { return false; }
		
		// These use getChunkFromChunkCoords
		@Override public Biome getBiomeForCoordsBody(BlockPos pos) { return Biomes.DEFAULT; }
		@Override public boolean canSeeSky(BlockPos pos) { return true; }
		@Override public int getLight(BlockPos pos) { return 15; }
		@Override public int getLight(BlockPos pos, boolean checkNeighbors) { return 15; }
		@Override public int getLightFor(EnumSkyBlock type, BlockPos pos) { return 15; }
		@Override public IBlockState getBlockState(BlockPos pos) { return Blocks.AIR.getDefaultState(); }
	}
	
	public static class DummySaveHandler implements ISaveHandler {
		public WorldInfo loadWorldInfo() { return null; }
		public void checkSessionLock() throws MinecraftException {  }
		public IChunkLoader getChunkLoader(WorldProvider provider) { return null; }
		public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {  }
		public void saveWorldInfo(WorldInfo worldInformation) {  }
		public IPlayerFileData getPlayerNBTManager() { return null; }
		public void flush() {  }
		public File getWorldDirectory() { return null; }
		public File getMapFileFromName(String mapName) { return null; }
		public TemplateManager getStructureTemplateManager() { return null; }
	}
	
	public static class DummyPlayerSP extends EntityPlayerSP {
		public DummyPlayerSP(World world) {
			super(getMC(), world, new NetHandlerPlayClient(
					getMC(), null, null, getMC().getSession().getProfile()
				), null, null);
		}
		
		
	}
	
}