package net.zjvis.flint.data.hub.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.BasicController;
import net.zjvis.flint.data.hub.controller.doc.vo.ErrorVO;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/doc")
public class DocController {
    private final ApplicationContext context;

    public DocController(ApplicationContext context) {
        this.context = context;
    }

    @Operation(summary = "all error list")
    @PostMapping("/error/list")
    public StandardResponse<List<ErrorVO>> errorList() {
        Collection<Object> controllers = context
                .getBeansWithAnnotation(RestController.class)
                .values();
        List<ErrorVO> errorVOList = new ArrayList<>();
        List<BasicController> basicControllers = controllers
                .stream()
                .filter(controller -> controller instanceof BasicController)
                .map(controller -> (BasicController) controller)
                .collect(Collectors.toList());
        for (BasicController controller : basicControllers) {
            Class<?>[] innerClasses = controller.getClass().getDeclaredClasses();
            for (Class<?> innerClass : innerClasses) {
                String innerClassName = innerClass.getSimpleName();
                if (innerClassName.equals("ERROR") && innerClass.isEnum()) {
                    Class<Enum> enumClass = (Class<Enum>) innerClass;
                    Enum[] enumConstants = enumClass.getEnumConstants();
                    for (Enum enumConstant : enumConstants) {
                        BasicController.Error basicError = (BasicController.Error) enumConstant;
                        errorVOList.add(ErrorVO.builder()
                                .code(controller.errorResponse(basicError).getCode())
                                .message(basicError.getMessageTemplate())
                                .build());
                    }
                }
            }
        }
        return StandardResponse.<List<ErrorVO>>builder()
                .data(errorVOList)
                .build();
    }
}
