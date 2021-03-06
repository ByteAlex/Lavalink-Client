package lavalink.client.io.pylon;

import bot.pylon.proto.discord.v1.model.ChannelData;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import lavalink.client.io.Link;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.*;
import lol.up.pylon.gateway.client.exception.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PylonLink extends Link {

    private static final Logger log = LoggerFactory.getLogger(PylonLink.class);
    private final PylonLavalink lavalink;

    PylonLink(PylonLavalink lavalink, String guildId) {
        super(lavalink, guildId);
        this.lavalink = lavalink;
    }

    public void connect(@NonNull Channel voiceChannel) {
        if (voiceChannel.getType() != ChannelData.ChannelType.GUILD_VOICE) {
            throw new IllegalArgumentException("Not a voice channel!");
        }
        connect(voiceChannel, true);
    }

    /**
     * Eventually connect to a channel. Takes care of disconnecting from an existing connection
     *
     * @param channel Channel to connect to
     */
    @SuppressWarnings("WeakerAccess")
    public void connect(@NonNull Channel channel, boolean checkChannel) {
        if (channel.getGuildId() != guild) {
            throw new IllegalArgumentException("The provided VoiceChannel is not a part of the Guild that this " +
                    "AudioManager handles." +
                    "Please provide a VoiceChannel from the proper Guild");
        }
        final Member self = channel.getGuild().flatTransform(Guild::getSelfMember).complete();
        if (!self.hasPermission(channel, Permission.CONNECT) && !self.hasPermission(channel, Permission.MOVE_MEMBERS)) {
            //throw new InsufficientPermissionException(channel, Permission.CONNECT);
            throw new InsufficientPermissionException(Permission.CONNECT);
        }

        //If we are already connected to this VoiceChannel, then do nothing.
        MemberVoiceState voiceState = self.getVoiceState().complete();
        if(voiceState != null) {

            if (checkChannel && channel.getId() == voiceState.getChannel().complete().getId()) {
                return;
            }

            if (voiceState.isInVoiceChannel()) {
                final int userLimit = channel.getData().getUserLimit(); // userLimit is 0 if no limit is set!
                if (!self.isOwner().complete() && !self.hasPermission(Permission.ADMINISTRATOR)) {
                    if (userLimit > 0                                                      // If there is a userlimit
                            && userLimit <= channel.getMembers().complete().size()                    // if that
                            // userlimit is reached
                            && !self.hasPermission(channel, Permission.MOVE_MEMBERS)) // If we don't have voice move
                    // others permissions
                    {
                    /*throw new InsufficientPermissionException(channel, Permission.MOVE_MEMBERS, // then throw exception!
                            "Unable to connect to VoiceChannel due to userlimit! Requires permission " +
                                    "VOICE_MOVE_OTHERS to bypass");*/
                        throw new InsufficientPermissionException(Permission.MOVE_MEMBERS);
                    }
                }
            }
        }

        setState(State.CONNECTING);
        queueAudioConnect(channel.getId());
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public GatewayGrpcClient getClient() {
        return lavalink.getClient();
    }

    @Override
    public void removeConnection() {
        // todo: does pylon handle that for us?
    }

    @Override
    protected void queueAudioDisconnect() {
        Guild g = getClient().getCacheService().getGuild(guild).complete();

        if (g != null) {
            g.disconnectVoice().queue();
        } else {
            log.warn("Attempted to disconnect, but guild {} was not found", guild);
        }
    }

    @Override
    protected void queueAudioConnect(long channelId) {
        final Channel vc = getClient().getCacheService().getChannel(guild, channelId).complete();
        if (vc != null && vc.getType() != ChannelData.ChannelType.GUILD_VOICE) {
            throw new IllegalArgumentException("Not a voice channel!");
        }
        if (vc != null) {
            vc.connectVoice().queue();
        } else {
            log.warn("Attempted to connect, but voice channel {} was not found", channelId);
        }
    }

    /**
     * @return the Guild, or null if it doesn't exist
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    @Nullable
    public Guild getGuild() {
        return getClient().getCacheService().getGuild(guild).complete();
    }
}
