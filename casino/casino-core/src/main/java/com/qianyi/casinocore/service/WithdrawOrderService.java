package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.RechargeTurnover;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.repository.WithdrawOrderRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WithdrawOrderService {

    @Autowired
    private WithdrawOrderRepository withdrawOrderRepository;

    public WithdrawOrder saveOrder(WithdrawOrder entity){
        return withdrawOrderRepository.save(entity);
    }

    public WithdrawOrder findUserByIdUseLock(Long userId){
        return withdrawOrderRepository.findUserByWithdrawIdOrderLock(userId);
    }

    public WithdrawOrder findById(Long id) {
        Optional<WithdrawOrder> optional = withdrawOrderRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public Page<WithdrawOrder> findUserPage(Pageable pageable, WithdrawOrder withdrawOrder) {
        Specification<WithdrawOrder> condition = this.getCondition(withdrawOrder);
        return withdrawOrderRepository.findAll(condition, pageable);
    }

    public List<WithdrawOrder> findOrderList( WithdrawOrder withdrawOrder) {
        Specification<WithdrawOrder> condition = this.getCondition(withdrawOrder);
        return withdrawOrderRepository.findAll(condition);
    }

    private Specification<WithdrawOrder> getCondition(WithdrawOrder withdrawOrder) {
        Specification<WithdrawOrder> specification = new Specification<WithdrawOrder>() {
            List<Predicate> list = new ArrayList<Predicate>();
            @Override
            public Predicate toPredicate(Root<WithdrawOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                if (!CommonUtil.checkNull(withdrawOrder.getNo())) {
                    list.add(cb.equal(root.get("no").as(String.class), withdrawOrder.getNo()));
                }
                if(!CommonUtil.checkNull(withdrawOrder.getBankId())){
                    list.add(cb.equal(root.get("bankId").as(String.class), withdrawOrder.getBankId()));
                }
                if(withdrawOrder.getStatus() != null){
                    list.add(cb.equal(root.get("status").as(Integer.class), withdrawOrder.getStatus()));
                }
                if(withdrawOrder.getUserId() != null){
                    list.add(cb.equal(root.get("userId").as(Long.class), withdrawOrder.getUserId()));
                }
                if (withdrawOrder.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), withdrawOrder.getFirstProxy()));
                }
                if (withdrawOrder.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), withdrawOrder.getSecondProxy()));
                }
                if (withdrawOrder.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), withdrawOrder.getThirdProxy()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public Page<WithdrawOrder> findUserPage(Pageable pageable, Long userId,Integer status, String startTime, String endTime) {
        Specification<WithdrawOrder> condition = this.getCondition(userId,status,startTime,endTime);
        return withdrawOrderRepository.findAll(condition, pageable);
    }
    private Specification<WithdrawOrder> getCondition(Long userId,Integer status, String startTime, String endTime) {
        Specification<WithdrawOrder> specification = new Specification<WithdrawOrder>() {
            @Override
            public Predicate toPredicate(Root<WithdrawOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (userId != null ) {
                    list.add(cb.equal(root.get("userId").as(Long.class), userId));
                }
                if (status != null ) {
                    list.add(cb.equal(root.get("status").as(Integer.class), status));
                }
                if(!ObjectUtils.isEmpty(startTime)&&!ObjectUtils.isEmpty(endTime)){
                    list.add(cb.between(root.get("createTime").as(String.class), startTime,endTime));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public Integer countByUserIdAndStatus(Long userId,int status) {
        return withdrawOrderRepository.countByUserIdAndStatus(userId,status);
    }
}
