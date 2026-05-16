package de.ellpeck.prettypipes.pipe.modules.filter;

import de.ellpeck.prettypipes.misc.ItemFilter;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FilterIncreaseModuleItem extends Item {
    public FilterIncreaseModuleItem() { super(new Properties()); }
    public ItemFilter getItemFilter(ItemStack module, PipeBlockEntity tile) { throw new AssertionError("stub"); }
}
