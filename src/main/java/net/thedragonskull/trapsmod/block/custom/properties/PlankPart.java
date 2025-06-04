package net.thedragonskull.trapsmod.block.custom.properties;

import net.minecraft.util.StringRepresentable;

public enum PlankPart implements StringRepresentable {
    BASE("base"),
    EXTENSION("extension");

    private final String name;

    private PlankPart(String pName) {
        this.name = pName;
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}
