package net.zjvis.flint.data.hub.repository;


import net.zjvis.flint.data.hub.entity.account.DingTalkUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DingTalkUserRepository extends CrudRepository<DingTalkUser, Long> {
    Optional<DingTalkUser> findByOpenId(String openId);

    List<DingTalkUser> findByUniversalUserId(Long universalUserId);
}