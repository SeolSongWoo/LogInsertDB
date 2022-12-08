package org.example;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;

public class DBAccessImpl implements DBAccess{
    private SqlSession sqlSession;

    DBAccessImpl() {
        String resource = "mabatis-config.xml";

        InputStream is = null;
        SqlSession sqlSession = null;

        try {
            is = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
            sqlSession = sqlSessionFactory.openSession(false);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("[에러] : " + e.getMessage());
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        this.sqlSession = sqlSession;
    }

    public void commit() {
        sqlSession.commit();
    }

    public void rollback() {
        sqlSession.rollback();
    }

    public int overlabCheck(String colname, String glider_nm) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        String glider_seq = FindGliderSeq(glider_nm.replaceAll("_", ""));
        map.put("glider_log_nm", colname);
        map.put("glider_seq", glider_seq);
        int result = sqlSession.selectOne("mybatis.LogOverLabCheck", map);
        return result;
    }

    public void Glider_Log_Put(String colname, String glider_nm, int ex) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            String glider_seq = FindGliderSeq( glider_nm.replaceAll("_", ""));
            map.put("glider_log_nm", colname);
            map.put("glider_seq", glider_seq);
            sqlSession.insert("mybatis.Glider_Log_Put", map);
        } catch (Exception e) {
            throw new Exception(e.getLocalizedMessage());
        }
    }

    public void Glider_Log_Data_Put(String filename, String colname, String colvalue, String glider_nm,String CurrTime,String MissionCode, int ex) throws Exception {
        String logtime = extractionCurrtime(CurrTime);
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            String glider_seq = FindGliderSeq( glider_nm.replaceAll("_", ""));
            map.put("glider_seq", glider_seq);
            String glider_log_info_seq = FindLogSeq( colname, glider_seq);
            String glider_log_seq = FindLogInfoSeq( filename, glider_seq);
            String glider_mis_seq = FindMissionSeq(MissionCode);
            map.put("glider_log_seq", glider_log_seq);
            map.put("glider_log_info_seq", glider_log_info_seq);
            map.put("glider_log_data_gb", colname);
            map.put("glider_log_data_val", colvalue);
            map.put("glider_mis_seq",glider_mis_seq);
            map.put("currtime",logtime);
            sqlSession.insert("mybatis.Glider_Log_Data_Put", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getLocalizedMessage());
        }
    }

    public String FindGliderSeq( String glider_nm) throws Exception {
        String glider_seq = sqlSession.selectOne("mybatis.FindGliderSeq", glider_nm);

        if(glider_seq == null){ throw new Exception("글라이더의 키값을 찾는데 실패하였습니다."); }

        return glider_seq;
    }

    public String FindLogSeq( String glider_log_nm, String glider_seq) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("glider_log_nm", glider_log_nm);
        map.put("glider_seq", glider_seq);
        String glider_log_seq = sqlSession.selectOne("mybatis.FindLogSeq", map);

        if(glider_log_seq == null){ throw new Exception("DB에 저장된 로그파일의 키값을 찾는데 실패하였습니다."); }

        return glider_log_seq;
    }

    public String FindLogInfoSeq( String filename, String glider_seq) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("filename", filename);
        map.put("glider_seq", glider_seq);
        String glider_log_seq = sqlSession.selectOne("mybatis.FindLogInfoSeq", map);
        if(glider_log_seq == null){ throw new Exception("DB에 저장된 로그정보의 키값을 찾는데 실패하였습니다."); }
        return glider_log_seq;
    }

    public String FindMissionSeq(String MissionCode) throws Exception {

        String glider_mis_seq = sqlSession.selectOne("mybatis.FindMissionSeq",MissionCode);
        if(glider_mis_seq == null){ throw new Exception("DB에 저장된 미션정보의 키값을 찾는데 실패하였습니다."); }

        return glider_mis_seq;
    }


    public void log_file_put( String glider_nm, String filename, int ex) throws Exception {
        System.out.println(sqlSession.toString());
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            String glider_nms = glider_nm.replaceAll("_", "");
            String glider_seq = FindGliderSeq( glider_nms);
            String reg_date = extractionFileDate(filename);
            map.put("glider_seq", glider_seq);
            map.put("glider_log_nm", filename);
            map.put("reg_date", reg_date.replaceAll(".log", ""));
            sqlSession.insert("mybatis.log_file_put", map);
        } catch (Exception e) {
            throw new Exception(e.getLocalizedMessage());
        }
    }
    public String extractionFileDate(String filename) throws Exception {
        String[] temp = filename.split("_");
        LocalDateTime now = LocalDateTime.now();
        int timeindex = 0;
        for (int i = 0; i < temp.length; i++) {
            if (temp[i].contains(String.valueOf(now.getYear())) && temp[i].contains("T")) {
                timeindex = i;
            }
        }
        if (timeindex == 0) {
            throw new Exception("로그파일이 과거 파일이거나 잘못된 형식입니다.");
        }
        String reg_date = temp[timeindex].replaceAll("T", "");
        return reg_date;
    }

    public String extractionCurrtime(String currtime) {
        String logtime = null;
        String[] temp = currtime.split(" ");
        logtime = temp[temp.length-2];
        if(currtime.toLowerCase().contains("jan")) {
            logtime += "01";
        }else if(currtime.toLowerCase().contains("feb")) {
            logtime += "02";
        }else if(currtime.toLowerCase().contains("mar")) {
            logtime += "03";
        }else if(currtime.toLowerCase().contains("apr")) {
            logtime += "04";
        }else if(currtime.toLowerCase().contains("may")) {
            logtime += "05";
        }else if(currtime.toLowerCase().contains("jun")) {
            logtime += "06";
        }else if(currtime.toLowerCase().contains("jul")) {
            logtime += "07";
        }else if(currtime.toLowerCase().contains("aug")) {
            logtime += "08";
        }else if(currtime.toLowerCase().contains("sep")) {
            logtime += "09";
        }else if(currtime.toLowerCase().contains("oct")) {
            logtime += "10";
        }else if(currtime.toLowerCase().contains("nov")) {
            logtime += "11";
        }else if(currtime.toLowerCase().contains("dec")) {
            logtime += "12";
        }
        String day = temp[3];
        if(Integer.parseInt(day) < 10) {
            day = "0"+temp[3];
        }
        logtime += day;
        logtime += temp[4];



        return logtime;
    }

}
