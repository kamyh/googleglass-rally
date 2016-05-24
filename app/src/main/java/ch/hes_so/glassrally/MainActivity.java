package ch.hes_so.glassrally;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
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

import ch.hes_so.glassrallylibs.bluetooth.BluetoothThread;
import ch.hes_so.glassrallylibs.bluetooth.Constants;
import ch.hes_so.glassrallylibs.command.Command;
import ch.hes_so.glassrallylibs.command.CommandEncoder;
import ch.hes_so.glassrallylibs.command.CommandFactory;

public class MainActivity extends Activity {
    private CardScrollView mCardScroller;

    private BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    private BluetoothThread mChatService = null;
    private RallyAdapter mRallyAdapter;

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 3;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

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
            if (mChatService.getState() == BluetoothThread.STATE_NONE) {
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

        // Initialize the BluetoothThread to perform bluetooth connections
        mChatService = new BluetoothThread(getApplicationContext(), mHandler);

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
        if (mChatService.getState() != BluetoothThread.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothThread to write
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
     * The Handler that gets information back from the BluetoothThread
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothThread.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mRallyAdapter.setDistanceColor(Color.GREEN);
                            break;
                        case BluetoothThread.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothThread.STATE_LISTEN:
                        case BluetoothThread.STATE_NONE:
                            mRallyAdapter.setDistanceColor(Color.RED);
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
        String msg = "cmd: " + cmd.getName() + ", param: " + cmd.getParameter();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.d(TAG, msg);

        switch (cmd.getName()) {
            case DEBUG:
                Log.d(TAG, "debug cmd: " + cmd.getParameter());
                break;

            case NEW_VECTOR: {
                String[] parameters = cmd.getParameter().split(Command.PARAMETER_DELIMITER);

                // FIXME Sometimes the Bluetooth truncate the sent string. I don't know why.
                if (parameters.length < 4) {
                    Log.wtf(TAG, "Should not happen");
                    break;
                }

                // current position
                double currentLat = Double.parseDouble(parameters[0]);
                double currentLng = Double.parseDouble(parameters[1]);

                Location currentPosition = new Location("current");
                currentPosition.setLatitude(currentLat);
                currentPosition.setLongitude(currentLng);
                mRallyAdapter.setOrigin(currentPosition);

                // target position
                double targetLat = Double.parseDouble(parameters[2]);
                double targetLng = Double.parseDouble(parameters[3]);

                Location targetPosition = new Location("target");
                targetPosition.setLatitude(targetLat);
                targetPosition.setLongitude(targetLng);
                mRallyAdapter.setDestination(targetPosition);

                break;
            }

            case REWARD: {
                String content = cmd.getParameter();
                Reward reward = new Reward(content);

                mRallyAdapter.addReward(reward);
                mCardScroller.setSelection(1); //Jump to the new reward Card

                break;
            }

            case NEW_DISTANCE: {
                try {
                    float distance = Float.parseFloat(cmd.getParameter());
                    mRallyAdapter.setDistance(distance);
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
                break;
            }
        }
    }

}

