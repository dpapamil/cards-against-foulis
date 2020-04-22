package com.papamilios.dimitris.cardsagainstfoulis.UI.games;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.papamilios.dimitris.cardsagainstfoulis.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GamesListAdapter extends RecyclerView.Adapter<GameViewHolder> implements GameViewHolder.OnItemSelectedListener  {

    private final LayoutInflater mInflater;
    private List<GameInfo> mGames = Collections.emptyList(); // Cached copy of words
    private int mSelectedPos = -1;
    private List<GameViewHolder> mViewHolders = new ArrayList<GameViewHolder>();
    private Set<GameSelectionListener> mListeners = new HashSet<GameSelectionListener>();

    public GamesListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void addListener(GameSelectionListener listener) {
        mListeners.add(listener);
    }
    public void removeListener(GameSelectionListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.game_item, parent, false);
        GameViewHolder viewHolder = new GameViewHolder(itemView, this);
        mViewHolders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GameViewHolder holder, int position) {
        GameInfo gameInfo = mGames.get(position);
        holder.setGameInfo(gameInfo);
        holder.setChecked(mSelectedPos == position);
    }

    public void setGames(List<GameInfo> games) {
        mGames = games;
        notifyDataSetChanged();
    }

    public void updateGamePlayers(@NonNull String gameId, @NonNull List<String> players) {
        GameInfo gameInfo = new GameInfo(gameId, "", new Date());
        int index = mGames.indexOf(gameInfo);
        if (index >= 0 && index < mGames.size()) {
            mGames.get(index).setPlayers(players);
            notifyItemChanged(index);
        }
    }

    public GameInfo getSelectedGame() {
        if (mSelectedPos < 0 || mSelectedPos >= mGames.size()) {
            return null;
        }

        return mGames.get(mSelectedPos);
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }

    // Select the card with the given text
    public void setSelected(String gameId) {
        for (int i = 0; i < mGames.size(); i++) {
            GameInfo gameInfo = mGames.get(i);
            if (gameInfo.getId().equals(gameId)) {
                mSelectedPos = -1;
                onSelectionChanged(gameInfo);
                break;
            }
        }
    }

    // Clear all selections
    public void clearSelection() {
        mSelectedPos = -1;
        notifyDataSetChanged();
    }

    // Enable/disable selection by clicking
    public void enableSelection(boolean enable) {
        for (GameViewHolder viewHolder : mViewHolders) {
            viewHolder.enableSelection(enable);
        }
    }

    @Override
    public void onSelectionChanged(GameInfo gameInfo) {
        Integer index = mGames.indexOf(gameInfo);
        if (index == mSelectedPos) {
            mSelectedPos = -1;
        } else {
            mSelectedPos = index;
        }
        notifyDataSetChanged();
        notifyListeners();
    }

    private void notifyListeners() {
        for (GameSelectionListener listener : mListeners) {
            listener.onSelectionChanged();
        }
    }

    // A listener interface to listen to selection events
    public interface GameSelectionListener {

        public void onSelectionChanged();
    }
}
