package Protocal;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by meishan on 16/12/1.
 */
public class SocketUtil {

    private static Map<Integer, String> msgImp = new HashMap<>();

    static {
        msgImp.put(DataProtocol.PROTOCOL_TYPE, "DataProtocol");       //0
        msgImp.put(DataAckProtocol.PROTOCOL_TYPE, "DataAckProtocol"); //1
        msgImp.put(PingProtocol.PROTOCOL_TYPE, "PingProtocol");       //2
        msgImp.put(PingAckProtocol.PROTOCOL_TYPE, "PingAckProtocol"); //3
    }

    /**
     * 解析数据内容
     *
     * @param data
     * @return
     */
    public static BasicProtocol parseContentMsg(byte[] data) {
        int protocolType = BasicProtocol.parseType(data);
        BasicProtocol basicProtocol;
        try {
            switch (protocolType){
                case DataProtocol.PROTOCOL_TYPE:
                    basicProtocol = new DataProtocol();
                    break;
                case DataAckProtocol.PROTOCOL_TYPE:
                    basicProtocol = new DataAckProtocol();
                    break;
                case PingProtocol.PROTOCOL_TYPE:
                    basicProtocol = new PingProtocol();
                    break;
                case PingAckProtocol.PROTOCOL_TYPE:
                    basicProtocol = new PingAckProtocol();
                    break;
                default:
                    basicProtocol = null;
            }
            //Log.d("Tag", basicProtocol.getClass().toString());
            basicProtocol.parseContentData(data);
        } catch (Exception e) {
            basicProtocol = null;
            e.printStackTrace();
        }
        return basicProtocol;
    }

    /**
     * 读数据
     *
     * @param inputStream
     * @return
     */
    public static BasicProtocol readFromStream(InputStream inputStream, int i, Socket socket) {
        BasicProtocol protocol;
        BufferedInputStream bis;
        byte[] header;

        //header中保存的是整个数据的长度值，4个字节表示。在下述write2Stream方法中，会先写入header
        header = new byte[BasicProtocol.LENGTH_LEN];

        try {

            bis = new BufferedInputStream(inputStream);

            int temp;
            int len = 0;
            while (len < header.length) {
                temp = bis.read(header, len, header.length - len);
                if (temp > 0) {
                    len += temp;
                } else if (temp == -1) {
                    Log.d("Tag", "isClose");
                    bis.close();
                    return null;
                }
            }

            len = 0;
            Log.d("Tag", "Header: " + Arrays.toString(header));
            //数据的长度值
            int length = byteArrayToInt(header);
            Log.d("Tag", "length:  " + length);
            byte[] content = new byte[length];
            while (len < length) {
                temp = bis.read(content, len, length - len);
                Log.d("Tag", "data:  " + Arrays.toString(content));
                if (temp > 0) {
                    len += temp;
                }
            }
            bis = null;
            bis = new BufferedInputStream(inputStream);
            byte[] bytes = new byte[8192];
            bis.read(bytes, 0, 8192);
            Log.d("Tag", "BYTES:  " + Arrays.toString(bytes));

            protocol = parseContentMsg(content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return protocol;
    }

    /**
     * 写数据
     *
     * @param protocol
     * @param outputStream
     */
    public static void write2Stream(BasicProtocol protocol, OutputStream outputStream) {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        byte[] buffData = protocol.genContentData();
        byte[] header = int2ByteArrays(buffData.length);
        try {
            bufferedOutputStream.write(header);
            bufferedOutputStream.write(buffData);
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭输入流
     *
     * @param is
     */
    public static void closeInputStream(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭输出流
     *
     * @param os
     */
    public static void closeOutputStream(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] int2ByteArrays(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static int byteArrayToInt(byte[] b) {
        int intValue = 0;
        for (int i = 0; i < b.length; i++) {
            intValue += (b[i] & 0xFF) << (8 * (3 - i)); //int占4个字节（0，1，2，3）
        }
        return intValue;
    }

    public static int byteArrayToInt(byte[] b, int byteOffset, int byteCount) {
        int intValue = 0;
        for (int i = byteOffset; i < (byteOffset + byteCount); i++) {
            intValue += (b[i] & 0xFF) << (8 * (3 - (i - byteOffset)));
        }
        return intValue;
    }

    public static int bytes2Int(byte[] b, int byteOffset) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        byteBuffer.put(b, byteOffset, 4); //占4个字节
        byteBuffer.flip();
        return byteBuffer.getInt();
    }
}