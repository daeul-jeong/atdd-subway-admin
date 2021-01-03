package nextstep.subway.station;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends AcceptanceTest {

	public static StationResponse 지하철역_등록되어_있음(String stationName) {
		Map<String, String> params = new HashMap<>();
		params.put("name", stationName);

		Long stationId = 역_생성_후_stationId응답(stationName);

		ExtractableResponse<Response> getResponse = RestAssured.given().log().all()
				.when()
				.get("/stations/" + stationId)
				.then().log().all()
				.extract();

		StationResponse stationResponse = getResponse.jsonPath().getObject(".", StationResponse.class);
		assertThat(stationResponse.getId()).isEqualTo(Long.valueOf(stationId));
		return stationResponse;
	}


	@DisplayName("지하철역을 생성한다.")
	@Test
	void createStation() {
		Long stationId = 역_생성_후_stationId응답("강남역");
	}

	@DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성한다.")
	@Test
	void createStationWithDuplicateName() {
		// given
		Long stationId = 역_생성_후_stationId응답("강남역");

		// when
		Map<String, String> params = new HashMap<>();
		params.put("name", "강남역");
		ExtractableResponse<Response> response = RestAssured.given().log().all()
				.body(params)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.when()
				.post("/stations")
				.then()
				.log().all()
				.extract();

		// then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@DisplayName("지하철역을 조회한다.")
	@Test
	void getStations() {
		/// given
		Long stationId = 역_생성_후_stationId응답("강남역");

		Long stationId2 = 역_생성_후_stationId응답("역삼역");

		// when
		ExtractableResponse<Response> response = RestAssured.given().log().all()
				.when()
				.get("/stations")
				.then().log().all()
				.extract();

		// then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		List<Long> expectedLineIds = Arrays.asList(stationId, stationId2);
		List<Long> resultLineIds = response.jsonPath().getList(".", StationResponse.class).stream()
				.map(it -> it.getId())
				.collect(Collectors.toList());
		assertThat(resultLineIds).containsAll(expectedLineIds);
	}

	@DisplayName("지하철역을 제거한다.")
	@Test
	void deleteStation() {
		// given
		Long stationId = 역_생성_후_stationId응답("강남역");

		// when
		ExtractableResponse<Response> response = RestAssured.given().log().all()
				.when()
				.delete("/stations/" + stationId)
				.then().log().all()
				.extract();

		// then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
}
