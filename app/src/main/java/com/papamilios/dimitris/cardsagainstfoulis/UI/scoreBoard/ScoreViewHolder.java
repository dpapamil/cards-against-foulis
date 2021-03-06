package com.papamilios.dimitris.cardsagainstfoulis.UI.scoreBoard;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.papamilios.dimitris.cardsagainstfoulis.R;

import static java.lang.StrictMath.max;


public class ScoreViewHolder extends RecyclerView.ViewHolder {
    // The views of the holder
    private View mParentView = null;
    private TextView mNameView = null;
    private TextView mScoreView = null;

    private final int sFontSize = 22;
    private final int sMinFontSize = 10;

    public ScoreViewHolder(View itemView) {
        super(itemView);
        mParentView = itemView;
        mNameView = (TextView) itemView.findViewById(R.id.score_board_name);
        mScoreView = (TextView) itemView.findViewById(R.id.score_board_score);
    }

    public void setScore(@NonNull String playerName, @NonNull Integer score) {
        mNameView.setText(playerName);
        mScoreView.setText(score.toString());
    }

    public void setFontSize(int offset) {
        int fontSize = max(sFontSize - (2 * offset), sMinFontSize);
        mNameView.setTextSize(fontSize);
        mScoreView.setTextSize(fontSize);
    }
}
