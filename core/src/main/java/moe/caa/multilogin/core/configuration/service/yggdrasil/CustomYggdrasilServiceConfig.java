package moe.caa.multilogin.core.configuration.service.yggdrasil;

import moe.caa.multilogin.api.service.ServiceType;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.ProxyConfig;
import moe.caa.multilogin.core.configuration.SkinRestorerConfig;
import org.jetbrains.annotations.NotNull;

/**
 * 自定义 Yggdrasil
 */
public class CustomYggdrasilServiceConfig extends BaseYggdrasilServiceConfig {
    private final String url;
    private final String postContent;
    private final String trackIpContent;
    private final HttpRequestMethod method;
    // nullable — если не задан, lookup не поддерживается
    private final String profileLookupUrl;

    public CustomYggdrasilServiceConfig(int id, String name, InitUUID initUUID, String initNameFormat, boolean whitelist, SkinRestorerConfig skinRestorer, boolean trackIp, int timeout, int retry, long retryDelay, ProxyConfig authProxy, String url, String postContent, String trackIpContent, HttpRequestMethod method) throws ConfException {
        this(id, name, initUUID, initNameFormat, whitelist, skinRestorer, trackIp, timeout, retry, retryDelay, authProxy, url, postContent, trackIpContent, method, null);
    }

    public CustomYggdrasilServiceConfig(int id, String name, InitUUID initUUID, String initNameFormat, boolean whitelist, SkinRestorerConfig skinRestorer, boolean trackIp, int timeout, int retry, long retryDelay, ProxyConfig authProxy, String url, String postContent, String trackIpContent, HttpRequestMethod method, String profileLookupUrl) throws ConfException {
        super(id, name, initUUID, initNameFormat, whitelist, skinRestorer, trackIp, timeout, retry, retryDelay, authProxy);
        this.url = url;
        this.postContent = postContent;
        this.trackIpContent = trackIpContent;
        this.method = method;
        this.profileLookupUrl = profileLookupUrl;
    }


    @Override
    protected String getAuthURL() {
        return url;
    }

    @Override
    protected String getAuthPostContent() {
        return postContent;
    }

    @Override
    protected String getAuthTrackIpContent() {
        return trackIpContent;
    }

    @Override
    public HttpRequestMethod getHttpRequestMethod() {
        return method;
    }

    @Override
    public String getProfileLookupUrl(String username) {
        if (profileLookupUrl == null || profileLookupUrl.isEmpty()) return null;
        return profileLookupUrl.replace("{username}", username);
    }

    @NotNull
    @Override
    public ServiceType getServiceType() {
        return ServiceType.CUSTOM_YGGDRASIL;
    }
}
