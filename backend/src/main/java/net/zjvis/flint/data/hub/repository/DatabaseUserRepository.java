package net.zjvis.flint.data.hub.repository;


import net.zjvis.flint.data.hub.entity.account.DatabaseUser;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DatabaseUserRepository extends PagingAndSortingRepository<DatabaseUser, Long> {
    Optional<DatabaseUser> findByUsername(String username);

    List<DatabaseUser> findByUniversalUserId(Long universalUserId);
}
