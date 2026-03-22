package org.inn.lockbox.common.repos.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.ObjectRepository;
import org.inn.lockbox.common.models.Credential;
import org.inn.lockbox.common.repos.CredentialRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.dizitart.no2.filters.FluentFilter.where;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CredentialRepositoryDAO implements CredentialRepository {

    // We pass a Supplier so we can "lazily" get the DB only when needed
    private final Supplier<Nitrite> dbProvider;

    private ObjectRepository<Credential> getRepository() {
        log.info("Creating credential repository");
        Nitrite db = dbProvider.get();
        if(db == null) throw new IllegalStateException("Database is not initialized or locked.");
        return db.getRepository(Credential.class);
    }

    @Override
    public void initializeRepository() {
        log.info("Initializing credential repository");
        ObjectRepository<Credential> repository = getRepository();
        if(repository.hasIndex("title")) {
            repository.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "title");
        }
    }

    @Override
    public void save(Credential credential) {
        log.info("Saving credential {}", credential);
        if(credential.getId() == null) {
            getRepository().insert(credential);
        } else {
            getRepository().update(credential);
        }
        getRepository().update(credential, true);
    }

    @Override
    public List<Credential> findAll() {
        log.info("Finding all credentials");
        return getRepository().find().toList();
    }

    @Override
    public Optional<Credential> findByTitle(String title) {
        log.info("Finding credential by title {}", title);
        return Optional.ofNullable(getRepository().find(where("title").eq(title)).firstOrNull());
    }

    @Override
    public Optional<Credential> findById(NitriteId id) {
        log.info("Finding credential by id {}", id);
        return Optional.ofNullable(getRepository().getById(id));
    }

    @Override
    public boolean deleteById(NitriteId id) {
        log.info("Deleting credential by id {}", id);
        return getRepository().remove(where("id").eq(id)).getAffectedCount() > 0;
    }

    @Override
    public boolean deleteAll() {
        log.info("Deleting all credentials");
        getRepository().remove(FluentFilter.where("id").notEq(null));
        return true;
    }

    @Override
    public boolean deleteByTitle(String title) {
        log.info("Deleting credential by title {}", title);
        return getRepository().remove(where("title").eq(title)).getAffectedCount() > 0;
    }
}
