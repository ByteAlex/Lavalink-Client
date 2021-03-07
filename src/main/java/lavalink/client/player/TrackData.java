package lavalink.client.player;
/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * Created by napster on 25.09.17.
 * <p>
 * Optional object to enrich an AudioTrack via {@code AudioTrack#setUserData}
 */
public class TrackData {

    public final long id;
    public final long authorId;

    public TrackData(long id, long authorId) {
        this.id = id;
        this.authorId = authorId;
    }

    @Override
    public String toString() {
        return id + "," + authorId;
    }

    static public TrackData createFromString(String stringTrackData) {
        String[] parts = stringTrackData.split(",");
        long id = Long.parseLong(parts[0]);
        long authorId = Long.parseLong(parts[1]);
        return new TrackData(id, authorId);
    }
}
