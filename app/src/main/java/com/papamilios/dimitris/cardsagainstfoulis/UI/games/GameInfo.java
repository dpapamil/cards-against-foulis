package com.papamilios.dimitris.cardsagainstfoulis.UI.games;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GameInfo {
    private String mId = null;
    private String mHostName = null;
    private Date mDateCreated = null;
    private List<String> mPlayers = null;

    public GameInfo(@NonNull String id, @NonNull String host, @NonNull Date dateCreated) {
        mId = id;
        mHostName = host;
        mDateCreated = dateCreated;
    }

    public String getId() { return mId; }

    public String getHostName() { return mHostName; }
    public String getFormattedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss 'at' dd/MM/yyyy");
        return dateFormat.format(mDateCreated);
    }

    public List<String> getPlayers() { return mPlayers; }
    public String getFormattedPlayersNames() {
        String names = "";
        for (int i = 0; i < mPlayers.size(); i++) {
            names += mPlayers.get(i);
            if (i < mPlayers.size() - 1) {
                names += ", ";
            }
        }
        return names;
    }
    public void setPlayers(List<String> players) {
        mPlayers = players;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        GameInfo otherGame = (GameInfo) other;
        return mId.equals(otherGame.getId());
    }
}
