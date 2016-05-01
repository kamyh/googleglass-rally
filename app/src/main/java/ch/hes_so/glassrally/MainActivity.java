package ch.hes_so.glassrally;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ch.hes_so.glassrally.compass.LatLng;
import ch.hes_so.glassrallylibs.bluetooth.BluetoothChatService;
import ch.hes_so.glassrallylibs.bluetooth.Constants;
import ch.hes_so.glassrallylibs.command.Command;
import ch.hes_so.glassrallylibs.command.CommandEncoder;
import ch.hes_so.glassrallylibs.command.CommandFactory;

public class MainActivity extends Activity {
    private CardScrollView mCardScroller;

    private View mView;
    private BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    private BluetoothChatService mChatService = null;
    private RallyAdapter mRallyAdapter;

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 3;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mView = buildView();

        mCardScroller = new CardScrollView(this);

        List<Reward> rewards = new LinkedList<>();

        mRallyAdapter = new RallyAdapter(this, rewards);
        mCardScroller.setAdapter(mRallyAdapter);
        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // Plays disallowed sound to indicate that TAP actions are not supported.
//                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//                am.playSoundEffect(Sounds.DISALLOWED);
                sendMessage(new Date().toString());
            }
        });
        setContentView(mCardScroller);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    /**
     * Builds a Glass styled "Hello World!" view using the {@link CardBuilder} class.
     */
    private View buildView() {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);

        card.setText(R.string.hello_world);
        return card.getView();
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getApplicationContext(), mHandler);

//        // Initialize the buffer for outgoing messages
//        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            Command cmd = CommandFactory.createDebugCommand(message);
            mChatService.write(cmd);

//            // Reset out string buffer to zero and clear the edit text field
//            mOutStringBuffer.setLength(0);
//            mEtOutText.setText(mOutStringBuffer);
            Log.d(TAG, "Glass has sent: " + message);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        Log.d(TAG, "status: " + subTitle.toString());
    }

    private void setStatus(int resId) {
        setStatus(getString(resId));
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    // this will be executed after the message has been sent
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    // construct a string from the valid bytes in the buffer
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Command cmd = CommandEncoder.fromStream(readMessage);
                    onCommandReceived(cmd);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void onCommandReceived(Command cmd) {
        //                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

        String msg = "cmd: " + cmd.getName() + ", param: " + cmd.getParameter();
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

        int test = 0; //TODO replace by cmd name
        switch (test) {
            case 0:
                //Connected
                //TODO Toast or something else ?

                break;
            case 1:
                //Game Over
                //TODO Game Over screen

                break;
            case 2: {
                //Position update
                //TODO parse lat long
                double lat = 0;
                double lng = 0;

                LatLng position = new LatLng(lat, lng);
                mRallyAdapter.setOrigin(position);

                break;
            }
            case 3: {
                //Next Checkpoint
                //TODO parse lat long
                double lat = 0;
                double lng = 0;

                LatLng destination = new LatLng(lat, lng);
                mRallyAdapter.setOrigin(destination);

                break;
            }
            case 4:
                //Reward

                //TODO parse Reward
                Reward reward = new Reward("reward name", "http://vignette3.wikia.nocookie.net/ssb/images/2/2b/Lol-face.gif");

                mRallyAdapter.addReward(reward);
                mCardScroller.setSelection(1); //Jump to the new reward Card
                break;

        }
    }

}
