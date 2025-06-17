package net.thedragonskull.trapsmod.trap_variants;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.Map;

public class TrapTemptRegistry {

    private static final Map<EntityType<?>, Ingredient> temptItems = new HashMap<>();

    public static void register(EntityType<?> type, Ingredient ingredient) {
        temptItems.put(type, ingredient);
    }

    public static Ingredient getTemptIngredientFor(EntityType<?> type) {
        return temptItems.get(type);
    }

    public static boolean hasTemptItem(EntityType<?> type) {
        return temptItems.containsKey(type);
    }
}

