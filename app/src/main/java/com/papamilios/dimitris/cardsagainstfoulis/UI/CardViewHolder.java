package com.papamilios.dimitris.cardsagainstfoulis.UI;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.graphics.Color;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CheckedTextView;

import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

class CardViewHolder extends RecyclerView.ViewHolder {
    private final CheckedTextView mCardItemView;
    private View mParentItem = null;
    private Card mCard;
    private boolean mSelected = false;
    private OnItemSelectedListener mListener;
    private boolean mSelectionEnabled = true;

    public CardViewHolder(View itemView, OnItemSelectedListener listener) {
        super(itemView);
        mParentItem = itemView;
        mListener = listener;
        mCardItemView = (CheckedTextView) itemView.findViewById(R.id.textView);

        mCardItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Don't do anything if we're not allowed to click and select
                if (!mSelectionEnabled) {
                    return;
                }

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
            mParentItem.setBackground(ResourcesCompat.getDrawable(mParentItem.getResources(), R.drawable.white_card_selected_back, null));
            mCardItemView.setTextColor(Color.WHITE);
        } else {
            mParentItem.setBackground(ResourcesCompat.getDrawable(mParentItem.getResources(), R.drawable.white_card_back, null));
            mCardItemView.setTextColor(Color.BLACK);
        }
        mCardItemView.setChecked(value);
        mSelected = value;
    }

    // Enable/disable selection by clicking
    public void enableSelection(boolean enable) {
        mSelectionEnabled = enable;
    }

    // A listener interface to listen to selection events
    public interface OnItemSelectedListener {

        public void onSelectionChanged(Card card);
    }
}
