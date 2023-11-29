package cn.paper_card.paper_card_auth.api;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse;
import com.mojang.util.UUIDTypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SuppressWarnings("unused")
public abstract class MinecraftSessionService {

    public final static UUID INVALID_UUID = new UUID(0, 0);
    public final static String INVALID_NAME = "INVALID_NAME";

    public final static String KEY_KICK_MESSAGE = "kick-message";

    private static class GameProfileSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {
        public GameProfileSerializer() {
        }

        public GameProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = (JsonObject) json;
            UUID id = object.has("id") ? (UUID) context.deserialize(object.get("id"), UUID.class) : null;
            String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : null;
            return new GameProfile(id, name);
        }

        public JsonElement serialize(GameProfile src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            if (src.getId() != null) {
                result.add("id", context.serialize(src.getId()));
            }

            if (src.getName() != null) {
                result.addProperty("name", src.getName());
            }

            return result;
        }
    }

    protected final @NotNull Gson gson;

    protected final static String HAS_JOIN_URL_SUFFIX = "/session/minecraft/hasJoined";

    protected final @NotNull URL baseUrl;

    public MinecraftSessionService(@NotNull URL baseUrl) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(GameProfile.class, new GameProfileSerializer());
        builder.registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer());
        builder.registerTypeAdapter(UUID.class, new UUIDTypeAdapter());
        builder.registerTypeAdapter(ProfileSearchResultsResponse.class, new ProfileSearchResultsResponse.Serializer());
        this.gson = builder.create();
        this.baseUrl = baseUrl;
    }

    protected GameProfile transformProfile(@NotNull GameProfile gameProfile) {
        return gameProfile;
    }

    protected static void closeStreams(@NotNull BufferedReader bufferedReader, @NotNull InputStreamReader inputStreamReader, @NotNull InputStream inputStream) throws IOException {

        IOException exception = null;
        try {
            bufferedReader.close();
        } catch (IOException e) {
            exception = e;
        }

        try {
            inputStreamReader.close();
        } catch (IOException e) {
            exception = e;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            exception = e;
        }

        if (exception != null) throw exception;
    }

    protected @Nullable GameProfile requestOnlineProfile(GameProfile gameProfile, String serverId, InetAddress inetAddress) throws AuthenticationUnavailableException {

        final InputStream inputStream = getInputStream(gameProfile, serverId, inetAddress);

        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        final StringBuilder stringBuffer = new StringBuilder();

        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append('\n');
            }
        } catch (IOException e) {
            // 依次关流，忽略异常
            try {
                closeStreams(bufferedReader, inputStreamReader, inputStream);
            } catch (IOException ignored) {
            }

            throw new AuthenticationUnavailableException(e);
        }

        // 全部读取完毕

        // JSON解析
        try {
            gameProfile = this.gson.fromJson(stringBuffer.toString(), GameProfile.class);
        } catch (JsonSyntaxException e) {
            try {
                closeStreams(bufferedReader, inputStreamReader, inputStream);
            } catch (IOException ignored) {
            }
            throw new AuthenticationUnavailableException("无法解析JSON数据", e);
        }

        // 依次关流
        try {
            closeStreams(bufferedReader, inputStreamReader, inputStream);
        } catch (IOException e) {
            throw new AuthenticationUnavailableException("关闭输入流时IO异常", e);
        }

        return gameProfile;
    }

    private @NotNull InputStream getInputStream(GameProfile gameProfile, String serverId, InetAddress inetAddress) throws AuthenticationUnavailableException {

        final URL url = getUrl(gameProfile, serverId, inetAddress);

        // URL连接
        final URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(4000); // 设置4秒超时时间
            urlConnection.setReadTimeout(4000);
            urlConnection.connect();
        } catch (IOException e) {
            throw new AuthenticationUnavailableException("无法连接到身份验证服务器：" + url.toExternalForm(), e);
        }

        // 输入流
        final InputStream inputStream;
        try {
            inputStream = urlConnection.getInputStream();
        } catch (IOException e) {
            throw new AuthenticationUnavailableException("打开输入流时IO异常", e);
        }

        return inputStream;
    }


    private @NotNull URL getUrl(GameProfile gameProfile, String serverId, InetAddress inetAddress) throws AuthenticationUnavailableException {

        final String stringUrl;
        if (inetAddress == null) {
            stringUrl = "%s%s?username=%s&serverId=%s".formatted(this.baseUrl.toString(), HAS_JOIN_URL_SUFFIX, gameProfile.getName(), serverId);
        } else {
            stringUrl = "%s%s?username=%s&serverId=%s&ip=%s".formatted(this.baseUrl.toString(), HAS_JOIN_URL_SUFFIX, gameProfile.getName(), serverId, inetAddress.getHostAddress());
        }

        final URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            throw new AuthenticationUnavailableException("URL错误！", e);
        }
        return url;
    }

    public GameProfile hasJoinedServer(GameProfile gameProfile, String serverId, InetAddress inetAddress) throws AuthenticationUnavailableException {
        final GameProfile p = this.requestOnlineProfile(gameProfile, serverId, inetAddress);
        if (p == null) return null;
        return this.transformProfile(p);
    }

    public static @NotNull GameProfile createInvalidProfile() {
        return new GameProfile(INVALID_UUID, INVALID_NAME);
    }

    public static @NotNull URL constantURL(@NotNull String url) {
        final URL result;
        try {
            result = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static class Mojang extends MinecraftSessionService {

        public Mojang() {
            // "https://sessionserver.mojang.com/session/minecraft/hasJoined"
            super(MinecraftSessionService.constantURL("https://sessionserver.mojang.com"));
        }
    }
}
