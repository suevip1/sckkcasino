package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordObty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordObtyRepository extends JpaRepository<GameRecordObty, Long>, JpaSpecificationExecutor<GameRecordObty> {

}