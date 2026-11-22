package com.example.task.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleValidation(IllegalArgumentException ex) {
        log.warn("Requisição inválida: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Dados inválidos");
        return problem;
    }

    // Como a publicação acontece dentro da transação, falha no broker desfaz a gravação da tarefa
    @ExceptionHandler(AmqpException.class)
    public ProblemDetail handleBrokerUnavailable(AmqpException ex) {
        log.error("Falha ao publicar evento no RabbitMQ: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível publicar o evento no broker. A operação não foi gravada.");
        problem.setTitle("Broker indisponível");
        return problem;
    }
}
