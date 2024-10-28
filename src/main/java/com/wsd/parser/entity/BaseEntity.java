package com.wsd.parser.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created", updatable = false, nullable = false)
    private LocalDateTime created;

    @Column(name = "updated")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime updated;
    @Version
    private Long version;

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @PrePersist
    protected void onCreate() {
        this.created = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated = LocalDateTime.now();
    }


}

