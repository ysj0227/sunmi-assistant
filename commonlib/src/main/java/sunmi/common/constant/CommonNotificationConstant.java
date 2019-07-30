package sunmi.common.constant;

public class CommonNotificationConstant {

    //common notice
    public static int totalEvents = 0x0001;
    public static final int netDisconnection = totalEvents++;    //网络断开
    public static final int netConnected = totalEvents++;    //网络连接
    public static final int ipcUpgrade = totalEvents++;    //ipc升级
}
