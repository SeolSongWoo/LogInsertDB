package org.example;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;

public class DBAccess {
    private SqlSession sqlSession;

    public void DBAccess () {
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
            System.out.println("오류 재시도 남은횟수 : " + ex);
            if (ex != 0) {
                ex--;
                Glider_Log_Put( colname, glider_nm, ex);
            } else {
                throw new Exception("");
            }
        }
    }

    public void Glider_Log_Data_Put(String filename, String colname, String colvalue, String glider_nm, int ex) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            String glider_seq = FindGliderSeq( glider_nm.replaceAll("_", ""));
            map.put("glider_seq", glider_seq);
            String glider_log_info_seq = FindLogSeq( colname, glider_seq);
            String glider_log_seq = FindLogInfoSeq( filename, glider_seq);
            map.put("glider_log_seq", glider_log_seq);
            map.put("glider_log_info_seq", glider_log_info_seq);
            map.put("glider_log_data_gb", colname);
            map.put("glider_log_data_val", colvalue);
            sqlSession.insert("mybatis.Glider_Log_Data_Put", map);
        } catch (Exception e) {
            System.out.println("오류 재시도 남은횟수 : " + ex);
            if (ex != 0) {
                ex--;
                Glider_Log_Put( colname, glider_nm, ex);
            } else {
                throw new Exception("로그의 데이터를 입력하는 과정에서 문제가 발생하였습니다.");
            }
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


    public void log_file_put( String glider_nm, String filename, int ex) throws Exception {
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
            System.out.println("오류 재시도 남은횟수 : " + ex);
            if (ex != 0) {
                ex--;
                log_file_put( glider_nm, filename, ex);
            } else {
                throw new Exception(e.getMessage());
            }
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

}
