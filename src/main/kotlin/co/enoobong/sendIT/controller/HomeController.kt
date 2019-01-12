package co.enoobong.sendIT.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import springfox.documentation.annotations.ApiIgnore


@ApiIgnore
@Controller
@RequestMapping("")
class HomeController {

    @GetMapping
    fun redirectHomeToDocumentation(): String {
        return "redirect:/swagger-ui.html"
    }
}