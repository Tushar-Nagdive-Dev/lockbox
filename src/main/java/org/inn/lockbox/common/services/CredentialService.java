package org.inn.lockbox.common.services;

import org.inn.lockbox.common.models.Credential;

public interface CredentialService {

    void sealNew(Credential cred);
}
