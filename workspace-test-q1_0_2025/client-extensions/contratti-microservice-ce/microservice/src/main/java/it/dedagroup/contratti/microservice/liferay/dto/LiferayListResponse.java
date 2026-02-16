package it.dedagroup.contratti.microservice.liferay.dto;

import java.util.List;
import java.util.Map;

public record LiferayListResponse(List<Map<String, Object>> items) {
}