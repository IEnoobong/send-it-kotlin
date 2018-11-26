package co.enoobong.sendIT.config

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method

@Configuration
class WebConfig {

    @Bean
    fun webMvcRegistrationsHandlerMapping(): WebMvcRegistrations {
        val apiBasePath = "api"
        return object : WebMvcRegistrations {
            override fun getRequestMappingHandlerMapping(): RequestMappingHandlerMapping {
                return object : RequestMappingHandlerMapping() {
                    override fun registerHandlerMethod(handler: Any, method: Method, mapping: RequestMappingInfo) {
                        val beanType = method.declaringClass
                        var newMapping = mapping
                        if (AnnotationUtils.findAnnotation(beanType, RestController::class.java) != null) {
                            val apiPattern = PatternsRequestCondition(apiBasePath)
                                .combine(mapping.patternsCondition)

                            newMapping = RequestMappingInfo(
                                mapping.name, apiPattern,
                                mapping.methodsCondition, mapping.paramsCondition,
                                mapping.headersCondition, mapping.consumesCondition,
                                mapping.producesCondition, mapping.customCondition
                            )
                        }

                        super.registerHandlerMethod(handler, method, newMapping)
                    }
                }
            }
        }
    }
}