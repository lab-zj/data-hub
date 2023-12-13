package net.zjvis.flint.data.hub.repository;


import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UniversalUserRepository extends CrudRepository<UniversalUser, Long> {
}