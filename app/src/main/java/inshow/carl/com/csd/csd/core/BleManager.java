package inshow.carl.com.csd.csd.core;

import android.content.Context;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import java.util.UUID;

import inshow.carl.com.csd.tools.L;

import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.bytes2HexString;

/**
 * Created by chendong on 2018/6/28.
 */

public class BleManager {

    private static BleManager mInstance;
    private BluetoothClient mClient;

    private BleManager() {
    }

    public static BleManager getInstance() {
        if (mInstance == null) {
            mInstance = new BleManager();
        }
        return mInstance;
    }

    public void initBle(Context context) {
        mClient = new BluetoothClient(context);
    }


    // Constants.STATUS_UNKNOWN
// Constants.STATUS_DEVICE_CONNECTED
// Constants.STATUS_DEVICE_CONNECTING
// Constants.STATUS_DEVICE_DISCONNECTING
// Constants.STATUS_DEVICE_DISCONNECTED

    public boolean isBluetoothOpened(){
        return mClient.isBluetoothOpened();
    }

    public void openBle(){
        mClient.openBluetooth();
    }

    public  int getBleState(String MAC){
        return mClient.getConnectStatus(MAC);
    }

    public  boolean isConnected(String MAC){
        return mClient.getConnectStatus(MAC) == Constants.STATUS_DEVICE_CONNECTED;
    }

    public void register(String MAC,BleConnectStatusListener mBleConnectStatusListener ){
        mClient.registerConnectStatusListener(MAC, mBleConnectStatusListener);
    }


    public void unRegister(String MAC,BleConnectStatusListener mBleConnectStatusListener ){
        mClient.unregisterConnectStatusListener(MAC, mBleConnectStatusListener);
    }


    public void connect(String MAC) {
        mClient.connect(MAC, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                if (code == REQUEST_SUCCESS) {

                }
            }
        });
    }

    public void disConnect(String MAC) {
        mClient.disconnect(MAC);
    }


    public void readCharacteristic(String MAC, final UUID serviceUUID, final UUID characterUUID, final IReadOnResponse readOnResponse) {
        mClient.read(MAC, serviceUUID, characterUUID, new BleReadResponse() {
            @Override
            public void onResponse(int code, byte[] data) {
                if (code == REQUEST_SUCCESS) {
                    L.d("serviceUUID:" + serviceUUID.toString() + ",characterUUID :" + characterUUID.toString()+ ",read:" + bytes2HexString(data));
                    readOnResponse.onSuccess(data);
                }else {
                    readOnResponse.onFail();
                }
            }
        });
    }


    public void writeCharacteristic(String MAC, UUID serviceUUID, final UUID characterUUID, final byte[] bytes) {
        mClient.write(MAC, serviceUUID, characterUUID, bytes, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {
                    L.d(characterUUID.toString() + ",writeCharacteristic success :"  + bytes2HexString(bytes));
                }else {
                    L.d(characterUUID.toString()  +",writeCharacteristic fail" );
                }
            }
        });
    }



    public void clear(String MAC){
        mClient.clearRequest(MAC, Constants.REQUEST_READ);
// Constants.REQUEST_READ，所有读请求
// Constants.REQUEST_WRITE，所有写请求
// Constants.REQUEST_NOTIFY，所有通知相关的请求
// Constants.REQUEST_RSSI，所有读信号强度的请求
    }


    public interface IReadOnResponse{
        void onSuccess( byte[] data);
        void onFail();
    }

}
