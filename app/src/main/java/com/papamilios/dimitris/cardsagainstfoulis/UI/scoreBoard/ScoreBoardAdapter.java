package com.papamilios.dimitris.cardsagainstfoulis.UI.scoreBoard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.UI.games.GameInfo;
import com.papamilios.dimitris.cardsagainstfoulis.controller.GamePlayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScoreBoardAdapter extends RecyclerView.Adapter<ScoreViewHolder> {

    private final LayoutInflater mInflater;
    private ScoreBoard mScoreBoard = new ScoreBoard();
    private List<ScoreViewHolder> mViewHolders = new ArrayList<ScoreViewHolder>();

    public ScoreBoardAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ScoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.score_board_item, parent, false);
        ScoreViewHolder viewHolder = new ScoreViewHolder(itemView);
        mViewHolders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ScoreViewHolder holder, int position) {
        GamePlayer player = mScoreBoard.getPlayer(position);
        Integer score = mScoreBoard.getScore(position);
        holder.setScore(player.getName(), score);
        int offset = mScoreBoard.getScore(0).intValue() - score.intValue();
        holder.setFontSize(offset);
    }

    public void initialiseScoreBoard(List<GamePlayer> gamePlayers) {
        mScoreBoard.initialiseBoard(gamePlayers);
        notifyDataSetChanged();
    }

    public void initialiseScoreBoard(@NonNull ScoreBoard scoreBoard) {
        mScoreBoard = scoreBoard;
        notifyDataSetChanged();
    }

    public void updateScoreBoard(@NonNull ScoreBoard updatedScoreBoard) {
        if (updatedScoreBoard.playerCount() != mScoreBoard.playerCount()) {
            throw new AssertionError("We can only have the same number of players");
        }
        // For every current player, find out if they've changed position
        Map<Integer, Integer> movements = new HashMap<Integer, Integer>();
        int pos = 0;
        while (pos < mScoreBoard.playerCount()) {
            String playerName = updatedScoreBoard.getPlayer(pos).getName();
            int oldPos = mScoreBoard.getPlayerPosition(playerName);
            if (pos != oldPos) {
                if (pos > oldPos) {
                    throw new AssertionError("Only one way iteration");
                }
                movements.put(oldPos, pos);
                pos = oldPos + 1;
            } else {
                pos++;
            }
        }

        // Update the scoreboard
        mScoreBoard.copyFrom(updatedScoreBoard);
        for (Map.Entry<Integer, Integer> movement : movements.entrySet()) {
            int oldPos = movement.getKey().intValue();
            int newPos = movement.getValue().intValue();
            notifyItemMoved(oldPos, newPos);
        }

        notifyItemRangeChanged(0, mScoreBoard.playerCount());
    }

    @Override
    public int getItemCount() {
        return mScoreBoard.playerCount();
    }
}
