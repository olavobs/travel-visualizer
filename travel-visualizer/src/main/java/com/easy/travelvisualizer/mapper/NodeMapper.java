package com.easy.travelvisualizer.mapper;

import com.easy.travelvisualizer.config.MapStructConfig;
import com.easy.travelvisualizer.domain.Node;
import com.easy.travelvisualizer.dto.NodeRequest;
import com.easy.travelvisualizer.dto.NodeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(config = MapStructConfig.class)
public interface NodeMapper {

    @Mapping(target = "price", source = "nodeRequest.price")
    @Mapping(target = "startMoment", source = "nodeRequest.startMoment")
    @Mapping(target = "endMoment", source = "nodeRequest.endMoment")
    @Mapping(target = "currency", source = "nodeRequest.currency")
    @Mapping(target = "transportCompanyName", source = "nodeRequest.transportCompanyName")
    @Mapping(target = "departurePlace", source = "nodeRequest.departurePlace")
    @Mapping(target = "arrivalPlace", source = "nodeRequest.arrivalPlace")
    @Mapping(target = "transportType", source = "nodeRequest.transportType")
    @Mapping(target = "currentCity", source = "nodeRequest.currentCity")
    @Mapping(target = "previousNode", source = "node")
    @Mapping(target = "id", ignore = true)
    Node fromNodeRequest(NodeRequest nodeRequest, Node node);

    @Mapping(target = "pricePath", expression = "java(calculatePricePath(node))")
    @Mapping(target = "previousCity", expression = "java(getPreciousCity(node))")
    NodeResponse fromNode(Node node);

    default String getPreciousCity(Node node) {
        if (node.getPreviousNode() != null) {
            return node.getPreviousNode().getCurrentCity();
        }
        return null;
    }

    default BigDecimal calculatePricePath(Node node) {
        if (node == null || node.getPrice() == null) {
            return BigDecimal.ZERO;
        } else {
            BigDecimal currentPrice = node.getPrice();
            BigDecimal previousPrices = calculatePricePath(node.getPreviousNode());
            return currentPrice.add(previousPrices);
        }
    }
}
