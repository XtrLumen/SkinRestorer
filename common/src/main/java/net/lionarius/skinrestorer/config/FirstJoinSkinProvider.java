package net.lionarius.skinrestorer.config;

import com.google.gson.annotations.SerializedName;
import net.lionarius.skinrestorer.skin.provider.CollectionSkinProvider;
import net.lionarius.skinrestorer.skin.provider.LittleSkinProvider;
import net.lionarius.skinrestorer.skin.provider.MojangSkinProvider;

public enum FirstJoinSkinProvider {
    @SerializedName(value = "MOJANG", alternate = {"mojang"})
    MOJANG(MojangSkinProvider.PROVIDER_NAME),
    @SerializedName(value = "LITTLESKIN", alternate = {"littleskin", "LITTLE_SKIN", "little_skin"})
    LITTLESKIN(LittleSkinProvider.PROVIDER_NAME),
    @SerializedName(value = "COLLECTION", alternate = {"collection"})
    COLLECTION(CollectionSkinProvider.PROVIDER_NAME);
    
    private final String name;
    
    FirstJoinSkinProvider(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}