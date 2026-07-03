package com.uepb.CoreService.repository;

import com.uepb.CoreService.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, String> {

    Optional<MenuItem> findByCafeteriaIdAndName(String cafeteriaId, String name);

    List<MenuItem> findByCafeteriaId(String cafeteriaId);
}
