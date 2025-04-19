package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.BlockingResponse;
import com.limed_backend.security.entity.Blocking;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface BlockingMapper {
    BlockingResponse toBlockingResponse(Blocking blocking);
}