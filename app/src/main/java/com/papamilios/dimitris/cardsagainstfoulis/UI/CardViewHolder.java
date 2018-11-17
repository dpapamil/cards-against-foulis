package com.papamilios.dimitris.cardsagainstfoulis.UI;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;

import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

class CardViewHolder extends RecyclerView.ViewHolder {
    private final CheckedTextView mCardItemView;
    private Card mCard;
    private boolean mSelected = false;
    private OnItemSelectedListener mListener;

    public CardViewHolder(View itemView, OnItemSelectedListener listener) {
        super(itemView);
        mListener = listener;
        mCardItemView = (CheckedTextView) itemView.findViewById(R.id.textView);

        mCardItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChecked(!mSelected);
                mListener.onSelectionChanged(mCard);

            }
        });

    }

    public CheckedTextView cardTextView() {
        return mCardItemView;
    }

    public void setCard(Card card) {
        mCard = card;
        cardTextView().setText(card.getText());
    }

    public void setChecked(boolean value) {
        if (value) {
            mCardItemView.setBackgroundColor(Color.LTGRAY);
        } else {
            mCardItemView.setBackgroundColor(Color.WHITE);
        }
        mCardItemView.setChecked(value);
        mSelected = value;
    }

    // A listener interface to listen to selection events
    public interface OnItemSelectedListener {

        public void onSelectionChanged(Card card);
    }
}
