package net.thedragonskull.trapsmod.trap_variants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BearTrapVariantRegistry {

    public interface TrapVariant {
        void apply(Level level, BlockPos pos, @Nullable LivingEntity entity);
    }

    private static final Map<Item, TrapVariant> variantMap = new HashMap<>();

    public static void register(Item item, TrapVariant Variant) {
        variantMap.put(item, Variant);
    }

    public static void triggerVariant(Item item, Level level, BlockPos pos, @Nullable LivingEntity entity) {
        TrapVariant variant = variantMap.get(item);
        if (variant != null) {
            variant.apply(level, pos, entity);
        }
    }

}
