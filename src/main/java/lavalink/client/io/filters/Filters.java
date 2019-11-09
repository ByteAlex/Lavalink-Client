package lavalink.client.io.filters;

import lavalink.client.player.LavalinkPlayer;

import javax.annotation.Nonnull;

import static com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer.BAND_COUNT;

@SuppressWarnings("unused")
public class Filters {

    @SuppressWarnings("WeakerAccess")
    public static float DEFAULT_VOLUME = 1.0f;

    private final LavalinkPlayer player;
    private final Runnable onCommit;
    private float volume = DEFAULT_VOLUME;
    private float[] bands = new float[BAND_COUNT];

    /**
     * Intended for internal use only
     */
    public Filters(LavalinkPlayer player, Runnable onCommit) {
        this.player = player;
        this.onCommit = onCommit;
    }


    @Nonnull
    public LavalinkPlayer getPlayer() {
        return player;
    }

    public float[] getBands() {
        return bands;
    }

    /**
     * Configures the equalizer.
     *
     * @param band the band to change, values 0-14
     * @param gain the gain in volume for the given band, range -0.25 (mute) to 1.0 (quadruple).
     */
    public void setBand(int band, float gain) {
        if (gain < -0.25 || gain > 1) throw new IllegalArgumentException("Gain must be -0.25 to 1.0");
        bands[band] = gain;
    }

    public float getVolume() {
        return volume;
    }

    /**
     * @param volume where 1.0f is regular volume. Values greater than 1.0f are allowed, but may cause clipping.
     */
    public void setVolume(float volume) {
        if (volume < 0) throw new IllegalArgumentException("Volume must be greater than 0");
        this.volume = volume;
    }


    /**
     * Commits these filters to the Lavalink server.
     * <p>
     * The client may choose to commit changes at any time, even if this method is never invoked.
     */
    public void commit() {
        onCommit.run();
    }
}