package com.moving.reservation.map;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class KakaoDistanceService {

    private static final String LOCAL_ADDRESS_URL = "https://dapi.kakao.com/v2/local/search/address.json";
    private static final String DIRECTIONS_URL = "https://apis-navi.kakaomobility.com/v1/directions";
    private static final String AUTHORIZATION_PREFIX = "KakaoAK ";
    private static final int METERS_PER_KILOMETER = 1000;

    private final KakaoMapProperties properties;
    private final RestClient restClient;

    public KakaoDistanceService(KakaoMapProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public int calculateDistanceKm(String fromAddress, String toAddress) {
        if (!properties.isReady()) {
            throw new IllegalStateException("Kakao REST API 키를 설정하면 자동 거리 계산을 사용할 수 있습니다.");
        }

        Coordinate origin = findCoordinate(fromAddress);
        Coordinate destination = findCoordinate(toAddress);
        int distanceMeters = findDrivingDistanceMeters(origin, destination);
        return (int) Math.ceil((double) distanceMeters / METERS_PER_KILOMETER);
    }

    private Coordinate findCoordinate(String address) {
        URI uri = UriComponentsBuilder.fromUriString(LOCAL_ADDRESS_URL)
                .queryParam("query", address)
                .build()
                .encode()
                .toUri();

        JsonNode response = restClient.get()
                .uri(uri)
                .header("Authorization", AUTHORIZATION_PREFIX + properties.getRestApiKey())
                .retrieve()
                .body(JsonNode.class);

        JsonNode documents = response == null ? null : response.path("documents");
        if (documents == null || !documents.isArray() || documents.isEmpty()) {
            throw new IllegalArgumentException("주소를 좌표로 변환할 수 없습니다. 주소를 더 정확히 입력해 주세요.");
        }

        JsonNode firstDocument = documents.get(0);
        return new Coordinate(firstDocument.path("x").asText(), firstDocument.path("y").asText());
    }

    private int findDrivingDistanceMeters(Coordinate origin, Coordinate destination) {
        URI uri = UriComponentsBuilder.fromUriString(DIRECTIONS_URL)
                .queryParam("origin", origin.toParameter())
                .queryParam("destination", destination.toParameter())
                .queryParam("priority", "RECOMMEND")
                .queryParam("summary", "true")
                .build()
                .encode()
                .toUri();

        JsonNode response = restClient.get()
                .uri(uri)
                .header("Authorization", AUTHORIZATION_PREFIX + properties.getRestApiKey())
                .retrieve()
                .body(JsonNode.class);

        int distance = response == null ? 0 : response.path("routes").path(0).path("summary").path("distance").asInt();
        if (distance <= 0) {
            throw new IllegalArgumentException("자동 경로 거리를 계산할 수 없습니다. 이동 거리를 직접 입력해 주세요.");
        }

        return distance;
    }

    private record Coordinate(String x, String y) {

        private String toParameter() {
            return x + "," + y;
        }
    }
}
