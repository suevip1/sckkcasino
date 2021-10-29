package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.repository.SysPermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SysPermissionService {



    @Autowired
    private SysPermissionRepository sysPermissionRepository;

    public List<SysPermission> findAll() {
        return sysPermissionRepository.findAll();
    }



    public List<SysPermission> findAllCondition(List<Long> permissionIds) {
        Specification<SysPermission> conditionthis = this.getCondition(permissionIds);
        return sysPermissionRepository.findAll(conditionthis);
    }

    private Specification<SysPermission> getCondition(List<Long> permissionIds) {
        Specification<SysPermission> specification = new Specification<SysPermission>() {
            @Override
            public Predicate toPredicate(Root<SysPermission> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (permissionIds != null && permissionIds.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : permissionIds) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public List<SysPermission> findByPromission(List<Long> permissionIds) {
        List<SysPermission> sysPermissionList = sysPermissionRepository.findAllById(permissionIds);
        return sysPermissionList;
    }
}
