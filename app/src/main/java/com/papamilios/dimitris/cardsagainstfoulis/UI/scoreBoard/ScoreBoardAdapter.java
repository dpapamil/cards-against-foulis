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
import java.util.List;

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
        holder.setFontSize(position);
    }

    public void initialiseScoreBoard(List<GamePlayer> gamePlayers) {
        mScoreBoard.initialiseBoard(gamePlayers);
        notifyDataSetChanged();
    }

    public void increasePlayerScore(@NonNull GamePlayer player) {
        mScoreBoard.increasePlayerScore(player);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mScoreBoard.playerCount();
    }
}
