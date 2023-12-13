package net.zjvis.flint.data.hub.service.graph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.zjvis.flint.data.hub.entity.graph.GraphMap;
import net.zjvis.flint.data.hub.repository.graph.GraphMapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GraphMapService {

    public static final Logger LOGGER = LoggerFactory.getLogger(GraphMapService.class);

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GraphMapRepository graphMapRepository;

    public GraphMapService(GraphMapRepository graphMapRepository) {
        this.graphMapRepository = graphMapRepository;
    }

    public GraphMap queryGraphMapByGraphUuid(String graphUid) {
        return graphMapRepository.findByGraphUuid(graphUid);
    }

    public GraphMap saveGraphMap(GraphMap graphMap) throws JsonProcessingException {
        LOGGER.info("[saveGraphMap] start, graph map={}",
            OBJECT_MAPPER.writeValueAsString(graphMap));
        GraphMap existedGraphMap = graphMapRepository.findByGraphUuid(graphMap.getGraphUuid());
        if (existedGraphMap == null) {
            graphMap = graphMap.toBuilder()
                .gmtCreate(LocalDateTime.now())
                .gmtModify(LocalDateTime.now())
                .build();
        } else {
            graphMap = graphMap.toBuilder()
                .id(existedGraphMap.getId())
                .gmtModify(LocalDateTime.now())
                .build();
        }
        return graphMapRepository.save(graphMap);
    }


}
