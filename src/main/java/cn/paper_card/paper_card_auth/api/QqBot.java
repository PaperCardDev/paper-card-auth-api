package cn.paper_card.paper_card_auth.api;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface QqBot {
    void sendAtMessage(long qq, @NotNull String message);
}
