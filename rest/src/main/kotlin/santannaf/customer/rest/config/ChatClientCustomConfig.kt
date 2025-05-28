package santannaf.customer.rest.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import santannaf.demo.analyse.queries.analysequeries.interceptor.R2dbcLoggingConnectionFactory
import santannaf.demo.analyse.queries.analysequeries.support.QueryQueue

@Configuration
class ChatClientCustomConfig {

    @Bean
    fun chatClient(builder: ChatClient.Builder) : ChatClient = builder.build()

//    @Bean
//    fun connectionFactory(baseFactory: ConnectionFactory, queue: QueryQueue): ConnectionFactory {
//        return R2dbcLoggingConnectionFactory(baseFactory, queue)
//    }
}
