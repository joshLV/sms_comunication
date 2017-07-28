package test;

import com.dzd.base.util.DateUtil;
import com.dzd.phonebook.util.RandomUtil;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/6/7.
 */
public class TestQW {


	/*public static void main(String[] str) {
		long startTime = new Date().getTime();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss：SSS");
		System.out.println("线程准备运行" + df.format(new Date()));
		for (int i = 0; i < 100; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						for (int x = 0; x < 100000; x++) {
							long startRequestTime = new Date().getTime();
							String result = Message.test1();
							long endRequestTime = new Date().getTime();
							System.out.println(" [" + (endRequestTime - startRequestTime) + "] " + result);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		System.out.println("diff minseconds=" + (new Date().getTime() - startTime) + "线程运行结束" + df.format(new Date()));
	}*/

	/* @Test
	public void test3() throws Exception {
		 String orderId =  "asdf" + getCurrentTimeMillis();//2017 07 21 19 33 12
		 System.out.println(orderId);


	}*/


}
