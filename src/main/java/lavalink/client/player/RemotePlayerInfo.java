package lavalink.client.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.Nullable;

public class RemotePlayerInfo {

    @Nullable
    final AudioTrack playing;
    final boolean paused;
    final long position;

    @Nullable
    final AudioTrack lastTrack;

    public RemotePlayerInfo(@Nullable AudioTrack playing, boolean paused, long position, @Nullable AudioTrack lastTrack) {
        this.playing = playing;
        this.paused = paused;
        this.position = position;
        this.lastTrack = lastTrack;
    }

    public boolean isPaused() {
        return paused;
    }

    public long getPosition() {
        return position;
    }

    @Nullable
    public AudioTrack getPlaying() {
        return playing;
    }

    @Nullable
    public AudioTrack getLastTrack() {
        return lastTrack;
    }
}
