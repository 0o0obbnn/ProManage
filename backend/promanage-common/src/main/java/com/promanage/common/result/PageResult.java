package com.promanage.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类
 * <p>
 * 用于封装分页查询的分页数据，保持与API的统一结构。
 * </p>
 *
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> list;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Long total;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer page;

    /**
     * 每页记录数
     */
    @Schema(description = "每页记录数", example = "20")
    private Integer pageSize;

    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "5")
    private Integer totalPages;

    /**
     * 是否有下一页
     */
    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrevious;

    /**
     * 构建分页结果
     *
     * @param list     数据列表
     * @param total    总记录数
     * @param page     当前页码
     * @param pageSize 每页记录数
     * @param <T>      数据类型
     * @return PageResult
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer pageSize) {
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) total / pageSize);

        return PageResult.<T>builder()
                .list(list)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .hasNext(page < totalPages)
                .hasPrevious(page > 1)
                .build();
    }

    /**
     * 空分页结果
     *
     * @param <T> 数据类型
     * @return PageResult
     */
    public static <T> PageResult<T> empty() {
        return PageResult.<T>builder()
                .list(List.of())
                .total(0L)
                .page(1)
                .pageSize(20)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}