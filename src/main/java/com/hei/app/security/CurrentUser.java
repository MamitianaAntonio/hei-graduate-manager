package com.hei.app.security;

import com.hei.app.model.Role;
import java.util.UUID;

public record CurrentUser(UUID accountId, Role role, UUID studentId, UUID teacherId) {}
