package it.gov.pagopa.payment.notices.service.events;

import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestEH;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

@Service
public class NoticeGenerationRequestProducerImpl implements NoticeGenerationRequestProducer {

    private final StreamBridge streamBridge;

    public NoticeGenerationRequestProducerImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /** Declared just to let know Spring to connect the producer at startup */
    @Slf4j
    @Configuration
    static class NoticeGenerationRequestProducerConfig {

        @Bean
        public Supplier<Flux<Message<NoticeGenerationRequestEH>>> noticeGeneration() {
            return Flux::empty;
        }

    }

    @Override
    public boolean noticeGeneration(NoticeGenerationRequestEH noticeGenerationRequestEH) {
        return streamBridge.send("noticeGeneration-out-0",
                buildMessage(noticeGenerationRequestEH));
    }

    public static Message<NoticeGenerationRequestEH> buildMessage(
            NoticeGenerationRequestEH noticeGenerationRequestEH){
        return MessageBuilder.withPayload(noticeGenerationRequestEH).build();
    }

}
