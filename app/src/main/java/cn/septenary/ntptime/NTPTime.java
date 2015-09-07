package cn.septenary.ntptime;

import android.util.Log;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Septenary on 15-9-7.
 */
public class NTPTime {

    private final List<String> sHosts;

    private long mLocalTimeOffset;

    private static final String TAG = "NTPTime";

    {
        sHosts = new ArrayList<>(17);
        sHosts.add("ntp.sjtu.edu.cn");// 202.120.2.101 (上海交通大学网络中心NTP服务器地址）
        sHosts.add("s1a.time.edu.cn");// 北京邮电大学
        sHosts.add("s1b.time.edu.cn");// 清华大学
        sHosts.add("s1c.time.edu.cn");// 北京大学
        sHosts.add("s1d.time.edu.cn");// 东南大学
        sHosts.add("s1e.time.edu.cn");// 清华大学
        sHosts.add("s2a.time.edu.cn");// 清华大学
        sHosts.add("s2b.time.edu.cn");// 清华大学
        sHosts.add("s2c.time.edu.cn");// 北京邮电大学
        sHosts.add("s2d.time.edu.cn");// 西南地区网络中心
        sHosts.add("s2e.time.edu.cn");// 西北地区网络中心
        sHosts.add("s2f.time.edu.cn");// 东北地区网络中心
        sHosts.add("s2g.time.edu.cn");// 华东南地区网络中心
        sHosts.add("s2h.time.edu.cn");// 四川大学网络管理中心
        sHosts.add("s2j.time.edu.cn");// 大连理工大学网络中心
        sHosts.add("s2k.time.edu.cn");// CERNET桂林主节点
        sHosts.add("s2m.time.edu.cn");// 北京大学
    }

    private static class SingletonHolder {
        private static NTPTime SINGLETON = new NTPTime();

    }

    private NTPTime() {
        //no instance
    }

    public static NTPTime getInstance() {
        return SingletonHolder.SINGLETON;
    }

    public void updteTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "start update time...  ");
                //                Collections.shuffle(sHosts);
                //                for (String host : sHosts) {
                //                    Log.e(TAG, "start...  " + host);
                //                    boolean ret = requestNTPTime(host);
                //                    if (ret) return;
                //                }
                requestNTPTime2(sHosts);
            }
        }).start();
    }

    private boolean requestNTPTime2(List<String> hosts) {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(5000);
        try {
            client.open();
            for (String host : hosts) {
                try {
                    InetAddress hostAddr = InetAddress.getByName(host);
                    // Log.e(TAG, "> " + hostAddr.getHostName() + "/" + hostAddr.getHostAddress());
                    TimeInfo info = client.getTime(hostAddr);
                    info.computeDetails();
                    mLocalTimeOffset = info.getOffset();
                    Log.e(TAG, "ok  " + host + "  " + mLocalTimeOffset);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "failed  " + host);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
        return false;
    }

    private boolean requestNTPTime(String host) {
        NTPUDPClient client = new NTPUDPClient();
        try {
            client.setSoTimeout(5000);
            client.open();
            try {
                InetAddress address = InetAddress.getByName(host);
                TimeInfo info = client.getTime(address);
                info.computeDetails();
                long delay = info.getDelay();
                mLocalTimeOffset = info.getOffset();
                Log.e(TAG, "ok  " + host + "   " + mLocalTimeOffset);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SocketException e) {
            Log.e(TAG, "failed  " + host);
            e.printStackTrace();
        } finally {
            client.close();
        }
        return false;
    }

    public long getCurrentTime() {
        return mLocalTimeOffset + System.currentTimeMillis();
    }

}
