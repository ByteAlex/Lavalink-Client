package lavalink.client.io.pylon.event;

import lavalink.client.io.Link;
import lavalink.client.io.pylon.PylonLavalink;
import lavalink.client.io.pylon.PylonLink;
import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.entity.event.VoiceStateUpdateEvent;
import lol.up.pylon.gateway.client.event.AbstractEventReceiver;

public class VoiceStateUpdateReceiver extends AbstractEventReceiver<VoiceStateUpdateEvent> {

    private final PylonLavalink lavalink;

    public VoiceStateUpdateReceiver(PylonLavalink lavalink) {
        this.lavalink = lavalink;
    }

    @Override
    protected void receive(final VoiceStateUpdateEvent event) {
        if(event.getVoiceState().getMember().getId() != event.getBotId()) {
            return; // only bot events
        }
        Channel channel = event.getVoiceState().getChannel().complete();
        PylonLink link = lavalink.getLink(event.getGuild().complete());

        if (channel == null) {
            // Null channel means disconnected
            if (link.getState() != Link.State.DESTROYED) {
                link.onDisconnected();
            }
        } else {
            link.setChannel(String.valueOf(channel.getId())); // Change expected channel
        }
    }

}
