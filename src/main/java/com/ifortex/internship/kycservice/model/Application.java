package com.ifortex.internship.kycservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ifortex.internship.kycservice.model.constant.KycStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID applicationId = UUID.randomUUID();

    @Column(nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status = KycStatus.PENDING;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant submittedAt;

    private Instant reviewedAt;

    private UUID reviewerId;

    private String rejectionReason;

    @JsonManagedReference
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents;

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
        if (documents != null) {
            for (Document document : documents) {
                document.setApplication(this);
            }
        }
    }
}