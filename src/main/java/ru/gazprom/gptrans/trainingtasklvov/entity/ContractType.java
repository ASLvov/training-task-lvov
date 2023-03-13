package ru.gazprom.gptrans.trainingtasklvov.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum ContractType implements EnumClass<String> {

    FIX_PRICE("FixPrice"),
    TIME_AND_MATERIAL("TimeAndMaterial"),
    OUT_STAFF("OutStaff");

    private String id;

    ContractType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ContractType fromId(String id) {
        for (ContractType at : ContractType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}