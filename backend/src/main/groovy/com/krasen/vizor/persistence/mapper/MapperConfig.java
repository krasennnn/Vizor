package com.krasen.vizor.persistence.mapper;

import org.mapstruct.*;

@org.mapstruct.MapperConfig(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface MapperConfig {}
