package com.authorizationserver.db.repository;

import com.authorizationserver.db.model.RsaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for key related data manipulation
 *
 * @author Blajan George
 */
@Repository
public interface RsaKeyRepository extends JpaRepository<RsaKey, String> {

    /**
     * Retrieve most recent key
     *
     * @return {@link RsaKey}
     */
    @Query(value = "SELECT * FROM rsa_key ORDER BY time DESC LIMIT 1", nativeQuery = true)
    Optional<RsaKey> getMostRecentKey();

    /**
     * @param n number of public key to be retrieved
     * @return {@link List} of {@link RsaKey}
     */
    @Query(value = "SELECT * FROM rsa_key ORDER BY time DESC LIMIT :n", nativeQuery = true)
    List<RsaKey> getLimitNMostRecentPublicKeys(@Param("n") Integer n);
}