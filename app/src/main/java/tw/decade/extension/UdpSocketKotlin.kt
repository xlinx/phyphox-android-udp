//package tw.decade.extension
//
//import android.os.Handler
//import android.util.Log
//import java.io.IOException
//import java.net.DatagramPacket
//import java.net.DatagramSocket
//import java.net.InetAddress
//import java.net.SocketException
//import java.net.UnknownHostException
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class UdpSocketKotlin(private val handler: Handler) {
//    private val TAG = "UdpSocketKotlin"
//
//    private var mThreadPool: ExecutorService? = null
//    private var socket: DatagramSocket? = null
//    private var receivePacket: DatagramPacket? = null
//    private val BUFFER_LENGTH = 1024
//    private val receiveByte = ByteArray(BUFFER_LENGTH)
//
//    private var isThreadRunning = false
//    private lateinit var clientThread: Thread
//
//    init {
//
//        val cpuNumbers = Runtime.getRuntime().availableProcessors()
//        mThreadPool = Executors.newFixedThreadPool(cpuNumbers * 5)
//    }
//    fun startUDPSocket() {
//        if (socket != null) return
//        try {
//            socket = DatagramSocket(8888)
//            if (receivePacket == null)
//
//                receivePacket = DatagramPacket(receiveByte, BUFFER_LENGTH)
//            startSocketThread()
//        } catch (e: SocketException) {
//            e.printStackTrace()
//        }
//    }
//    private fun startSocketThread() {
//        clientThread = Thread(Runnable {
//            Log.d(TAG, "clientThread is running...")
//            receiveMessage()
//        })
//        isThreadRunning = true
//        clientThread.start()
//    }
//    private fun receiveMessage() {
//        while (isThreadRunning) {
//            try {
//                socket?.receive(receivePacket)
//                if (receivePacket == null || receivePacket?.length == 0)
//                    continue
//                mThreadPool?.execute {
//                    val strReceive = String(receivePacket!!.data, receivePacket!!.offset, receivePacket!!.length)
//                    Log.d(TAG, strReceive + " from " + receivePacket!!.address.hostAddress + ":" + receivePacket!!.port)
//
//                    handler.sendMessage(handler.obtainMessage(1,strReceive))
//                    receivePacket?.length = BUFFER_LENGTH
//                }
//            } catch (e: IOException) {
//                stopUDPSocket()
//                e.printStackTrace()
//                return
//            }
//        }
//    }
//
//    fun sendMessage(message: String) {
//        mThreadPool?.execute {
//            try {
//                val targetAddress = InetAddress.getByName("192.168.0.255")
//                val packet = DatagramPacket(message.toByteArray(), message.length, targetAddress, 8080)
//                socket?.send(packet)
//            } catch (e: UnknownHostException) {
//                e.printStackTrace()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    fun stopUDPSocket() {
//        isThreadRunning = false
//        receivePacket = null
//        clientThread.interrupt()
//        if (socket != null) {
//            socket?.close()
//            socket = null
//        }
//    }
//
//}
