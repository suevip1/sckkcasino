package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordVNC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

public interface GameRecordVNCRepository extends JpaRepository<GameRecordVNC, Long>, JpaSpecificationExecutor<GameRecordVNC> {

    GameRecordVNC findByMerchantCodeAndBetOrder(String merchantCode, String betOrder);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordVnc u set u.codeNumStatus=?2 where u.id=?1")
    void updateCodeNumStatus(Long id, Integer codeNumStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordVnc u set u.extractStatus=?2 where u.id=?1")
    void updateExtractStatus(Long id, Integer status);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordVnc u set u.washCodeStatus=?2 where u.id=?1")
    void updateWashCodeStatus(Long id, Integer washCodeStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordVnc u set u.rebateStatus=?2 where u.id=?1")
    void updateRebateStatus(Long id, Integer rebateStatus);

    @Query(value = "SELECT IFNULL(sum(real_money),0) turnover,count(1) betCount,IFNULL(sum(bet_money),0) betAmount,IFNULL(sum(win_money),0) winAmount,\n" +
            "from game_record_vnc where platform=?1 and bet_time_str BETWEEN ?2 and ?3",nativeQuery = true)
    Map<String,Object> findSumByPlatformAndTime(String platform, String startTime, String endTime);
}
