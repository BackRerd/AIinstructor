package lnjz.backrer.aiinstructor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lnjz.backrer.aiinstructor.entity.ModelEndpoints;
import lnjz.backrer.aiinstructor.entity.Result;
import lnjz.backrer.aiinstructor.service.ModelEndpointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/model-endpoints")
public class ModelEndpointsController {

    @Autowired
    private ModelEndpointsService modelEndpointsService;

    /**
     * 新增模型端点
     */
    @PostMapping
    public Result<?> addModelEndpoint(@RequestBody ModelEndpoints modelEndpoints) {
        try {
            boolean success = modelEndpointsService.save(modelEndpoints);
            if (success) {
                return Result.success("新增成功", modelEndpoints);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            return Result.error("新增异常: " + e.getMessage());
        }
    }

    /**
     * 根据ID删除模型端点
     */
    @DeleteMapping("/{modelId}")
    public Result<?> deleteModelEndpoint(@PathVariable Integer modelId) {
        try {
            boolean success = modelEndpointsService.removeById(modelId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败，记录不存在");
            }
        } catch (Exception e) {
            return Result.error("删除异常: " + e.getMessage());
        }
    }

    /**
     * 批量删除模型端点
     */
    @DeleteMapping("/batch")
    public Result<?> batchDeleteModelEndpoints(@RequestBody List<Integer> modelIds) {
        try {
            boolean success = modelEndpointsService.removeByIds(modelIds);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除异常: " + e.getMessage());
        }
    }

    /**
     * 更新模型端点
     */
    @PutMapping
    public Result<?> updateModelEndpoint(@RequestBody ModelEndpoints modelEndpoints) {
        try {
            if (modelEndpoints.getModelId() == null) {
                return Result.error("ID不能为空");
            }
            boolean success = modelEndpointsService.updateById(modelEndpoints);
            if (success) {
                return Result.success("更新成功", modelEndpoints);
            } else {
                return Result.error("更新失败，记录不存在");
            }
        } catch (Exception e) {
            return Result.error("更新异常: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询模型端点
     */
    @GetMapping("/{modelId}")
    public Result<?> getModelEndpointById(@PathVariable Integer modelId) {
        try {
            ModelEndpoints modelEndpoints = modelEndpointsService.getById(modelId);
            if (modelEndpoints != null) {
                return Result.success("查询成功", modelEndpoints);
            } else {
                return Result.error("记录不存在");
            }
        } catch (Exception e) {
            return Result.error("查询异常: " + e.getMessage());
        }
    }

    /**
     * 分页查询模型端点
     */
    @GetMapping("/page")
    public Result<?> getModelEndpointsPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String modelType,
            @RequestParam(required = false) String mainUrl) {

        try {
            Page<ModelEndpoints> page = new Page<>(pageNum, pageSize);
            QueryWrapper<ModelEndpoints> queryWrapper = new QueryWrapper<>();

            // 添加查询条件
            if (modelType != null && !modelType.trim().isEmpty()) {
                queryWrapper.like("model_type", modelType.trim());
            }
            if (mainUrl != null && !mainUrl.trim().isEmpty()) {
                queryWrapper.like("main_url", mainUrl.trim());
            }

            // 按创建时间倒序排列
            queryWrapper.orderByDesc("created_at");

            IPage<ModelEndpoints> result = modelEndpointsService.page(page, queryWrapper);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            return Result.error("分页查询异常: " + e.getMessage());
        }
    }

    /**
     * 查询所有模型端点列表
     */
    @GetMapping("/list")
    public Result<?> getAllModelEndpoints() {
        try {
            QueryWrapper<ModelEndpoints> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("created_at");
            List<ModelEndpoints> list = modelEndpointsService.list(queryWrapper);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询异常: " + e.getMessage());
        }
    }

    /**
     * 根据模型类型查询
     */
    @GetMapping("/by-type/{modelType}")
    public Result<?> getModelEndpointsByType(@PathVariable String modelType) {
        try {
            QueryWrapper<ModelEndpoints> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_type", modelType);
            queryWrapper.orderByDesc("created_at");
            List<ModelEndpoints> list = modelEndpointsService.list(queryWrapper);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询异常: " + e.getMessage());
        }
    }
}