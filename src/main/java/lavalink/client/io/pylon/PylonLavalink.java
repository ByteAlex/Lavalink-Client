package lavalink.client.io.pylon;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import lavalink.client.io.Lavalink;
import lavalink.client.io.pylon.event.*;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.entity.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PylonLavalink extends Lavalink<PylonLink> {

    private static final Logger log = LoggerFactory.getLogger(PylonLavalink.class);

    private final GatewayGrpcClient client;
    private boolean autoReconnect = true;

    public PylonLavalink(final GatewayGrpcClient client, int numShards) {
        super(String.valueOf(client.getDefaultBotId()), numShards);
        this.client = client;
        client.registerReceiver(VoiceServerUpdateEvent.class, new VoiceServerUpdateReceiver(this));
        client.registerReceiver(VoiceStateUpdateEvent.class, new VoiceStateUpdateReceiver(this));
        client.registerReceiver(GuildMemberRemoveEvent.class, new GuildLeaveReceiver(this));
        client.registerReceiver(ChannelDeleteEvent.class, new ChannelDeleteReceiver(this));
        client.registerReceiver(ReadyEvent.class, new ReconnectReceiver(this));
    }

    public boolean getAutoReconnect() {
        return autoReconnect;
    }

    @SuppressWarnings("unused")
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public GatewayGrpcClient getClient() {
        return client;
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public PylonLink getLink(Guild guild) {
        return getLink(String.valueOf(guild.getId()));
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    @Nullable
    public PylonLink getExistingLink(Guild guild) {
        return getExistingLink(String.valueOf(guild.getId()));
    }

    @Override
    protected PylonLink buildNewLink(String guildId) {
        return new PylonLink(this, guildId);
    }
}
