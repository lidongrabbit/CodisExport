package com.asiainfo.codis;

import codis.Conf;
import com.asiainfo.codis.client.ClientToCodis;
import com.asiainfo.codis.conf.StatisticalTablesConf;
import com.asiainfo.codis.event.EventFactory;
import com.asiainfo.codis.event.EventQueue;
import com.asiainfo.codis.event.OutputFileEvenQueueImpl;
import com.asiainfo.codis.util.OutputFileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;


import java.io.File;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class ExportData {
    private static Logger logger = Logger.getLogger(ExportData.class);

    public static void main(String[] args) throws Exception{
        String userdir = System.getProperty("user.dir") + File.separator + "conf" + File.separator;
        DOMConfigurator.configure(userdir + "log4j.xml");

        StatisticalTablesConf.init();

        String[] codisHostsInfo = Conf.getProp("codisHostsInfo").split(",");
        ForkJoinPool pool = new ForkJoinPool(Conf.getInt(Conf.CODIS_CLIENT_THREAD_COUNT, Conf.DEFAULT_CODIS_CLIENT_THREAD_COUNT));

        EventQueue<List<String>> eventQueue = new OutputFileEvenQueueImpl();
        if (Conf.getBoolean(Conf.EXPORT_FILE_ENABLE, Conf.DEFAULT_EXPORT_FILE_ENABLE)){
            pool.submit(new EventFactory(eventQueue));
        }

        long startTime=System.currentTimeMillis();

        ClientToCodis clientToCodis = new ClientToCodis(codisHostsInfo, pool, eventQueue);
        ForkJoinTask<Map<String, Map<String, Long>>> result = pool.submit(clientToCodis);

        Map<String, Map<String, Long>> finalResult = result.join();

        if (result.getException() != null){
            logger.error(result.getException());
        }

        logger.info("All tasks have been done.");

        exportData(finalResult);


        long endTime = System.currentTimeMillis();
        logger.info("Take " + (endTime - startTime) + "ms.");

    }


    public static void exportData(Map<String, Map<String, Long>> finalResult){
        logger.info("Start to export data...");
        for (Map.Entry entry : finalResult.entrySet()) {
            String filePath = entry.getKey() + StatisticalTablesConf.TABLE_FILE_TYPE;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date = new java.util.Date();

            List<String> list = new ArrayList();

            Map<String, Long> rows = (Map<String, Long>)entry.getValue();

            for (String key : rows.keySet()){
                list.add(sdf.format(date) + StatisticalTablesConf.TABLE_COLUMN_SEPARATOR + key + String.valueOf(rows.get(key)));
            }

            OutputFileUtils.exportToLocal(filePath, list);
        }
    }

}
