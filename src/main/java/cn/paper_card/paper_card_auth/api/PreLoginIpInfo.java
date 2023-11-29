package cn.paper_card.paper_card_auth.api;

import java.util.UUID;

public record PreLoginIpInfo(UUID uuid, String name, String ip, long time, long count) {

}
