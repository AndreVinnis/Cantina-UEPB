package com.uepb.OrderService.exception;

public class ServiceTimeOutException extends RuntimeException {
    public ServiceTimeOutException(String serviceName) {
        super("Houve um erro de comunicação com o serviço: " + serviceName);
    }
}
