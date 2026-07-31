package com.uepb.OrderService.integration;

import com.uepb.OrderService.dto.request.OrderItemRequest;
import com.uepb.OrderService.exception.ServiceTimeOutException;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class CoreServiceFallbackFactory implements FallbackFactory<CoreService> {

    @Override
    public CoreService create(Throwable cause) {
        return new CoreService() {

            @Override
            public List<ItemValidatedResponse> validateOrderItems(String id, List<OrderItemRequest> orderRequest) {
                Throwable rootCause = cause.getCause() != null ? cause.getCause() : cause;

                if (rootCause instanceof TimeoutException || rootCause.getMessage().toLowerCase().contains("timeout")) {
                    throw new ServiceTimeOutException("Core Service");
                }

                if (cause instanceof FeignException feignEx) {
                    if (feignEx.status() == 400) {
                        throw new RuntimeException("Ocorreu um erro no serviço Core. " + feignEx.getMessage());
                    }
                }

                throw new RuntimeException("Erro de comunicação com o CoreService", cause);
            }

            @Override
            public Void decrementsStock(String id, List<OrderItemRequest> orderRequest) {
                Throwable rootCause = cause.getCause() != null ? cause.getCause() : cause;

                if (rootCause instanceof TimeoutException || rootCause.getMessage().toLowerCase().contains("timeout")) {
                    throw new ServiceTimeOutException("Core Service");
                }

                if (cause instanceof FeignException feignEx) {
                    if (feignEx.status() == 400) {
                        throw new RuntimeException("Ocorreu um erro no serviço Core. " + feignEx.getMessage());
                    }
                }

                throw new RuntimeException("Erro de comunicação com o CoreService", cause);
            }

            @Override
            public String getIdCafeteria() {
                Throwable rootCause = cause.getCause() != null ? cause.getCause() : cause;

                if (rootCause instanceof TimeoutException || rootCause.getMessage().toLowerCase().contains("timeout")) {
                    throw new ServiceTimeOutException("Core Service");
                }

                if (cause instanceof FeignException feignEx) {
                    if (feignEx.status() == 404) {
                        throw new RuntimeException("Ocorreu um erro no serviço Core. " + feignEx.getMessage());
                    }
                }

                throw new RuntimeException("Erro de comunicação com o CoreService", cause);
            }
        };
    }
}
