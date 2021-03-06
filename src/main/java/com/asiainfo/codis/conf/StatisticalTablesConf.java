package com.asiainfo.codis.conf;

import codis.Conf;
import com.asiainfo.codis.ExportData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class StatisticalTablesConf {
    private static Logger logger = Logger.getLogger(StatisticalTablesConf.class);
    private static String TABLES_CONFIG = "tables.json";
    private static Properties properties = new Properties();
    private static Map<String, String> allTables = new HashMap<>();
    public static boolean isAllDone = false;

    private static Map<String, CodisTable> allTablesSchema;

    public static String TABLE_COLUMN_SEPARATOR = Conf.getProp().getProperty(Conf.OUTPUT_FILE_SEPARATOR, Conf.DEFAULT_OUTPUT_FILE_SEPARATOR);

    public final static String TABLE_IGNORE_HEADER_FLAG = "#";
    public final static String CODIS_KEY_PREFIX = "codis_key_prefix";
    public final static String DEFAULT_CODIS_KEY_PREFIX = "siteposition";
    public final static String TABLE_FILE_TYPE = ".txt";
    public final static String EMPTY_VALUE = "#NA#";

    public static String getColumnslHeader(String tableName){
        return properties.getProperty(tableName, "");
    }


    public static Map<String, CodisTable> getAllTablesSchema() {
        return allTablesSchema;
    }

    public static Map<String, String> getTables(){
        return allTables;
    }

    public static void init(){
        try {
            String confDir = Paths.get(ExportData.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent().getParent() + File.separator + "conf" + File.separator;
            Gson gson = new GsonBuilder().create();

            allTablesSchema = gson.fromJson(FileUtils.readFileToString(new File(confDir + TABLES_CONFIG)), new TypeToken<Map<String, CodisTable>>() {
            }.getType());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
