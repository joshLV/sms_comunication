package test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
 
    public static String test1() {
		try {
//            PostMethod postMethod = new PostMethod("http://192.168.1.43:8899/sms_communication_web/v4/sms/send.do");
            PostMethod postMethod = new PostMethod("http://192.168.1.52:60009/v4/sms/send.do");
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
            String timeStamp = format.format(new Date());
            String uid = "222";
            String key = "133f7aae9be3778bcd06c96c1c73aa85";
            postMethod.addParameter("uid", uid);
            postMethod.addParameter("timestamp", timeStamp);
            postMethod.addParameter("sign", getSign(uid,key,timeStamp));
            postMethod.addParameter("mobile", "18617052312");
            postMethod.addParameter("text", "【CCCC】8888888888888888888888888退订回T");
            HttpClient httpClient = new HttpClient();
            httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            try {
                httpClient.executeMethod(postMethod);
                return (postMethod.getResponseBodyAsString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "error";
	}

	@SuppressWarnings("resource")
	static StringBuffer getPhones() {
		StringBuffer strbf = new StringBuffer();
		boolean flag = true;
		String encoding = "UTF-8";
		File file = new File("E:/data/ceshi.txt");
		if (file.isFile() && file.exists()) { // 判断文件是否存在
			InputStreamReader read = null;
			try {
				read = new InputStreamReader(new FileInputStream(file),
						encoding);
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (flag) {
						strbf.append(lineTxt);
						flag = false;
					} else {
						strbf.append(",");
						strbf.append(lineTxt);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if(read!=null){
						read.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return strbf;
	}

    public static String string2MD5(String inStr){
        MessageDigest md5 = null;
        try{
            md5 = MessageDigest.getInstance("MD5");
        }catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++){
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString(); 
    }
    private static String getSign(String account,String key,String timeStamp){
        return string2MD5(account+key+timeStamp);
    }
}

