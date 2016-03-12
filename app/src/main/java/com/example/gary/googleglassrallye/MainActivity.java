package com.example.gary.googleglassrallye;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.gary.googleglassrallye.adapter.MovieCardsAdapter;
import com.example.gary.googleglassrallye.model.MovieCard;
import com.google.android.glass.app.Card.ImageLayout;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p/>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class MainActivity extends Activity {
    private GestureDetector mGestureDetector;
    private List<MovieCard> mCards;
    private CardScrollView mCardScrollView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);

        mContext = this;

        mGestureDetector = createGestureDetector(this);


        prepareMovieCards();

        mCardScrollView = new CardScrollView(this);
        MovieCardsAdapter adapter = new MovieCardsAdapter(mContext, mCards);
        mCardScrollView.setAdapter(adapter);
        mCardScrollView.activate();
        setContentView(mCardScrollView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScrollView.activate();
    }

    @Override
    protected void onPause() {
        mCardScrollView.deactivate();
        super.onPause();
    }

    /**
     * Create a menu for the current Card view inflate from the menu.xml file
     *
     * @param featureId
     * @param menu
     * @return
     */
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    /**
     * show menu items for the CardViewScroller
     * this menu is shared between all card in the CardViewScoller (but I think
     * it's possible the get the card position to customize the menu)
     *
     * @param featureId
     * @param item
     * @return
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId == Window.FEATURE_OPTIONS_PANEL) {
            switch (item.getItemId()) {
                case R.id.find_android:
                    findDevelopers("Android");
                    break;
                case R.id.find_javascript:
                    findDevelopers("Java Script");
                    break;
                case R.id.find_ios:
                    findDevelopers("iOS");
                    break;
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);

        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    openOptionsMenu();
                    return true;
                } else if (gesture == Gesture.TWO_TAP) {
                    // do something on two finger tap
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) {
                    // do something on right (forward) swipe
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    // do something on left (backwards) swipe
                    return true;
                } else if (gesture == Gesture.SWIPE_DOWN) {
                    finish();
                }
                return false;
            }
        });

        gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
            @Override
            public void onFingerCountChanged(int previousCount, int currentCount) {
                // do something on finger count changes
            }
        });

        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
            @Override
            public boolean onScroll(float displacement, float delta, float velocity) {
                // do something on scrolling
                return true;
            }
        });

        return gestureDetector;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

    /**
     * Here the card are created at runtime. I might be possible to load them from a local database
     * Note: Some lines of this snippet are deprecated. At the moment, you should use
     * the CardBuilder class to create a Card.
     * See: https://developers.google.com/glass/develop/gdk/card-design
     */
    private void prepareMovieCards() {
        mCards = new ArrayList<MovieCard>();

        // Card with no background image
        MovieCard mc = new MovieCard("I don't know. But who cares! Ha ha!",
                "Wait! What does that mean?", ImageLayout.FULL, new int[]{});
        mCards.add(mc);

        // Card with full background image
        mc = new MovieCard("I wanna go home. Does anyone know where my dad is?",
                "Pet store?", ImageLayout.FULL,
                new int[]{R.drawable.card_full});
        mCards.add(mc);

        // Card with full background of 3 images
        mc = new MovieCard("Dude? Dude? Focus dude... Dude?",
                "Oh, he lives. Hey, dude!", ImageLayout.FULL, new int[]{
                R.drawable.card_bottom_left,
                R.drawable.card_bottom_right, R.drawable.card_top});
        mCards.add(mc);

        // Card with left aligned images
        mc = new MovieCard("Just keep swimming.",
                "I'm sorry, Dory. But I... do", ImageLayout.LEFT, new int[]{
                R.drawable.card_bottom_left,
                R.drawable.card_bottom_right, R.drawable.card_top});
        mCards.add(mc);

    }

    private void findDevelopers(String platform) {
        Toast.makeText(getApplicationContext(), "Platform: " + platform, Toast.LENGTH_LONG).show();
    }

}
