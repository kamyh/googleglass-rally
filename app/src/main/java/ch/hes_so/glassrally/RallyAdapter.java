package ch.hes_so.glassrally;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.List;

public class RallyAdapter extends CardScrollAdapter
{
    private List<Reward> mRewards;
    private Context mContext;

    public RallyAdapter(Context context, List<Reward> rewards) {
        mContext = context;
        mRewards = rewards;
    }

    @Override
    public int getCount() {
        return mRewards.size(); // + 1;  //TODO
    }

    @Override
    public Object getItem(int i) {
        if(i == 0)
            return null; //TODO return Arrow
        else
            return mRewards.get(i /*- 1*/);  //TODO
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //Card card = new Card(context);

        /*if(i > 0)*/ { //TODO

            Reward reward = mRewards.get(i); // mRewards.get(i-1);

            View rewardView = new CardBuilder(mContext, CardBuilder.Layout.EMBED_INSIDE)
                    .setEmbeddedLayout(R.layout.reward_layout)
                    .setFootnote(reward.getName())
                    .getView();

            WebView webView = (WebView) rewardView.findViewById(R.id.webview);
            //webView.loadData(reward.getContent(), "text/html", "utf-8");
            webView.loadUrl(reward.getContent());
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            return rewardView;
        }

        //return null;
    }

    @Override
    public int getPosition(Object o) {

        //TODO check if Arrow
        return mRewards.indexOf(o);
    }
}
