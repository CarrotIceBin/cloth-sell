package com.clothsell.module.mall.controller.admin.review;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.dal.dataobject.review.ReviewDO;
import com.clothsell.module.mall.service.review.ReviewService;
import com.clothsell.module.mall.vo.review.ReviewPageReqVO;
import com.clothsell.module.mall.vo.review.ReviewRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 商品评价")
@RestController
@RequestMapping("/mall/review")
@Validated
public class ReviewController {
    @Resource
    private ReviewService reviewService;

    @GetMapping("/page")
    @Operation(summary = "评价分页")
    @PreAuthorize("@ss.hasPermission('mall:review:query')")
    public CommonResult<PageResult<ReviewRespVO>> getReviewPage(@Valid ReviewPageReqVO pageReqVO) {
        PageResult<ReviewDO> page = reviewService.getReviewPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream()
                .map(row -> BeanUtils.toBean(row, ReviewRespVO.class)).toList(), page.getTotal()));
    }

    @PutMapping("/update-published")
    @Operation(summary = "显示或隐藏评价")
    @PreAuthorize("@ss.hasPermission('mall:review:update')")
    public CommonResult<Boolean> updatePublished(@RequestParam("id") Long id,
                                                 @RequestParam("published") Boolean published) {
        reviewService.updatePublished(id, published);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除评价")
    @PreAuthorize("@ss.hasPermission('mall:review:delete')")
    public CommonResult<Boolean> deleteReview(@RequestParam("id") Long id) {
        reviewService.deleteReview(id);
        return success(true);
    }
}
