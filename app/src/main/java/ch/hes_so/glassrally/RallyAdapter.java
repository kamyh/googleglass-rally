package ch.hes_so.glassrally;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.List;

import ch.hes_so.glassrally.compass.CompassView;

public class RallyAdapter extends CardScrollAdapter {
    private List<Reward> mRewards;
    private Context mContext;

    private CompassView mCompassView;

    public RallyAdapter(Context context, List<Reward> rewards) {
        mContext = context;
        mRewards = rewards;
        mCompassView = new CompassView(context);
    }

    @Override
    public int getCount() {
        return mRewards.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        if (i == 0)
            return null; //TODO return something ?
        else
            return mRewards.get(i - 1);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (i > 0) {
            Reward reward = mRewards.get(i - 1);

            View rewardView = new CardBuilder(mContext, CardBuilder.Layout.EMBED_INSIDE)
                    .setEmbeddedLayout(R.layout.reward_layout)
                    .getView();

            WebView webView = (WebView) rewardView.findViewById(R.id.webview);

            //For scaling the content but doesn't work
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.setInitialScale(1);
            webView.setVerticalScrollBarEnabled(false);
            webView.setHorizontalScrollBarEnabled(false);

            //Load the content
            webView.loadUrl(reward.getContent());

            return rewardView;
        } else {
            return mCompassView.getCompassView();
        }
    }

    @Override
    public int getPosition(Object o) {
        if (o instanceof Reward)
            return mRewards.indexOf(o);
        else
            return 0; //TODO maybe check more ?
    }

    public void addReward(Reward reward) {
        mRewards.add(0, reward);
        notifyDataSetChanged();
    }

    public void resetReward() {
        mRewards.clear();
        notifyDataSetChanged();
    }

    public void setOrigin(Location origin) {
        mCompassView.setOrigin(origin);
    }

    public void setDestination(Location destination) {
        mCompassView.setDestination(destination);
    }

    public void setDistance(float distance) {
        this.mCompassView.setDistance(distance);
    }

    public void setDistanceColor(int color){
        this.mCompassView.setDistanceColor(color);
    }
}
