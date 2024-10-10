package tw.decade.extension;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpSocketX extends Thread {
    public static final String TAG = "[tw.decade][UDP]";
    public static final String RECEIVE_ACTION = "GetUDPReceive";
    public static final String RECEIVE_STRING = "ReceiveString";
    public static final String RECEIVE_BYTES = "ReceiveBytes";

    static int httpServerPort_UDP = 16888;
    private String ServerIp;
    private String CallBack_Ip="";
    private String CallBack_Ip_lasttime="";
    private boolean isOpen;
    private static DatagramSocket ds = null;
    private Context context;

    public void changeServerStatus(boolean isOpen) {
        this.isOpen = isOpen;
        if (!isOpen) {
            ds.close();
            Log.e(TAG, "UDP-Server closed");
        }
    }
    public void setPort(int port){
        this.httpServerPort_UDP = port;
    }

     public UdpSocketX(Context context) {
        this.context = context;
        this.ServerIp = "0.0.0.0";
        this.isOpen = true;
        httpServerPort_UDP = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("remoteAccessPort_UDP", "16888"));

         Log.d(TAG, "[Init][UDP][UdpSocketX][]" + ServerIp + ":" + httpServerPort_UDP);

    }
    String[] ip_4_digi=getIPAddress(true).split("\\.");
    String ip_classC_bordcasting=ip_4_digi[0]+"."+ip_4_digi[1]+"."+ip_4_digi[2]+".255";
    public void bordcasting(String string) throws IOException {

        unicasting(string,ip_classC_bordcasting, httpServerPort_UDP);
    }
    public void callback_casting(String string) throws IOException {
        if(this.CallBack_Ip.isEmpty())
            bordcasting(string);
        else
            unicasting(string,this.CallBack_Ip, httpServerPort_UDP);
    }

    public void unicasting(String string, String remoteIp, int remotePort) throws IOException {
        if(!this.CallBack_Ip_lasttime.equals(remoteIp)){
            Log.d(TAG, "[O][UDP]Changing target UDP Client IP：" + remoteIp + ":" + remotePort);
            this.CallBack_Ip_lasttime=remoteIp;
        }

        InetAddress inetAddress = InetAddress.getByName(remoteIp);
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            DatagramPacket dpSend = new DatagramPacket(string.getBytes(), string.getBytes().length, inetAddress, remotePort);
            datagramSocket.send(dpSend);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
//    public static String getLocalIP(Context context) {
//        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
//        assert wifiManager != null;
//        WifiInfo info = wifiManager.getConnectionInfo();
//        int ipAddress = info.getIpAddress();
//        return String.format("%d.%d.%d.%d"
//                , ipAddress & 0xff
//                , ipAddress >> 8 & 0xff
//                , ipAddress >> 16 & 0xff
//                , ipAddress >> 24 & 0xff);
//    }
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }
    @Override
    public void run() {
        InetSocketAddress inetSocketAddress = new InetSocketAddress( httpServerPort_UDP);
        try {
            ds = new DatagramSocket(inetSocketAddress);
            Log.e(TAG, "[O][UDP-Server] started");
        } catch (SocketException e) {
            Log.e(TAG, "[X][UDP] fail: " + e.getMessage());
            e.printStackTrace();
        }
        byte[] msgRcv = new byte[1024];
        DatagramPacket dpRcv = new DatagramPacket(msgRcv, msgRcv.length);
        Log.e(TAG, "UDP-Server Listening port:"+ httpServerPort_UDP+" ...(default send broadcasting x.x.x.255; otherwise send hi to phone IP port:16888, then udp will send json to last one who calls ip address.)");
        while (isOpen) {
            try {
                ds.receive(dpRcv);
                this.CallBack_Ip=dpRcv.getAddress().toString().replace("/","");
                String string = new String(dpRcv.getData(), dpRcv.getOffset(), dpRcv.getLength());
                Log.d(TAG, "UDP-Server rx： " + string);

                Intent intent = new Intent();
                intent.setAction(RECEIVE_ACTION);
                intent.putExtra(RECEIVE_STRING,string);
                intent.putExtra(RECEIVE_BYTES, dpRcv.getData());
                context.sendBroadcast(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

