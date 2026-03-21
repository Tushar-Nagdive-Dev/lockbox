package org.inn.lockbox.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.Indices;
import org.inn.lockbox.common.enums.CredentialCategory;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Indices({
        @Index(fields = "title", type = IndexType.UNIQUE),
        @Index(fields = "type", type = IndexType.NON_UNIQUE)
})
public class Credential implements Serializable {

    @Id
    private NitriteId id;

    private String title;
    private String description;
    private CredentialCategory type;

    private Map<String, String> secrets;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expireAt;
    private boolean isActive;
}
