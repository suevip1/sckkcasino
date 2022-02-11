package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.AdGame;
import com.qianyi.casinocore.repository.AdGameRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Service
@CacheConfig(cacheNames = {"adGame"})
public class AdGamesService {

    @Autowired
    private AdGameRepository adGameRepository;

    public List<AdGame> findByGameCode(String gameCode){
        return adGameRepository.findByGameCode(gameCode);
    }


    public Page<AdGame> findAll(Pageable pageable, AdGame adGame) {
        Specification<AdGame> condition = this.getCondition(adGame);
        return adGameRepository.findAll(condition, pageable);
    }

    private Specification<AdGame> getCondition(AdGame adGame) {
        Specification<AdGame> specification = new Specification<AdGame>() {
            @Override
            public Predicate toPredicate(Root<AdGame> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(adGame.getGameName())) {
                    list.add(cb.equal(root.get("gameName").as(String.class), adGame.getGameName()));
                }
                if (adGame.getGamesStatus() != null) {
                    list.add(cb.equal(root.get("gamesStatus").as(Integer.class), adGame.getGamesStatus()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public List<AdGame> findAllConnection(Specification<AdGame> condition) {
        return adGameRepository.findAll(condition);
    }

    public void saveAll(List<AdGame> adGameList) {
        adGameRepository.saveAll(adGameList);
    }

    public Long fontCount() {
        return adGameRepository.count();
    }
}
