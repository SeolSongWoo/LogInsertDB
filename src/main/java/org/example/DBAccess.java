package org.example;

public interface DBAccess {

    /**
     * 작업들이 정상적으로 완료되면 DB 커밋
     */
    void commit();

    /**
     * 작업들에 문제가 발생하면 DB 롤백
     */
    void rollback();

    /**
     * 로그 중복 체크(해당 로그파일이 존재하는가?)
     * @param glider_log
     * @return 1 or 0
     * @throws Exception
     */
    int overlabCheck(String colname, String glider_nm) throws Exception;
    void Glider_Log_Put(String colname, String glider_nm, int ex) throws Exception;
    void Glider_Log_Data_Put(String filename, String colname, String colvalue, String glider_nm,String CurrTime,String MissionCode, int ex) throws Exception;
    String FindGliderSeq( String glider_nm) throws Exception;
    String FindLogSeq( String glider_log_nm, String glider_seq) throws Exception;
    String FindLogInfoSeq( String filename, String glider_seq) throws Exception;
    String FindMissionSeq(String MissionCode) throws Exception;
    void log_file_put( String glider_nm, String filename, int ex) throws Exception;
    String extractionFileDate(String filename) throws Exception;
    String extractionCurrtime(String currtime);



}
