package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long>, JpaSpecificationExecutor<GameRecord> {

    @Query(value = "SELECT end_time from game_record ORDER BY end_time desc limit 1",nativeQuery = true)
    String findEndTime();

}
