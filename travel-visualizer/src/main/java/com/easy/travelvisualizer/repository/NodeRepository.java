package com.easy.travelvisualizer.repository;

import com.easy.travelvisualizer.domain.Node;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NodeRepository extends CrudRepository<Node, Long> {
    Optional<Node> findByCurrentCity(String currentCity);

}
