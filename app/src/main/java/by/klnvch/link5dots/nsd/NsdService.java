package by.klnvch.link5dots.nsd;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.net.Socket;

import by.klnvch.link5dots.R;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NsdService extends Service implements
        NsdManager.RegistrationListener, NsdManager.DiscoveryListener, NsdManager.ResolveListener{

    private static final String TAG = "NsdService";

    public static final String BLUETOOTH_GAME_VIEW_PREFERENCES = "BLUETOOTH_GAME_VIEW_PREFERENCES";

    // Member fields
    private Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // new constants and fields
    private static final String SERVICE_NAME = "Link Five Dots";
    public static final String SERVICE_TYPE = "_http._tcp.";

    private String mServiceName = SERVICE_NAME;
    private NsdManager mNsdManager;
    private int mPort = -1;
    private NsdServiceInfo mService;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        NsdService getService() {
            return NsdService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mNsdManager = (NsdManager)getSystemService(Context.NSD_SERVICE);
        //
        mState = STATE_NONE;
        start();
    }

    @Override
    public void onDestroy() {
        stop();
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }
    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        if(mHandler != null) {
            mHandler.obtainMessage(NsdActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        }
    }
    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }
    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(this);
            mSecureAcceptThread.start();
        }
    }

    public synchronized void connect(NsdServiceInfo serviceInfo) {

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING){
            if (mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(this, serviceInfo);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    synchronized void connected(Socket socket) {

        // Cancel the thread that completed the connection
        if (mConnectThread != null){
            //mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(this, socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        mHandler.obtainMessage(NsdPickerActivity.MESSAGE_DEVICE_NAME).sendToTarget();

        setState(STATE_CONNECTED);

        // clean preferences
        SharedPreferences prefs = getSharedPreferences(BLUETOOTH_GAME_VIEW_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public String getDeviceName(){
        if(mService != null){
            return mService.getServiceName();
        }else{
            return "";
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }
    /**
     * Write to the ConnectedThread in an not synchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write not synchronized
        r.write(out);
    }
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    public void connectionFailed(NsdServiceInfo device) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(NsdActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putInt(NsdPickerActivity.TOAST, R.string.bluetooth_connecting_error_message);
        bundle.putString(NsdPickerActivity.DEVICE_NAME, mService.getServiceName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        NsdService.this.start();
    }
    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    public void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(NsdActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putInt(NsdActivity.TOAST, R.string.bluetooth_disconnected);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        NsdService.this.start();
    }

    public void resetConnectThread(){
        mConnectedThread = null;
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj){
        mHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
    }

    /// new overrides

    @Override
    public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
        Log.d(TAG, "onRegistrationFailed");
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
        Log.d(TAG, "onUnregistrationFailed");
    }

    @Override
    public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
        mServiceName = nsdServiceInfo.getServiceName();
        Log.d(TAG, "onServiceRegistered");
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
        Log.d(TAG, "onServiceUnregistered");
    }

    //////////////////////////////////////////////////////

    @Override
    public void onStartDiscoveryFailed(String s, int i) {
        Log.e(TAG, "Discovery failed: Error code:" + i);
        mNsdManager.stopServiceDiscovery(this);
    }

    @Override
    public void onStopDiscoveryFailed(String s, int i) {
        Log.e(TAG, "Discovery failed: Error code:" + i);
        mNsdManager.stopServiceDiscovery(this);
    }

    @Override
    public void onDiscoveryStarted(String s) {
        Log.d(TAG, "onDiscoveryStarted");
    }

    @Override
    public void onDiscoveryStopped(String s) {
        Log.i(TAG, "Discovery stopped: " + s);
    }

    @Override
    public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
        Log.d(TAG, "Service discovery success" + nsdServiceInfo);
        if (!nsdServiceInfo.getServiceType().equals(SERVICE_TYPE)) {
            Log.d(TAG, "Unknown Service Type: " + nsdServiceInfo.getServiceType());
        } else if (nsdServiceInfo.getServiceName().equals(mServiceName)) {
            Log.d(TAG, "Same machine: " + mServiceName);
        } else if (nsdServiceInfo.getServiceName().contains(mServiceName)){
            mNsdManager.resolveService(nsdServiceInfo, this);
        }
    }

    @Override
    public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
        Log.e(TAG, "service lost" + nsdServiceInfo);
        if (mService == nsdServiceInfo) {
            nsdServiceInfo = null;
        }
    }

    ////////////////////////////////////////////////////////////////

    @Override
    public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
        Log.e(TAG, "Resolve failed" + i);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
        Log.e(TAG, "Resolve Succeeded. " + nsdServiceInfo);

        if (nsdServiceInfo.getServiceName().equals(mServiceName)) {
            Log.d(TAG, "Same IP.");
            return;
        }
        mService = nsdServiceInfo;
    }

    // new functions

    public void setLocalPort(int port) {
        this.mPort = port;
    }

    public void registerService() {
        if (mPort > -1) {
            NsdServiceInfo serviceInfo = new NsdServiceInfo();
            serviceInfo.setPort(mPort);
            serviceInfo.setServiceName(mServiceName);
            serviceInfo.setServiceType(SERVICE_TYPE);

            mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, this);
        } else {
            Log.i(TAG, "ServerSocket isn't bound");
        }
    }

    public void unRegisterService() {
        mNsdManager.unregisterService(this);
    }

    public void discoverServices() {
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, this);
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(this);
    }
}
