package com.krasen.vizor.persistence.mapper;

public interface BaseMapper<D, E> {
    D toDomain(E entity);
    E toEntity(D domain);
}
