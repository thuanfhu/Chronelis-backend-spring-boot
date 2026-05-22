package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserSummaryMapper {
  UserSummaryResponse toSummary(User user);
}
