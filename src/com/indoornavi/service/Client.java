package com.indoornavi.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;
import com.indoornavi.App;

import java.util.Observable;

public class Client extends Observable {
    private static final String TAG = App.TAG + " Client";

    /**
     * Messenger for communicating with service.
     */
    Messenger service;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger clientMessenger = new Messenger(new IncomingHandler());

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConversationConst.MSG_GOT_WIFI_RSSI:
                    onWifiScanReady(msg);
                    break;
                case ConversationConst.MSG_POSITION_CHANGED:
                    onPositionChanged(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void onWifiScanReady(Message msg) {
        Bundle data = msg.getData();
        data.setClassLoader(App.appContext.getClassLoader());
        WifiScanResult scanResult = (WifiScanResult) data.getParcelable(ConversationConst.MSG_PARAM_WIFI_RSSI);
        Log.d(TAG, "Received data from " + scanResult.data.size() + " access points. Notifying " + countObservers() + " observers");
        setChanged();
        notifyObservers(scanResult);
    }

    private void onPositionChanged(Message msg) {
        int position = msg.arg1;
        Log.d(TAG, "Received position " + position + ". Notifying " + countObservers() + " observers");
        setChanged();
        notifyObservers(position);
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            Client.this.service = new Messenger(service);

            try {
                Log.d(TAG, "requesting for register client");
                Message msg = Message.obtain(null, ConversationConst.MSG_REGISTER_CLIENT);
                msg.replyTo = clientMessenger;
                Client.this.service.send(msg);
            } catch (RemoteException e) {
                Log.d(TAG, "Exception", e);
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            service = null;
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    public void bindService(Context context) {
        Log.d(TAG, "binding client service");
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        Intent intent = new Intent(context, PositioningService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Context context) {
        Log.d(TAG, "unbinding client service");
        if (service != null) {
            try {
                Message msg = Message.obtain(null, ConversationConst.MSG_UNREGISTER_CLIENT);
                msg.replyTo = clientMessenger;
                service.send(msg);
            } catch (RemoteException e) {
                Log.d(TAG, "Exception", e);
                // There is nothing special we need to do if the service
                // has crashed.
            }
        }

        // Detach our existing connection.
        context.unbindService(serviceConnection);
    }
}
