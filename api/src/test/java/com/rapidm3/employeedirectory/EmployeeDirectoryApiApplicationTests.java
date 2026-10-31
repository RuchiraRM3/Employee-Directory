package com.rapidm3.employeedirectory;

import com.rapidm3.employeedirectory.security.TokenVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class EmployeeDirectoryApiApplicationTests {

    @MockitoBean
    TokenVerifier tokenVerifier;

    @Test
    void contextLoads() {
        // Also proves ddl-auto=validate: entity mappings match db/schema.sql.
    }
}
