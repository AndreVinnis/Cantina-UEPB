package com.uepb.OrderService.repository;

import com.uepb.OrderService.domain.Order;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByCafeteriaId(String cafeteriaId);

    List<Order> findByClientCpf(String cpf);

    Optional<Order> findBySessionToken(String sessionToken);
}
