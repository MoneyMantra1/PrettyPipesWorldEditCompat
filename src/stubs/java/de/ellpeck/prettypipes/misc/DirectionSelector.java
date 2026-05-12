package de.ellpeck.prettypipes.misc;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;

public class DirectionSelector {
    public record Data(String direction) {
        public static final Codec<DirectionSelector.Data> CODEC = Codec.STRING.xmap(DirectionSelector.Data::new, DirectionSelector.Data::direction);
        public static final DataComponentType<DirectionSelector.Data> TYPE = DataComponentType.<DirectionSelector.Data>builder().persistent(CODEC).cacheEncoding().build();
    }
}
