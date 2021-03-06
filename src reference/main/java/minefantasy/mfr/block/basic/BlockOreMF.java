package minefantasy.mfr.block.basic;

import minefantasy.mfr.MineFantasyReborn;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.GameRegistry;
import minefantasy.mfr.init.CreativeTabMFR;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Random;

public class BlockOreMF extends Block {
    public int rarity;
    private int xp;
    private Item drop;
    private int dropMin;
    private int dropMax;
    private Random rand = new Random();

    public BlockOreMF(String name, int harvestLevel) {
        this(name, harvestLevel, 0);
    }

    public BlockOreMF(String name, int harvestLevel, int rarity) {
        this(name, harvestLevel, rarity, null, 1, 1, 0);
    }

    public BlockOreMF(String name, int harvestLevel, int rarity, Item drop, int min, int max, int xp) {
        this(name, harvestLevel, rarity, drop, min, max, xp, Material.ROCK);
    }

    public BlockOreMF(String name, int harvestLevel, int rarity, Item drop, int min, int max, int xp,
                      Material material) {
        super(material);
        this.xp = xp;
        this.drop = drop;
        this.rarity = rarity;
        this.dropMin = min;
        this.dropMax = max;
        GameRegistry.findRegistry(Block.class).register(this);
        setRegistryName(name);
        setUnlocalizedName(MineFantasyReborn.MODID + "." + name);
        setSoundType(SoundType.STONE);
        if (material == Material.ROCK) {
            this.setHarvestLevel("pickaxe", harvestLevel);
        }
        this.setCreativeTab(CreativeTabMFR.tabOres);
        OreDictionary.registerOre(name, this);
    }

    public BlockOreMF setBlockSoundType(SoundType soundType) {
        setSoundType(soundType);
        return this;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return drop != null ? drop : Item.getItemFromBlock(this);
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random rand) {
        return rand.nextInt(dropMax);
    }

    /**
     * Returns the usual quantity dropped by the block plus a bonus of 1 to 'i'
     * (inclusive).
     */
    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand) {
        if (fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(this.blockState.getBaseState(), rand, fortune)) {
            int j = rand.nextInt(fortune + 2) - 1;

            if (j < 0) {
                j = 0;
            }

            return this.quantityDropped(rand) * (j + 1);
        } else {
            return this.quantityDropped(rand);
        }
    }

    /**
     * Drops the block items with a specified chance of dropping the specified items
     */
    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        if (xp > 0 && this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this)) {
            return rand.nextInt(xp * 2);
        }
        return 0;
    }
}
