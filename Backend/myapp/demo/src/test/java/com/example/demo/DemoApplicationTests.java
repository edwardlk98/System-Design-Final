package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void signUpNewUser_returnsSuccessMessage() {
        HospitalController controller = new HospitalController();
        controller.init();

        HospitalController.SignUpRequest request = new HospitalController.SignUpRequest();
        request.username = "newpatient";
        request.fullName = "New Patient";
        request.password = "password123";

        ResponseEntity<HospitalController.ApiResponse> response = controller.signUp(request);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().success);
        assertEquals("Sign up successful. Welcome, New Patient!", response.getBody().message);
    }

    @Test
    void signUpDuplicateUsername_returnsBadRequest() {
        HospitalController controller = new HospitalController();
        controller.init();

        HospitalController.SignUpRequest request = new HospitalController.SignUpRequest();
        request.username = "edward"; // already created in init()
        request.fullName = "Edward";
        request.password = "password123";

        ResponseEntity<HospitalController.ApiResponse> response = controller.signUp(request);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().success);
        assertEquals("This username is already taken.", response.getBody().message);
    }
}
