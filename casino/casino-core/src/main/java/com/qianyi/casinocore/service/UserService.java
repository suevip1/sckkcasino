package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserDetail;
import com.qianyi.casinocore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    UserRepository userRepository;
    public User findByAccount(String account) {
        return userRepository.findByAccount(account);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findById(Long id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public void subMoney(Long id, BigDecimal money) {
        synchronized (id) {
            userRepository.subMoney(id,money);
        }
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public void addMoney(Long id, BigDecimal money) {
        synchronized (id) {
            userRepository.addMoney(id,money);
        }
    }

    public Integer countByIp(String ip) {
        return userRepository.countByRegisterIp(ip);
    }


    /**
     * 用户列表查询
     *
     * @param user
     * @return
     */
    public Page<User> findUserPage(Pageable pageable, User user) {
        Specification<User> condition = this.getCondition(user);
        return userRepository.findAll(condition, pageable);
    }

    /**
     * 查询条件拼接，灵活添加条件
     * @param user
     * @return
     */
    private Specification<User> getCondition(User user) {
        Specification<User> specification = new Specification<User>(){
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
//                if(StringUtils.isNotBlank(user.getAccount())){
//                    list.add(cb.equal(root.get("account").as(String.class), user.getAccount()));
//                }
                if(user.getId() != null){
                    list.add(cb.equal(root.get("id").as(Long.class), user.getId()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

}
