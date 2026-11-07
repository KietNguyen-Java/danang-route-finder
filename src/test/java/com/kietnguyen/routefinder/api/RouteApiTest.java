package com.kietnguyen.routefinder.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests over HTTP against the seeded Da Nang map (Flyway migrations on H2, or on
 * PostgreSQL when SPRING_DATASOURCE_URL points to one). Seed ids: 1 = airport, 4 = Dragon Bridge,
 * 7 = Han Market, 9 = My Khe Beach, 12 = Marble Mountains, 14 = FPT University.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RouteApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listsSeededPlaces() throws Exception {
        mvc.perform(get("/api/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(24))));
        mvc.perform(get("/api/places").param("category", "beach"))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void findsRouteFromAirportToFptUniversity() throws Exception {
        mvc.perform(get("/api/routes").param("from", "1").param("to", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("ASTAR"))
                .andExpect(jsonPath("$.places[0].name").value("Sân bay Quốc tế Đà Nẵng"))
                .andExpect(jsonPath("$.places[-1].name").value("Đại học FPT Đà Nẵng"))
                .andExpect(jsonPath("$.totalDistanceMeters", greaterThan(10_000)))
                .andExpect(jsonPath("$.steps[0].road").isNotEmpty());
    }

    @Test
    void aStarAndDijkstraAgreeButAStarDoesLessWork() throws Exception {
        mvc.perform(get("/api/routes/compare").param("from", "3").param("to", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runs[0].algorithm").value("DIJKSTRA"))
                .andExpect(jsonPath("$.runs[1].nodesExpanded", lessThanOrEqualTo(24)));
        String dijkstra = body(get("/api/routes").param("from", "3").param("to", "20").param("algorithm", "DIJKSTRA"));
        String aStar = body(get("/api/routes").param("from", "3").param("to", "20").param("algorithm", "ASTAR"));
        assertEquals(field(dijkstra, "totalDistanceMeters"), field(aStar, "totalDistanceMeters"));
    }

    @Test
    void honoursOneWayRoad() throws Exception {
        // Bach Dang is one-way from Han Market (7) to Dragon Bridge (4).
        mvc.perform(get("/api/routes").param("from", "7").param("to", "4"))
                .andExpect(jsonPath("$.places", hasSize(2)));
        mvc.perform(get("/api/routes").param("from", "4").param("to", "7"))
                .andExpect(jsonPath("$.places", hasSize(greaterThan(2))));
    }

    @Test
    void returnsAlternativeRoutesCheapestFirst() throws Exception {
        mvc.perform(get("/api/routes/alternatives").param("from", "1").param("to", "9").param("k", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].algorithm").value("YEN"));
    }

    @Test
    void plansRoundTripVisitingEveryStop() throws Exception {
        mvc.perform(post("/api/tours").contentType(MediaType.APPLICATION_JSON).content("""
                        {"startPlaceId": 1, "stopPlaceIds": [9, 12, 7, 4], "returnToStart": true}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitOrder", hasSize(6)))
                .andExpect(jsonPath("$.visitOrder[0].id").value(1))
                .andExpect(jsonPath("$.visitOrder[5].id").value(1))
                .andExpect(jsonPath("$.legs", hasSize(5)));
    }

    @Test
    void findsNearbyPlacesNearestFirst() throws Exception {
        mvc.perform(get("/api/places/nearby").param("lat", "16.0612").param("lon", "108.2270").param("radiusMeters", "900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].place.name").value("Cầu Rồng"))
                .andExpect(jsonPath("$[0].distanceMeters").value(0))
                .andExpect(jsonPath("$[1].distanceMeters", lessThanOrEqualTo(900)));
    }

    @Test
    void newRoadIsUsedImmediately() throws Exception {
        String created = body(post("/api/places").contentType(MediaType.APPLICATION_JSON).content("""
                {"name": "Bán đảo Sơn Trà - Bãi Bụt", "category": "nature", "latitude": 16.1170, "longitude": 108.2800}"""));
        long id = Long.parseLong(field(created, "id"));

        mvc.perform(get("/api/routes").param("from", "13").param("to", String.valueOf(id)))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/roads").contentType(MediaType.APPLICATION_JSON).content("""
                        {"fromPlaceId": 13, "toPlaceId": %d, "name": "Hoàng Sa", "distanceMeters": 2600, "speedKmh": 30}"""
                        .formatted(id)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/routes").param("from", "1").param("to", String.valueOf(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.places[-1].id").value(id));
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        mvc.perform(get("/api/routes").param("from", "1").param("to", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No place with id 999"));
        mvc.perform(get("/api/routes").param("from", "1").param("to", "2").param("algorithm", "TELEPORT"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/routes").param("from", "1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/routes/alternatives").param("from", "1").param("to", "2").param("k", "50"))
                .andExpect(status().isBadRequest());
        // Airport to Hoi An is about 25 km in a straight line; a 1 km road is impossible.
        mvc.perform(post("/api/roads").contentType(MediaType.APPLICATION_JSON).content("""
                        {"fromPlaceId": 1, "toPlaceId": 20, "distanceMeters": 1000, "speedKmh": 50}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("shorter than the straight-line distance")));
        mvc.perform(post("/api/places").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Cầu Rồng", "category": "landmark", "latitude": 16.06, "longitude": 108.22}"""))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/places").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "", "category": "x", "latitude": 99, "longitude": 108}"""))
                .andExpect(status().isBadRequest());
    }

    private String body(org.springframework.test.web.servlet.RequestBuilder request) throws Exception {
        MvcResult result = mvc.perform(request).andReturn();
        return result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Reads a top-level number field without pulling in a JSON library. */
    private static String field(String json, String name) {
        int start = json.indexOf("\"" + name + "\":") + name.length() + 3;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        return json.substring(start, end);
    }
}
