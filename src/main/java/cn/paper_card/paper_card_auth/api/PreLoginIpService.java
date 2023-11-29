package cn.paper_card.paper_card_auth.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@SuppressWarnings("unused")
public interface PreLoginIpService {
    boolean addOrUpdateByUuidAndIp(@NotNull UUID uuid, @NotNull String ip, @NotNull String name, long time) throws Exception;

    @Nullable PreLoginIpInfo queryLastByUuid(@NotNull UUID uuid) throws Exception;
}
