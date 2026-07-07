package com.uepb.CoreService.exceptions;

public class NoCafeteriaFound extends RuntimeException {
    public NoCafeteriaFound() {
        super("Nenhuma lanchonete foi encontrada");
    }
}
