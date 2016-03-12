package com.example.gary.googleglassrallye.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.gary.googleglassrallye.model.MovieCard;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.List;

public class MovieCardsAdapter extends CardScrollAdapter {
    private List<MovieCard> mCards;
    private Context context;

    public MovieCardsAdapter(Context context, List<MovieCard> mCards) {
        this.context = context;
        this.mCards = mCards;
    }

    @Override
    public int getPosition(Object item) {
        return mCards.indexOf(item);
    }

    @Override
    public int getCount() {
        return mCards.size();
    }

    @Override
    public Object getItem(int position) {
        return mCards.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Card card = new Card(context);

        MovieCard mc = mCards.get(position);

        // Card text
        if (mc.getText() != null)
            card.setText(mc.getText());

        // Card footer note
        if (mc.getFooterText() != null)
            card.setFootnote(mc.getFooterText());

        // Set image layout
        if (mc.getImgLayout() != null)
            card.setImageLayout(mc.getImgLayout());

        // loop and set card images
        for (int img : mc.getImages()) {
            card.addImage(img);
        }

        return card.getView();
    }


}