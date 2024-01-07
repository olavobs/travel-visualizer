package com.easy.travelvisualizer.service;

import com.easy.travelvisualizer.domain.Node;
import com.easy.travelvisualizer.dto.NodeRequest;
import com.easy.travelvisualizer.dto.NodeResponse;
import com.easy.travelvisualizer.dto.UpdateNodeRequest;
import com.easy.travelvisualizer.mapper.NodeMapper;
import com.easy.travelvisualizer.repository.NodeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NodeService {

    private final NodeRepository nodeRepository;
    private final NodeMapper nodeMapper;

    public List<NodeResponse> findAll() {
        List<Node> all = (List<Node>) nodeRepository.findAll();
        return all.stream().map(nodeMapper::fromNode).toList();
    }

    public void create(NodeRequest nodeRequest) {
        Node previousNode = null;
        if (nodeRequest.previousCity() != null) {
            Optional<Node> maybePreviousCity = findById(nodeRequest.previousCity());
            if (maybePreviousCity.isPresent()) {
                previousNode = maybePreviousCity.get();
            }
        }
        Node node = nodeMapper.fromNodeRequest(nodeRequest, previousNode);
        nodeRepository.save(node);
    }

    private Optional<Node> findById(Long previousCity) {
        return nodeRepository.findById(previousCity);
    }

    public void update(UpdateNodeRequest newNode) {
        Optional<Node> maybeNode = nodeRepository.findById(newNode.currentNodeId());

        if (maybeNode.isPresent()) {
            Node node = maybeNode.get();

            Field[] fields = UpdateNodeRequest.class.getDeclaredFields();

            for (Field field : fields) {
                try {
                    Object value = field.get(newNode);

                    if (value != null) {
                        Field nodeField = Node.class.getDeclaredField(field.getName());
                        nodeField.setAccessible(true);
                        nodeField.set(node, value);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            nodeRepository.save(node);
        } else {
            throw new EntityNotFoundException("Node not found for ID: " + newNode.currentNodeId());
        }
    }
}
