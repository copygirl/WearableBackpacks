package net.mcft.copy.backpacks.client;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Slightly funny way of creating an IBakedModel from a base
 *  model which doesn't have any of its "overlay" texture quads. */
@SideOnly(Side.CLIENT)
public class BakedModelDefaultTexture implements IBakedModel
{
	private final IBakedModel _baseModel;
	private final List<BakedQuad>[] _sideQuads;
	private final List<BakedQuad> _defaultQuads;
	
	@SuppressWarnings("unchecked")
	public BakedModelDefaultTexture(IBakedModel base) {
		_baseModel = base;
		_sideQuads = Arrays.stream(EnumFacing.VALUES)
			.map((side) -> filterQuads(_baseModel.getQuads(null, side, 0)))
			.toArray((length) -> new List[length]);
		_defaultQuads = filterQuads(_baseModel.getQuads(null, null, 0));
	}
	private static List<BakedQuad> filterQuads(List<BakedQuad> source) {
		return source.stream()
			.filter((quad) -> !quad.getSprite().getIconName().contains("overlay"))
			.collect(Collectors.toList());
	}
	
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		return ((side != null) ? _sideQuads[side.getIndex()] : _defaultQuads);
	}
	
	public boolean isAmbientOcclusion() { return _baseModel.isAmbientOcclusion(); }
	
	public boolean isGui3d() { return _baseModel.isGui3d(); }
	
	public boolean isBuiltInRenderer() { return _baseModel.isBuiltInRenderer(); }
	
	public TextureAtlasSprite getParticleTexture() { return _baseModel.getParticleTexture(); }
	
	@Deprecated
	public ItemCameraTransforms getItemCameraTransforms() { return _baseModel.getItemCameraTransforms(); }
	
	public ItemOverrideList getOverrides() { return _baseModel.getOverrides(); }
}
