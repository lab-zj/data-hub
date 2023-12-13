package net.zjvis.flint.data.hub.service.graph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.zjvis.flint.data.hub.entity.graph.TableMap;
import net.zjvis.flint.data.hub.repository.graph.TableMapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TableMapService {

    public static final Logger LOGGER = LoggerFactory.getLogger(TableMapService.class);

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TableMapRepository tableMapRepository;

    public TableMapService(TableMapRepository tableMapRepository) {
        this.tableMapRepository = tableMapRepository;
    }

    public static String getFuzzyTableNameWithAlgorithm(Long taskId, String algorithmName) {
        return String.format("%d_%s", taskId, algorithmName);
    }

    public static String getFuzzyTableNameCommon(Long taskId) {
        return String.format("%d", taskId);
    }

    public static String getActiveTableNameWithAlgorithm(Long taskId, String algorithmName) {
        return String.format("%d_%s_%d", taskId, algorithmName, System.currentTimeMillis());
    }

    public static String getActiveTableNameCommon(Long taskId) {
        return String.format("%d_%d", taskId, System.currentTimeMillis());
    }

    public TableMap queryTableMapByFuzzyName(String name) {
        return tableMapRepository.findByFuzzyName(name);
    }

    public TableMap saveTableMap(TableMap tableMap) throws JsonProcessingException {
        LOGGER.info("[saveTableMap] start, tableMap={}",
            OBJECT_MAPPER.writeValueAsString(tableMap));
        TableMap existedTableMap = tableMapRepository.findByFuzzyName(tableMap.getFuzzyName());
        if (existedTableMap == null) {
            tableMap = tableMap.toBuilder()
                .gmtCreate(LocalDateTime.now())
                .gmtModify(LocalDateTime.now())
                .build();
        } else {
            tableMap = tableMap.toBuilder()
                .id(existedTableMap.getId())
                .gmtCreate(existedTableMap.getGmtCreate())
                .gmtModify(LocalDateTime.now())
                .build();
        }

        return tableMapRepository.save(tableMap);
    }


}
