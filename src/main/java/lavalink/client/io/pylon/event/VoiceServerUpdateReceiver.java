package lavalink.client.io.pylon.event;

import bot.pylon.proto.discord.v1.model.VoiceStateData;
import com.google.protobuf.StringValue;
import lavalink.client.io.pylon.PylonLavalink;
import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.entity.Member;
import lol.up.pylon.gateway.client.entity.MemberVoiceState;
import lol.up.pylon.gateway.client.entity.event.VoiceServerUpdateEvent;
import lol.up.pylon.gateway.client.event.AbstractEventReceiver;
import org.json.JSONObject;

public class VoiceServerUpdateReceiver extends AbstractEventReceiver<VoiceServerUpdateEvent> {

    private final PylonLavalink lavalink;

    public VoiceServerUpdateReceiver(PylonLavalink lavalink) {
        this.lavalink = lavalink;
    }

    @Override
    protected void receive(final VoiceServerUpdateEvent event) {
        final Guild guild = event.getGuild().complete();
        final String sessionId = guild.getSelfMember().flatTransform(Member::getVoiceState)
                .transform(MemberVoiceState::getData)
                .transform(VoiceStateData::getSessionId)
                .transform(StringValue::getValue)
                .complete();

        final JSONObject raw = new JSONObject()
                .put("guild_id", String.valueOf(event.getGuildId()))
                .put("token", event.getToken())
                .put("endpoint", event.getEndpoint());

        lavalink.getLink(guild).onVoiceServerUpdate(raw, sessionId);
    }
}
