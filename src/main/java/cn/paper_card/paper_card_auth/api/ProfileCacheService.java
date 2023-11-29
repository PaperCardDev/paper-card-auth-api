package cn.paper_card.paper_card_auth.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@SuppressWarnings("unused")
public interface ProfileCacheService {

    // 添加或更新，添加返回true
    boolean addOrUpdate(@NotNull UUID uuid, @NotNull String name) throws Exception;

    // 根据UUID查询
    @Nullable GameProfileInfo queryByUuid(@NotNull UUID uuid) throws Exception;

    // 根据名字查询，时间必须在指定的时间之后，按时间降序排序取出第一个
    @Nullable GameProfileInfo queryByNameTimeAfter(@NotNull String name, long time) throws Exception;
}
