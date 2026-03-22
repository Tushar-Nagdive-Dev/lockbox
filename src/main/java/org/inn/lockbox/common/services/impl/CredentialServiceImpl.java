package org.inn.lockbox.common.services.impl;

import lombok.RequiredArgsConstructor;
import org.inn.lockbox.common.models.Credential;
import org.inn.lockbox.common.repos.CredentialRepository;
import org.inn.lockbox.common.services.CredentialService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final CredentialRepository credentialRepository;

    @Override
    public void sealNew(Credential credential) {
        if(credentialRepository.findByTitle(credential.getTitle()).isPresent()) {
            throw new RuntimeException("Seal with this title already exists");
        }
        credentialRepository.save(credential);
    }
}
