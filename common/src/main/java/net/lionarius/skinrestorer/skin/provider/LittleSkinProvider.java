package net.lionarius.skinrestorer.skin.provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import net.lionarius.skinrestorer.SkinRestorer;
import net.lionarius.skinrestorer.skin.SkinVariant;
import net.lionarius.skinrestorer.util.JsonUtils;
import net.lionarius.skinrestorer.util.PlayerUtils;
import net.lionarius.skinrestorer.util.Result;
import net.lionarius.skinrestorer.util.WebUtils;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class LittleSkinProvider implements SkinProvider {
    
    public static final String PROVIDER_NAME = "littleskin";
    
    private static final URI API_URI;
    
    private static LoadingCache<String, Optional<Property>> SKIN_CACHE;
    
    static {
        try {
            API_URI = new URI("https://littleskin.cn");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public static void reload() {
        createCache();
    }
    
    private static void createCache() {
        var config = SkinRestorer.getConfig().providersConfig().littleskin();
        var time = config.cache().enabled() ? config.cache().duration() : 0;
        
        SKIN_CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(time, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull Optional<Property> load(@NotNull String key) throws Exception {
                        return LittleSkinProvider.loadSkin(key);
                    }
                });
    }
    
    @Override
    public String getArgumentName() {
        return "username";
    }
    
    @Override
    public boolean hasVariantSupport() {
        return false;
    }
    
    @Override
    public Result<Optional<Property>, Exception> fetchSkin(String username, SkinVariant variant) {
        try {
            if (!StringUtil.isValidPlayerName(username))
                throw new IllegalArgumentException("invalid username");
            
            var usernameLowerCase = username.toLowerCase(Locale.ROOT);
            
            return Result.success(SKIN_CACHE.get(usernameLowerCase));
        } catch (UncheckedExecutionException e) {
            return Result.error((Exception) e.getCause());
        } catch (Exception e) {
            return Result.error(e);
        }
    }
    
    public static Optional<Property> loadSkin(String username) throws Exception {
        var profile = LittleSkinProvider.getLittleSkinProfile(username);
        var skinData = LittleSkinProvider.extractSkinData(profile);
        var signedProperty = MineskinSkinProvider.loadSkin(skinData.url().toURI(), skinData.variant());
        
        return signedProperty;
    }
    
    private static GameProfile getLittleSkinProfile(String username) throws IOException {
        var uuid = LittleSkinProvider.getPlayerUuid(username);
        
        var request = HttpRequest.newBuilder()
                .uri(API_URI
                        .resolve("/api/yggdrasil/sessionserver/session/minecraft/profile/")
                        .resolve(uuid + "?unsigned=true")
                )
                .GET()
                .build();
        
        var response = WebUtils.executeRequest(request);
        WebUtils.throwOnClientErrors(response);
        
        if (response.statusCode() != 200)
            throw new IllegalArgumentException("no profile with name " + username);
        
        return JsonUtils.fromJson(response.body(), MinecraftProfilePropertiesResponse.class).profile();
    }
    
    private static String getPlayerUuid(String username) throws IOException {
        var request = HttpRequest.newBuilder()
                .uri(API_URI
                        .resolve("/api/yggdrasil/api/users/profiles/minecraft/")
                        .resolve(username)
                )
                .GET()
                .build();
        
        var response = WebUtils.executeRequest(request);
        WebUtils.throwOnClientErrors(response);
        
        if (response.statusCode() != 200)
            throw new IllegalArgumentException("no user with name " + username);
        
        var json = JsonUtils.fromJson(response.body(), java.util.Map.class);
        return (String) json.get("id");
    }
    
    private static record SkinData(java.net.URL url, SkinVariant variant) {}
    
    private static SkinData extractSkinData(GameProfile profile) throws IOException {
        var textures = PlayerUtils.getPlayerSkin(profile);
        if (textures == null)
            throw new IllegalArgumentException("no skin in profile");

        var json = JsonUtils.fromJson(
            new String(
                java.util.Base64.getDecoder().decode(textures.value()),
                java.nio.charset.StandardCharsets.UTF_8
            ),
            java.util.Map.class
        );

        var skin = (java.util.Map<String, Object>) ((java.util.Map<String, Object>) json.get("textures")).get("SKIN");

        SkinVariant variant = SkinVariant.CLASSIC;
        var metadataObj = skin.get("metadata");
        if (metadataObj instanceof java.util.Map) {
            if ("slim".equals(((java.util.Map<String, Object>) metadataObj).get("model"))) {
                variant = SkinVariant.SLIM;
            }
        }

        return new SkinData(new java.net.URL((String) skin.get("url")), variant);
    }
}