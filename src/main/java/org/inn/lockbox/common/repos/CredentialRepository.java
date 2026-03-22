package org.inn.lockbox.common.repos;

import org.dizitart.no2.collection.NitriteId;
import org.inn.lockbox.common.models.Credential;

import java.util.List;
import java.util.Optional;

public interface CredentialRepository {

    void save(Credential credential);

    List<Credential> findAll();

    Optional<Credential> findByTitle(String title);

    Optional<Credential> findById(NitriteId id);

    boolean deleteById(NitriteId id);

    boolean deleteAll();

    boolean deleteByTitle(String title);

    void initializeRepository();
}
