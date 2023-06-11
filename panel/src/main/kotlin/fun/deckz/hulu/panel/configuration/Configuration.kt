package `fun`.deckz.hulu.panel.configuration

import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter


@Configuration
class Configuration {

    @Bean
    fun jsonConverter(): KotlinSerializationJsonHttpMessageConverter {
        return KotlinSerializationJsonHttpMessageConverter(
            Json {
                encodeDefaults = true
            }
        )
    }

}