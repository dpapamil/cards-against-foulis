package com.papamilios.dimitris.cardsagainstfoulis.UI.games;

import com.papamilios.dimitris.cardsagainstfoulis.R;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class GameViewHolder extends RecyclerView.ViewHolder {
    // The views of the holder
    private View mParentView = null;
    private TextView mHostNameView = null;
    private TextView mTimeCreatedView = null;
    private TextView mPlayersView = null;

    // The game info
    private GameInfo mGameInfo;

    private boolean mSelected = false;
    private boolean mSelectionEnabled = true;

    private OnItemSelectedListener mListener = null;

    public GameViewHolder(View itemView, GameViewHolder.OnItemSelectedListener listener) {
        super(itemView);
        mListener = listener;
        mParentView = itemView;
        mHostNameView = (TextView) itemView.findViewById(R.id.host_name_id);
        mTimeCreatedView = (TextView) itemView.findViewById(R.id.game_time_id);
        mPlayersView = (TextView) itemView.findViewById(R.id.game_players);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Don't do anything if we're not allowed to click and select
                if (!mSelectionEnabled) {
                    return;
                }

                mListener.onSelectionChanged(mGameInfo);

            }
        });
    }

    public void setGameInfo(@NonNull GameInfo gameInfo) {
        mGameInfo = gameInfo;
        mHostNameView.setText(gameInfo.getHostName());
        mTimeCreatedView.setText(gameInfo.getFormattedDate());
        mPlayersView.setText(gameInfo.getFormattedPlayersNames());
    }

    public void setChecked(boolean value) {
        mParentView.setSelected(value);
        mSelected = value;
    }

    // Enable/disable selection by clicking
    public void enableSelection(boolean enable) {
        mSelectionEnabled = enable;
    }

    // A listener interface to listen to selection events
    public interface OnItemSelectedListener {

        public void onSelectionChanged(GameInfo gameInfo);
    }
}
