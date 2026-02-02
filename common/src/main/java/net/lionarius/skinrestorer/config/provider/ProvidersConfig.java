package net.lionarius.skinrestorer.config.provider;

import net.lionarius.skinrestorer.SkinRestorer;
import net.lionarius.skinrestorer.config.provider.collection.CollectionProviderConfig;
import net.lionarius.skinrestorer.util.gson.GsonPostProcessable;

public final class ProvidersConfig implements GsonPostProcessable {
    public static final ProvidersConfig DEFAULT = new ProvidersConfig(
            new MojangProviderConfig(),
            new LittleSkinProviderConfig(),
            new MineskinProviderConfig(),
            new CollectionProviderConfig()
    );
    
    private MojangProviderConfig mojang;
    private LittleSkinProviderConfig littleskin;
    private MineskinProviderConfig mineskin;
    private CollectionProviderConfig collection;
    
    public ProvidersConfig(MojangProviderConfig mojang, LittleSkinProviderConfig littleskin, MineskinProviderConfig mineskin, CollectionProviderConfig collection) {
        this.mojang = mojang;
        this.littleskin = littleskin;
        this.mineskin = mineskin;
        this.collection = collection;
    }
    
    public MojangProviderConfig mojang() {
        return this.mojang;
    }
    
    public LittleSkinProviderConfig littleskin() {
        return this.littleskin;
    }
    
    public MineskinProviderConfig mineskin() {
        return this.mineskin;
    }
    
    public CollectionProviderConfig collection() {
        return this.collection;
    }
    
    @Override
    public void gsonPostProcess() {
        if (this.mojang == null) {
            SkinRestorer.LOGGER.warn("Mojang provider config is null, using default");
            this.mojang = ProvidersConfig.DEFAULT.mojang();
        }
        
        if (this.littleskin == null) {
            SkinRestorer.LOGGER.warn("LittleSkin provider config is null, using default");
            this.littleskin = ProvidersConfig.DEFAULT.littleskin();
        }
        
        if (this.mineskin == null) {
            SkinRestorer.LOGGER.warn("Mineskin provider config is null, using default");
            this.mineskin = ProvidersConfig.DEFAULT.mineskin();
        }
        
        if (this.collection == null) {
            SkinRestorer.LOGGER.warn("Collection provider config is null, using default");
            this.collection = ProvidersConfig.DEFAULT.collection();
        }
    }
}