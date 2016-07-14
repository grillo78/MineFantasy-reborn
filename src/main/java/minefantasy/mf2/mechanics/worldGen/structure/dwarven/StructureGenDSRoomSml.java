package minefantasy.mf2.mechanics.worldGen.structure.dwarven;

import minefantasy.mf2.block.list.BlockListMF;
import minefantasy.mf2.block.tileentity.decor.TileEntityAmmoBox;
import minefantasy.mf2.material.WoodMaterial;
import minefantasy.mf2.mechanics.worldGen.structure.StructureGenAncientForge;
import minefantasy.mf2.mechanics.worldGen.structure.StructureModuleMF;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class StructureGenDSRoomSml extends StructureModuleMF
{
	public StructureGenDSRoomSml(World world, StructureCoordinates position)
	{
		super(world, position);
	}
	
	protected int getHeight()
	{
		return 5;
	}
	protected int getDepth() 
	{
		return 8;
	}
	protected int getWidth() 
	{
		return 4;
	}
	@Override
	public boolean canGenerate() 
	{
		int width = getWidth(), depth = getDepth(), height = getHeight();
		int filledSpaces = 0, emptySpaces = 0;
		
		for(int x = -width; x <= width; x ++)
		{
			for(int y = 0; y <= height; y ++)
			{
				for(int z = 1; z <= depth; z ++)
				{
					Block block = this.getBlock(x, y, z);
					if(!allowBuildOverBlock(block) || this.isUnbreakable(x, y, z, direction))
					{
						return false;
					}
					if(!block.getMaterial().isSolid())
					{
						++emptySpaces;
					}
					else
					{
						++filledSpaces;
					}
				}
			}
		}
		if(WorldGenDwarvenStronghold.debug_air)
		{
			return true;
		}
		return ((float) emptySpaces / (float)filledSpaces) < 0.25F;//at least 75% full
	}
	private boolean allowBuildOverBlock(Block block)
	{
		if(block == Blocks.stonebrick || block == BlockListMF.reinforced_stone)
		{
			return false;
		}
		return true;
	}
	
	
	@Override
	public void generate()
	{
		int width = getWidth(), depth = getDepth(), height = getHeight();
		
		for(int x = -width; x <= width; x ++)
		{
			for(int z = 0; z <= depth; z ++)
			{
				Object[] blockarray;
				//FLOOR
				blockarray = getFloor(width, depth, x, z);
				if(blockarray != null)
				{
					placeBlock((Block)blockarray[0], 0, x, 0, z);
				}
				//WALLS
				for(int y = 1; y <= height; y ++)
				{
					blockarray = getWalls(width, depth, x, z);
					if(blockarray != null && this.allowBuildOverBlock(getBlock(x, y, z)))
					{
						int meta = (Boolean)blockarray[1] ? StructureGenAncientForge.getRandomMetadata(rand) : 0;
						placeBlock((Block)blockarray[0], meta, x, y, z);
					}
				}
				//CEILING
				blockarray = getCeiling(width, depth, x, z);
				if(blockarray != null)
				{
					int meta = (Boolean)blockarray[1] ? StructureGenAncientForge.getRandomMetadata(rand) : 0;
					placeBlock((Block)blockarray[0], meta, x, height, z);
				}
			}
		}
		placeBlock(Blocks.air, 0, 0, 1, 0);
		placeBlock(Blocks.air, 0, 0, 2, 0);
		
		placeBlock(BlockListMF.reinforced_stone_framed, 0, -1, 1, -1);
		placeBlock(BlockListMF.reinforced_stone, 		0, -1, 2, -1);
		placeBlock(BlockListMF.reinforced_stone_framediron, 0, -1, 3, -1);
		
		placeBlock(BlockListMF.reinforced_stone_framed, 0, 1, 1, -1);
		placeBlock(BlockListMF.reinforced_stone, 		0, 1, 2, -1);
		placeBlock(BlockListMF.reinforced_stone_framediron, 0, 1, 3, -1);
		
		placeBlock(BlockListMF.reinforced_stone, 0, 0, 3, -1);
		placeBlock(Blocks.air, 0, 0, 1, -1);
		placeBlock(Blocks.air, 0, 0, 2, -1);
		
		buildHomeFurnishings(width, depth, height);
	}

	private Object[] getFloor(int width, int depth, int x, int z) 
	{
		if(x== -(width-1) || x == (width-1) || z == 1 || z == depth-1)
		{
			return new Object[]{BlockListMF.reinforced_stone, 0};
		}
		return new Object[]{BlockListMF.cobble_pavement, 0};
	}
	private Object[] getCeiling(int width, int depth, int x, int z) 
	{
		if(x== -(width-1) || x == (width-1) || z == 1 || z == depth-1)
		{
			return new Object[]{BlockListMF.reinforced_stone, false};
		}
		return new Object[]{Blocks.stonebrick, true};
	}
	private Object[] getWalls(int width, int depth, int x, int z)
	{
		if(x == -width || x == width || z == depth ||z == 0)
		{
			if((x == -width && (z == depth || z == 0)) || (x == width && (z == depth || z == 0)))
			{
				return new Object[]{BlockListMF.reinforced_stone, false};
			}
			
			return new Object[]{Blocks.stonebrick, true};
		}
		return new Object[]{Blocks.air, false};
	}
	
	private void buildHomeFurnishings(int width, int depth, int height) 
	{
		placeBlock(Blocks.furnace, rotateLeft(), width, 2, 2);
		
		placeBlock(Blocks.double_stone_slab, 0, width-3, 1, 1);
		placeBlock(Blocks.double_stone_slab, 0, width-3, 1, 2);
		
		for(int x = width-1; x >= (width-4); x --)
		{
			placeBlock(Blocks.stonebrick, StructureGenAncientForge.getRandomMetadata(rand), x, 1, 4);
			placeBlock(Blocks.stonebrick, StructureGenAncientForge.getRandomMetadata(rand), x, 2, 4);
			placeBlock(Blocks.stone_slab, 0, x, 3, 4);
		}
		placeBlock(Blocks.stone_slab, 0, width-1, 1, 6);
		placeBlock(Blocks.stone_slab, 0, width-2, 1, 6);
		placeBlock(Blocks.cauldron, 0, width-2, 1, 1);
		
		placeBlock(Blocks.stone_brick_stairs, this.getStairDirection(reverse()), -(width-1), 1, 1);
		placeBlock(Blocks.stone_slab, 8, -(width-1), 1, 2);
		placeBlock(Blocks.stone_brick_stairs, this.getStairDirection(direction), -(width-1), 1, 3);
		
		
		placeBlock(BlockListMF.crate_basic, rotateLeft(), width-3, 1, 6);
		TileEntityAmmoBox crate = (TileEntityAmmoBox)this.getTileEntity(width-3, 1, 6, direction);
		if(crate != null)
		{
			crate.setMaterial(WoodMaterial.getMaterial("ScrapWood"));
		}
	}
}