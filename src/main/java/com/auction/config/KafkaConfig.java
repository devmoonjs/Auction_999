package com.auction.config;

import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.coupon.dto.CouponClaimMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.backoff.FixedBackOff;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableTransactionManagement
public class KafkaConfig {

    @Value("${kafka.producer.bootstrap-servers}")
    private String kafkaServer;

    // 공통 ProducerFactory
    private <T> ProducerFactory<String, T> createProducerFactory(Class<T> valueType) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.RoundRobinPartitioner");
        configProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "transactional-id");  // 원자성 위해 transaction id 추가

        // 모든 레플리카에서 전송 완료 시 ack
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // 공통 KafkaTemplate 빈
    private <T> KafkaTemplate<String, T> createKafkaTemplate(Class<T> valueType) {
        return new KafkaTemplate<>(createProducerFactory(valueType));
    }

    // 쿠폰용 KafkaTemplate 빈 정의
    @Bean
    public KafkaTemplate<String, CouponClaimMessage> couponKafkaTemplate() {
        KafkaTemplate<String, CouponClaimMessage> kafkaTemplate = createKafkaTemplate(CouponClaimMessage.class);
        kafkaTemplate.setTransactionIdPrefix("coupon-");
        return kafkaTemplate;
    }

    // 환불용 KafkaTemplate 빈 정의
    @Bean
    public KafkaTemplate<String, RefundEvent> refundKafkaTemplate() {
        KafkaTemplate<String, RefundEvent> kafkaTemplate = createKafkaTemplate(RefundEvent.class);
        kafkaTemplate.setTransactionIdPrefix("refund-");    // 트랜잭션 id 접두사 설정
        return kafkaTemplate;
    }

    @Bean
    public KafkaTemplate<Object, Object> generalKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs()));
    }

    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.RoundRobinPartitioner");
        return props;
    }



    // 공통 ConsumerFactory
    private <T> ConsumerFactory<String, T> createConsumerFactory(String groupId, Class<T> valueType) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.auction.domain.coupon.dto, com.auction.domain.auction.event.dto");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    // 공통 KafkaListenerContainerFactory 빈
    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createKafkaListenerContainerFactory(
            ConsumerFactory<String, T> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    // 쿠폰용 KafkaListenerContainerFactory 빈 정의
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CouponClaimMessage> kafkaListenerContainerFactory() {
        return createKafkaListenerContainerFactory(createConsumerFactory("couponGroup", CouponClaimMessage.class));
    }

    // 환불용 KafkaListenerContainerFactory 빈 정의
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefundEvent> refundKafkaListenerContainerFactory(KafkaTemplate<Object, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, RefundEvent> factory =  createKafkaListenerContainerFactory(createConsumerFactory("refundGroup", RefundEvent.class));
        factory.setCommonErrorHandler(refundErrorHandler(kafkaTemplate));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }

    @Bean
    public DefaultErrorHandler refundErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (consumerRecord, exception) -> new TopicPartition(consumerRecord.topic() + ".DLT", consumerRecord.partition()));

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 5));

        // Point 서버 접속 오류 시 DLT 전송
        errorHandler.addNotRetryableExceptions(ConnectException.class);

        return errorHandler;
    }
}
