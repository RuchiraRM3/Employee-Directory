package com.rapidm3.employeedirectory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

import com.rapidm3.employeedirectory.entity.EmployeeChange;
import com.rapidm3.employeedirectory.repository.EmployeeChangeRepository;
import com.rapidm3.employeedirectory.security.InvalidTokenException;
import com.rapidm3.employeedirectory.security.TokenVerifier;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end API tests: MockMvc -> filters -> controller -> service -> JPA -> real local MySQL
 * (employee_directory_test, built from db/schema.sql + db/seed.sql). Each test rolls back.
 * Seed facts used: 16 employees, Engineering(1)=7, Human Resources(2)=4, Finance(3)=5, Customer Success(4)=0.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmployeeApiTest {

    private static final String VALID_TOKEN = "valid-token";
    private static final String UID = "test-uid";

    @Autowired
    MockMvc mvc;

    @Autowired
    EmployeeChangeRepository changes;

    @MockitoBean
    TokenVerifier tokenVerifier;

    @BeforeEach
    void stubFirebase() {
        when(tokenVerifier.verify(VALID_TOKEN)).thenReturn(UID);
        when(tokenVerifier.verify("bad-token")).thenThrow(new InvalidTokenException("bad", null));
    }

    private ResultActions call(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN));
    }

    private ResultActions send(MockHttpServletRequestBuilder request, String json) throws Exception {
        return call(request.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String body(String name, String email, String jobTitle, Object departmentId, String status) {
        return """
                {"name": %s, "email": %s, "jobTitle": %s, "departmentId": %s, "hireDate": "2026-01-05", "status": %s}
                """.formatted(q(name), q(email), q(jobTitle), departmentId, q(status));
    }

    private static String q(String s) {
        return s == null ? "null" : "\"" + s + "\"";
    }

    /** Body equal to seed employee 2 (Bram Holloway) with optional overrides. */
    private static String bram(String jobTitle, int departmentId) {
        return body("Bram Holloway", "bram.holloway@example.test", jobTitle, departmentId, "ACTIVE");
    }

    @Nested
    class Authentication {

        @Test
        void missingTokenIsRejected() throws Exception {
            mvc.perform(get("/employees"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }

        @Test
        void invalidTokenIsRejected() throws Exception {
            mvc.perform(get("/departments").header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", containsString("expired")));
        }

        @Test
        void nonBearerSchemeIsRejected() throws Exception {
            mvc.perform(get("/employees").header(HttpHeaders.AUTHORIZATION, "Basic abc"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void corsPreflightFromViteOriginIsAllowedWithoutToken() throws Exception {
            mvc.perform(options("/employees")
                            .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
        }

        @Test
        void corsPreflightFromOtherOriginIsRefused() throws Exception {
            mvc.perform(options("/employees")
                            .header(HttpHeaders.ORIGIN, "http://evil.example")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void unauthorizedResponseStillCarriesCorsHeaderSoBrowserSees401() throws Exception {
            mvc.perform(get("/employees").header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
        }
    }

    @Nested
    class Search {

        @Test
        void noFiltersReturnsEveryoneSortedByName() throws Exception {
            call(get("/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(16)))
                    .andExpect(jsonPath("$[0].name").value("Avery Quill"))
                    .andExpect(jsonPath("$[0].department.name").value("Engineering"));
        }

        @Test
        void nameIsCaseInsensitiveTrimmedSubstring() throws Exception {
            call(get("/employees").param("name", "  QUILL "))
                    .andExpect(jsonPath("$[*].name", contains("Avery Quill")));
        }

        @Test
        void departmentFilterAlone() throws Exception {
            call(get("/employees").param("departmentId", "2"))
                    .andExpect(jsonPath("$", hasSize(4)));
        }

        @Test
        void nameAndDepartmentCombineWithAnd() throws Exception {
            call(get("/employees").param("name", "er").param("departmentId", "1"))
                    .andExpect(jsonPath("$[*].name", contains("Avery Quill", "Piers Danvers")));
        }

        @Test
        void blankNameMeansNoNameFilter() throws Exception {
            call(get("/employees").param("name", "   ").param("departmentId", "3"))
                    .andExpect(jsonPath("$", hasSize(5)));
        }

        @Test
        void noMatchesReturnsEmptyArray() throws Exception {
            call(get("/employees").param("name", "zzz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        /** Regression: '%' and '_' were passed to LIKE as wildcards, so they matched every employee. */
        @Test
        void likeWildcardsAreSearchedLiterally() throws Exception {
            call(get("/employees").param("name", "_")).andExpect(jsonPath("$", hasSize(0)));
            call(get("/employees").param("name", "%")).andExpect(jsonPath("$", hasSize(0)));
            call(get("/employees").param("name", "\\")).andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        void nonNumericDepartmentIdIs400() throws Exception {
            call(get("/employees").param("departmentId", "abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        }

        @Test
        void departmentsAreListedByName() throws Exception {
            call(get("/departments"))
                    .andExpect(jsonPath("$[*].name",
                            contains("Customer Success", "Engineering", "Finance", "Human Resources")));
        }
    }

    @Nested
    class Details {

        @Test
        void existingEmployee() throws Exception {
            call(get("/employees/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("avery.quill@example.test"))
                    .andExpect(jsonPath("$.hireDate").value("2021-03-15"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.department.id").value(1));
        }

        @Test
        void unknownEmployeeIs404WithJsonError() throws Exception {
            call(get("/employees/9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("EMPLOYEE_NOT_FOUND"))
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    class Create {

        @Test
        void validEmployeeIs201WithNormalizedEmail() throws Exception {
            send(post("/employees"), body("  Rowan Tate ", " Rowan.Tate@Example.TEST ", "Analyst", 4, "ACTIVE"))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, containsString("/employees/")))
                    .andExpect(jsonPath("$.name").value("Rowan Tate"))
                    .andExpect(jsonPath("$.email").value("rowan.tate@example.test"))
                    .andExpect(jsonPath("$.department.name").value("Customer Success"));

            call(get("/employees").param("departmentId", "4")).andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        void invalidFieldsAre400WithFieldErrors() throws Exception {
            send(post("/employees"), body(" ", "not-an-email", null, null, "ACTIVE"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.fieldErrors.name").value("Name is required."))
                    .andExpect(jsonPath("$.fieldErrors.email").value("Email must be a valid email address."))
                    .andExpect(jsonPath("$.fieldErrors.jobTitle").exists())
                    .andExpect(jsonPath("$.fieldErrors.departmentId").exists());
        }

        @Test
        void nonexistentDepartmentIs400() throws Exception {
            send(post("/employees"), body("Rowan Tate", "rowan@example.test", "Analyst", 999, "ACTIVE"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.departmentId").value("Department does not exist."));
        }

        @Test
        void unknownStatusIs400() throws Exception {
            send(post("/employees"), body("Rowan Tate", "rowan@example.test", "Analyst", 1, "RETIRED"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
        }

        @Test
        void duplicateEmailIgnoringCaseIs409() throws Exception {
            send(post("/employees"), body("Someone Else", "AVERY.QUILL@example.test", "Analyst", 1, "ACTIVE"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
                    .andExpect(jsonPath("$.fieldErrors.email").value("Email is already in use."));
        }

        @Test
        void errorBodyNeverLeaksInternals() throws Exception {
            send(post("/employees"), "{ not json")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", not(containsString("Exception"))))
                    .andExpect(jsonPath("$.trace").doesNotExist());
        }
    }

    @Nested
    class Update {

        @Test
        void unknownEmployeeIs404() throws Exception {
            send(put("/employees/9999"), bram("Software Engineer", 1))
                    .andExpect(status().isNotFound());
        }

        @Test
        void invalidBodyIs400() throws Exception {
            send(put("/employees/2"), body("Bram", "bad", "x", 1, "ACTIVE"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void takingAnotherEmployeesEmailIs409() throws Exception {
            send(put("/employees/2"), body("Bram Holloway", "avery.quill@example.test", "Software Engineer", 1, "ACTIVE"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
        }

        @Test
        void keepingOwnEmailAndTrackedFieldsWritesNoAuditRow() throws Exception {
            send(put("/employees/2"), body("Bram H. Holloway", "bram.holloway@example.test", "Software Engineer", 1, "ON_LEAVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Bram H. Holloway"))
                    .andExpect(jsonPath("$.status").value("ON_LEAVE"));

            assertThat(changes.findByEmployeeIdOrderByIdAsc(2L)).isEmpty();
        }

        @Test
        void departmentChangeWritesAuditRow() throws Exception {
            send(put("/employees/2"), bram("Software Engineer", 3))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.department.name").value("Finance"));

            List<EmployeeChange> rows = changes.findByEmployeeIdOrderByIdAsc(2L);
            assertThat(rows).hasSize(1);
            EmployeeChange row = rows.get(0);
            assertThat(row.getOldDepartment().getId()).isEqualTo(1L);
            assertThat(row.getNewDepartment().getId()).isEqualTo(3L);
            assertThat(row.getOldJobTitle()).isNull();
            assertThat(row.getNewJobTitle()).isNull();
            assertThat(row.getChangedBy()).isEqualTo(UID);
        }

        @Test
        void jobTitleChangeWritesAuditRow() throws Exception {
            send(put("/employees/2"), bram("Senior Software Engineer", 1)).andExpect(status().isOk());

            List<EmployeeChange> rows = changes.findByEmployeeIdOrderByIdAsc(2L);
            assertThat(rows).hasSize(1);
            assertThat(rows.get(0).getOldJobTitle()).isEqualTo("Software Engineer");
            assertThat(rows.get(0).getNewJobTitle()).isEqualTo("Senior Software Engineer");
            assertThat(rows.get(0).getOldDepartment()).isNull();
        }

        @Test
        void bothChangesInOneUpdateWriteOneRow() throws Exception {
            send(put("/employees/2"), bram("Finance Analyst", 3)).andExpect(status().isOk());

            List<EmployeeChange> rows = changes.findByEmployeeIdOrderByIdAsc(2L);
            assertThat(rows).hasSize(1);
            assertThat(rows.get(0).getNewDepartment().getId()).isEqualTo(3L);
            assertThat(rows.get(0).getNewJobTitle()).isEqualTo("Finance Analyst");
        }

        @Test
        void auditIsNotWrittenWhenUpdateFails() throws Exception {
            send(put("/employees/2"), bram("Senior Software Engineer", 999)).andExpect(status().isBadRequest());

            assertThat(changes.findByEmployeeIdOrderByIdAsc(2L)).isEmpty();
        }
    }

    /** DELETE /employees/{id} archives (soft-deletes): hidden everywhere, but the row and its history stay. */
    @Nested
    class Archive {

        @Autowired
        JdbcTemplate jdbc;

        @Test
        void deleteArchivesEmployeeAndKeepsRecordAndHistory() throws Exception {
            int historyBefore = changes.findByEmployeeIdOrderByIdAsc(11L).size();

            call(delete("/employees/11")).andExpect(status().isNoContent());

            call(get("/employees/11")).andExpect(status().isNotFound());
            call(get("/employees"))
                    .andExpect(jsonPath("$", hasSize(15)))
                    .andExpect(jsonPath("$[*].id", not(hasItem(11))));
            Map<String, Object> row = jdbc.queryForMap("select archived_at, archived_by from employee where id = 11");
            assertThat(row.get("archived_at")).isNotNull();
            assertThat(row.get("archived_by")).isEqualTo(UID);
            assertThat(changes.findByEmployeeIdOrderByIdAsc(11L)).hasSize(historyBefore);
        }

        @Test
        void deletingUnknownOrAlreadyArchivedEmployeeIs404() throws Exception {
            call(delete("/employees/9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("EMPLOYEE_NOT_FOUND"));
            call(delete("/employees/2")).andExpect(status().isNoContent());
            call(delete("/employees/2")).andExpect(status().isNotFound());
        }

        @Test
        void archivedEmployeeCannotBeEdited() throws Exception {
            call(delete("/employees/2")).andExpect(status().isNoContent());
            send(put("/employees/2"), bram("Software Engineer", 1)).andExpect(status().isNotFound());
        }

        @Test
        void archivedEmployeesEmailStaysReserved() throws Exception {
            call(delete("/employees/2")).andExpect(status().isNoContent());
            send(post("/employees"), body("New Bram", "bram.holloway@example.test", "Engineer", 1, "ACTIVE"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
                    .andExpect(jsonPath("$.fieldErrors.email").value(containsString("archived")));
        }

        @Test
        void deleteRequiresSignIn() throws Exception {
            mvc.perform(delete("/employees/2")).andExpect(status().isUnauthorized());
            call(get("/employees/2")).andExpect(status().isOk());
        }

        @Test
        void corsPreflightAllowsDeleteFromViteOrigin() throws Exception {
            mvc.perform(options("/employees/2")
                            .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                    .andExpect(status().isOk());
        }
    }

    /** Regression: framework errors returned Spring's default body (no code/message) instead of ApiError. */
    @Nested
    class ErrorShape {

        @Test
        void unsupportedMethodIs405ApiError() throws Exception {
            call(patch("/employees/1"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        void unsupportedContentTypeIs415ApiError() throws Exception {
            call(post("/employees").contentType(MediaType.TEXT_PLAIN).content("x"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        void unknownPathIs404ApiError() throws Exception {
            call(get("/nope"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
    }
}
