package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ExtractPointsChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExtractPointsChangeRepository extends JpaRepository<ExtractPointsChange,Long>, JpaSpecificationExecutor<ExtractPointsChange> {
}
