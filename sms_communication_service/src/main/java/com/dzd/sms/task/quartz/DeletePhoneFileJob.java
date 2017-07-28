package com.dzd.sms.task.quartz;

import com.dzd.utils.LogUtil;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;

/**
 * Created by Administrator on 2017/7/7.
 */
@DisallowConcurrentExecution
public class DeletePhoneFileJob   implements Job {
    private static LogUtil logger = LogUtil.getLogger(DeletePhoneFileJob.class);

    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        try {
            delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        String catalinaHome = System.getProperty("catalina.home");
        String txtPath = catalinaHome + "/fileUpload/";
        File file = new File(txtPath);
        String[] tempList = file.list();

        if (tempList != null && tempList.length != 0) {
            File temp = null;
            for (int i = 0; i < tempList.length; i++) {
                if (txtPath.endsWith(File.separator)) {
                    temp = new File(txtPath + tempList[i]);
                } else {
                    temp = new File(txtPath + File.separator + tempList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
            }
            logger.info("删除文件个数：" + tempList.length);
        }

    }

}
