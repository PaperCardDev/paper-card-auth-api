package cn.paper_card.paper_card_auth.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record GameProfileInfo(@NotNull UUID uuid, @NotNull String name, long time) {
}
