package org.example;
import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
public class LogTranceDB {

    public static void main(String[] args) {

        // TODO Auto-generated method stub

        DBAccess dbAccess = new DBAccess();

        List<LinkedHashMap<String, Object>> dataList = new ArrayList<LinkedHashMap<String, Object>>();
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

        File dir = new File("d:/logfile/");
        File files[] = dir.listFiles();
        if (files.length != 0) {
            for (File file : files) {
                if (file.exists()) {
                    BufferedReader infile = null;
                    String sLine = null;
                    String glider_nm = null;
                    try {
                        infile = new BufferedReader(new FileReader(file));
                        while ((sLine = infile.readLine()) != null) {
                            String tempsline = sLine.replaceAll(" ", "");
                            if (tempsline.contains("LOGFILECLOSED")) {
                                break;
                            }
                            if (tempsline.contains("VehicleName")) {
                                if (data.get("VehicleName") != null) {
                                    data.put("filename", file.getName());
                                    dataList.add(data);
                                    data = new LinkedHashMap<String, Object>();
                                }
                                String[] temp = tempsline.split(":");
                                data.put("VehicleName", temp[1]);
                                glider_nm = temp[1];
                            } else if (tempsline.contains("CurrTime")) {
                                String[] temp = sLine.split(":");
                                data.put("CurrTime", (temp[1] + temp[2] + temp[3]).trim());
                            } else if (tempsline.contains("DRLocation")) {
                                String[] temp = sLine.split(":");
                                String[] temp2 = temp[1].split("measured");
                                temp2[0] = temp2[0].trim();
                                data.put("DRLocation", temp2[0]);
                            } else if (tempsline.contains("GPSTooFar")) {
                                String[] temp = sLine.split(":");
                                String[] temp2 = temp[1].split("measured");
                                temp2[0] = temp2[0].trim();
                                data.put("GPSTooFar", temp2[0]);
                            } else if (tempsline.contains("GPSInvalid")) {
                                String[] temp = sLine.split(":");
                                String[] temp2 = temp[1].split("measured");
                                temp2[0] = temp2[0].trim();
                                data.put("GPSInvalid", temp2[0]);
                            } else if (tempsline.contains("GPSLocation")) {
                                String[] temp = sLine.split(":");
                                String[] temp2 = temp[1].split("measured");
                                temp2[0] = temp2[0].trim();
                                data.put("GPSLocation", temp2[0]);
                            } else if (tempsline.contains("sensor:")) {
                                String[] temp = sLine.split(":");
                                String[] temp2 = temp[1].split(" ");
                                String temp3 = temp2[0];
                                String[] sensor = temp3.split("=");
                                data.put(sensor[0], sensor[1]);
                            }
                        }
                        data.put("filename", file.getName());
                        dataList.add(data);
                        data = new LinkedHashMap<String, Object>();
                        infile.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    try {
                        dbAccess.log_file_put( glider_nm, file.getName(), 3);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(file.getName()+"파일 DB저장 실패");
                        dataList.clear();
                    }
                    if (!dataList.isEmpty()) {
                        try {
                            for (int i = 0; i < dataList.size(); i++) {
                                glider_nm = dataList.get(i).get("VehicleName").toString();
                                String filename = dataList.get(i).get("filename").toString();
                                for (String key : dataList.get(i).keySet()) {
                                    if (key.equals("VehicleName") || key.equals("filename")) {
                                        continue;
                                    }
                                    int overlabVal = 0;
                                    overlabVal = dbAccess.overlabCheck( key, glider_nm);
                                    dbAccess.commit();
                                    if (overlabVal == 1) {
                                        dbAccess.Glider_Log_Data_Put(filename, key, dataList.get(i).get(key).toString(), glider_nm, 3);
                                    } else {
                                        dbAccess.Glider_Log_Put( key, glider_nm, 3);
                                        dbAccess.Glider_Log_Data_Put( filename, key, dataList.get(i).get(key).toString(), glider_nm, 3);
                                    }
                                }
                            }
                            dbAccess.commit();
                            File dst = new File("D:/movelogfile/" + file.getName());
                            FileUtils.moveFile(file, dst);
                            System.out.println(file.getName()+"파일 DB저장 완료");
                        } catch (Exception e) {
                            dbAccess.rollback();
                            System.out.println(file.getName()+"파일 DB저장 실패");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
