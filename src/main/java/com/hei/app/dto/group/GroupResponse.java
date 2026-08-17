package com.hei.app.dto.group;

import com.hei.app.model.Track;
import java.util.UUID;

public record GroupResponse(UUID id, String ref, Track track) {}
