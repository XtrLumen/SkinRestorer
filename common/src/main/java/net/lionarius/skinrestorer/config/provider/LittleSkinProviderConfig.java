package net.lionarius.skinrestorer.config.provider;

import net.lionarius.skinrestorer.skin.provider.LittleSkinProvider;

public final class LittleSkinProviderConfig extends BuiltInProviderConfig {
    private static final CacheConfig DEFAULT_CACHE_VALUE = new CacheConfig(true, 60);
    
    public LittleSkinProviderConfig() {
        super(LittleSkinProvider.PROVIDER_NAME, DEFAULT_CACHE_VALUE);
    }
    
    @Override
    public void gsonPostProcess() {
        super.validate(LittleSkinProvider.PROVIDER_NAME, DEFAULT_CACHE_VALUE);
    }
}