package moe.caa.multilogin.core.auth.service.yggdrasil;

import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;
import moe.caa.multilogin.core.configuration.service.yggdrasil.BaseYggdrasilServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.ohc.LoggingInterceptor;
import moe.caa.multilogin.core.ohc.RetryInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 根据用户名向各 Yggdrasil 服务查询档案（无需 serverId）
 * 用于在玩家从未登录过服务器时也能添加白名单
 */
public class ProfileLookupService {

    private final MultiCore core;

    public ProfileLookupService(MultiCore core) {
        this.core = core;
    }

    /**
     * Поиск по всем настроенным сервисам
     */
    public List<LookupResult> lookupByUsername(String username) {
        List<LookupResult> results = new ArrayList<>();
        for (BaseServiceConfig config : core.getPluginConfig().getServiceIdMap().values()) {
            if (!(config instanceof BaseYggdrasilServiceConfig)) continue;
            results.addAll(lookupByUsernameAndService(username, (BaseYggdrasilServiceConfig) config));
        }
        return results;
    }

    /**
     * Поиск по конкретному сервису
     */
    public List<LookupResult> lookupByUsernameAndService(String username, BaseYggdrasilServiceConfig yggConfig) {
        List<LookupResult> results = new ArrayList<>();
        String url = yggConfig.getProfileLookupUrl(username);
        if (url == null || url.isEmpty()) return results;
        try {
            GameProfile profile = doLookup(yggConfig, url);
            if (profile != null && profile.getId() != null) {
                results.add(new LookupResult(profile, yggConfig));
            }
        } catch (Exception e) {
            LoggerProvider.getLogger().debug(
                    "Profile lookup failed for service " + yggConfig.getId() + " username=" + username, e);
        }
        return results;
    }

    private GameProfile doLookup(BaseYggdrasilServiceConfig config, String url) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(config.getRetry(), config.getRetryDelay()))
                .addInterceptor(new LoggingInterceptor())
                .writeTimeout(Duration.ofMillis(config.getTimeout()))
                .readTimeout(Duration.ofMillis(config.getTimeout()))
                .connectTimeout(Duration.ofMillis(config.getTimeout()))
                .proxy(config.getAuthProxy().getProxy())
                .proxyAuthenticator(config.getAuthProxy().getProxyAuthenticator())
                .build();

        Request request = new Request.Builder()
                .get()
                .url(url)
                .header("User-Agent", core.getHttpRequestHeaderUserAgent())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 204 || response.code() == 404) return null;
            String body = Objects.requireNonNull(response.body()).string();
            if (body.isEmpty() || body.equals("null")) return null;
            return core.getGson().fromJson(body, GameProfile.class);
        }
    }

    public static class LookupResult {
        public final GameProfile profile;
        public final BaseYggdrasilServiceConfig serviceConfig;

        public LookupResult(GameProfile profile, BaseYggdrasilServiceConfig serviceConfig) {
            this.profile = profile;
            this.serviceConfig = serviceConfig;
        }
    }
}


