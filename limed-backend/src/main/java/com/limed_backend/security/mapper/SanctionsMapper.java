package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Sanction.ActiveSanctionResponse;
import com.limed_backend.security.dto.Sanction.InactiveSanctionResponse;
import com.limed_backend.security.entity.Sanction;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SanctionsMapper {

    @Named("toActiveSanctionResponse")
    default ActiveSanctionResponse toActiveSanctionResponse(Sanction sanction) {

        return new ActiveSanctionResponse(
                sanction.getId(),
                sanction.getSanctionType(),
                sanction.getStartTime(),
                sanction.getEndTime(),
                sanction.getReason(),
                sanction.getSanctionedBy().getUsername());
    }

    @Named("toInactiveSanctionResponse")
    default InactiveSanctionResponse toInactiveSanctionResponse(Sanction sanction) {

        return new InactiveSanctionResponse(
                sanction.getId(),
                sanction.getSanctionType(),
                sanction.getStartTime(),
                sanction.getEndTime(),
                sanction.getReason(),
                sanction.getSanctionedBy().getUsername(),
                sanction.isRevokedSanction(),
                sanction.getRevokedBy().getUsername());
    }
}
