package ru.gazprom.gptrans.trainingtasklvov.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@JmixEntity
@Table(name = "TTL_STATUS")
@Entity(name = "ttl_Status")
public class Status {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotBlank(message = "{msg://ru.gazprom.gptrans.trainingtasklvov.entity/Status.code.validation.NotBlank}")
    @Column(name = "CODE")
    private String code;

    @NotBlank(message = "{msg://ru.gazprom.gptrans.trainingtasklvov.entity/Status.name.validation.NotBlank}")
    @InstanceName
    @Column(name = "NAME")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}