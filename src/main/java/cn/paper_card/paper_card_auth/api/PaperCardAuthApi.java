package cn.paper_card.paper_card_auth.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("unused")
public interface PaperCardAuthApi {

    @NotNull ProfileCacheService getProfileCacheService();

    @NotNull PreLoginIpService getPreLoginIpService();

    @NotNull TextureCacheService getTextureCacheService();

    void onPreLoginCheckMojangOffline(@NotNull Object event, @Nullable QqBot qqBot);


    void onPreLoginCheckInvalid(@NotNull Object event);


    // 接收主群消息，返回回复消息，返回null表示不回复
    @Nullable String onGroupMessage(long qq, @NotNull String message);
}
