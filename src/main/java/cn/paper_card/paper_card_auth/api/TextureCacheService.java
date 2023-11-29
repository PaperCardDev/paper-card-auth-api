package cn.paper_card.paper_card_auth.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@SuppressWarnings("unused")
public interface TextureCacheService {
    // 添加或更新缓存，添加返回true，更新返回false
    boolean addOrUpdateByUuid(@NotNull TextureInfo info) throws Exception;

    // 根据UUID查询
    @Nullable TextureInfo queryByUuid(@NotNull UUID uuid) throws Exception;
}
