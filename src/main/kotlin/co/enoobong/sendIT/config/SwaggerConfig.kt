package co.enoobong.sendIT.config

import co.enoobong.sendIT.SendITApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig {
    private companion object {
        private fun generateApiInfo(): ApiInfo {
            return ApiInfo(
                "SendIT",
                "SendIT is a courier service that helps users deliver parcels to different destinations.",
                "1",
                "urn:tos",
                Contact("Ibanga Enoobong", "https://www.linkedin.com/in/ienoobong/", "ibangaenoobong@yahoo.com"),
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0",
                arrayListOf()
            )
        }
    }

    @Bean
    fun apiDoc(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .apiInfo(generateApiInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage(SendITApplication::class.java.`package`.name))
            .paths(PathSelectors.any())
            .build()
    }
}