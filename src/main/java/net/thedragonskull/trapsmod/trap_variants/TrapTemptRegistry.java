package net.thedragonskull.trapsmod.trap_variants;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static List<EntityType<?>> getMobsForItem(ItemStack stack) {
        return temptItems.entrySet()
                .stream()
                .filter(entry -> entry.getValue().test(stack))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}

