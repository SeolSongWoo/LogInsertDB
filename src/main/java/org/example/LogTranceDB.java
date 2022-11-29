package org.example;
import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
public class LogTranceDB {

    public static void main(String[] args) {

        // TODO Auto-generated method stub

        DBAccess dbAccess = new DBAccessImpl();

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
                    int check = 0;
                    try {
                        infile = new BufferedReader(new FileReader(file));
                        while ((sLine = infile.readLine()) != null) {  //반복을 돌려, 한줄 한줄 내용을 읽어드림.
                            String tempsline = sLine.replaceAll(" ", "");
                            if(tempsline.contains("MissionName")) {
                                check = 1;
                            }

                            if(check != 1) {
                                continue;
                            }
                            if (tempsline.contains("LOGFILECLOSED")) {  //LOGFILECLOSED 즉, 로그파일이 끝나는 구문이 있으면 해당파일 읽기 종료
                                break;
                            }
                            if (tempsline.contains("VehicleName")) {
                                if (data.get("VehicleName") != null) { //중복된 Key명이 발생하면, 기존 맵을 리스트로 넣고, 맵을 초기화.
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
                            } else if(tempsline.contains("MissionName")) {
                                String[] temp = sLine.split(" ");
                                String MissionName = temp[0];
                                String[] MissionName_temp = MissionName.split(":");
                                String MissionCode = temp[1];
                                String[] MissionCode_temp = MissionCode.split(":");
                                data.put("MissionName",MissionName_temp[1]);
                                data.put("MissionCode",MissionCode_temp[1]);
                            }
                        }
                        data.put("filename", file.getName());
                        dataList.add(data);
                        data = new LinkedHashMap<String, Object>();
                        infile.close();
                    } catch (Exception e) {
                        System.out.println(file.getName()+"파일 DB저장 실패");
                        e.printStackTrace();
                        break;
                    }
                    try {
                        dbAccess.log_file_put( glider_nm, file.getName(), 3); //파일명 및 작성날짜등을 DB에 저장
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(file.getName()+"파일 DB저장 실패");
                        dataList.clear();
                    }
                    if (!dataList.isEmpty()) { // DB에 파일데이터 및 로그정보들을 넣는작업 시작.
                        try {
                            for (int i = 0; i < dataList.size(); i++) {
                                glider_nm = dataList.get(i).get("VehicleName").toString();
                                String filename = dataList.get(i).get("filename").toString();
                                for (String key : dataList.get(i).keySet()) {
                                    if (key.equals("VehicleName") || key.equals("filename")) { //로그정보에 파일이름과 글라이더이름은 제외할것.
                                        continue;
                                    }
                                    int overlabVal = 0;
                                    overlabVal = dbAccess.overlabCheck( key, glider_nm); // 중복체크 (넣으려는 로그명이 이미 있는가?)
                                    if (overlabVal == 1) { // 있으면 로그 데이터만 DB에 입력
                                        dbAccess.Glider_Log_Data_Put(filename, key, dataList.get(i).get(key).toString(), glider_nm,dataList.get(i).get("CurrTime").toString(),dataList.get(i).get("MissionCode").toString(), 3);
                                    } else { // 없으면, 로그명을 새로 입력하고, 데이터 입력
                                        dbAccess.Glider_Log_Put( key, glider_nm, 3);
                                        dbAccess.Glider_Log_Data_Put( filename, key, dataList.get(i).get(key).toString(), glider_nm,dataList.get(i).get("CurrTime").toString(),dataList.get(i).get("MissionCode").toString(), 3);
                                    }
                                }
                            }
                            dbAccess.commit(); //위 작업이 모두 원활히 수행되면 커밋 실행
                            File dst = new File(MoveFilePath(file.getName()));
                            FileUtils.moveFile(file, dst); // DB에 저장한 파일을 다른 폴더로 옮김.
                            System.out.println(file.getName()+"파일 DB저장 완료");
                            dataList = new ArrayList<LinkedHashMap<String, Object>>();
                        } catch (Exception e) {
                            dbAccess.rollback(); //위 작업들의 문제가 발생하면 롤백.
                            System.out.println(file.getName()+"파일 DB저장 실패");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static String MoveFilePath(String filename) {
        String[] split = filename.split("network");
        String FileDir = (split[0].replaceAll("_", "")).toUpperCase();
        String Path = "D:/movelogfile/"+FileDir+"/"+filename;
        return Path;
    }
}
