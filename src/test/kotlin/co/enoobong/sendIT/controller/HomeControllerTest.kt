package co.enoobong.sendIT.controller

import co.enoobong.sendIT.config.ControllerTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@ControllerTest
@WebMvcTest(value = [HomeController::class])
class HomeControllerTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `home should redirect to documentation`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isFound)
            .andExpect(view().name("redirect:/swagger-ui.html"))
            .andExpect(redirectedUrl("/swagger-ui.html"))
    }
}