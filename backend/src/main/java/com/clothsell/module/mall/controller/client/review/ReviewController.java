package com.clothsell.module.mall.controller.client.review;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.dal.dataobject.review.ReviewDO;
import com.clothsell.module.mall.service.review.ReviewService;
import com.clothsell.module.mall.vo.review.ReviewPageReqVO;
import com.clothsell.module.mall.vo.review.ReviewRespVO;
import com.clothsell.module.mall.vo.review.ReviewSaveReqVO;
import com.clothsell.module.mall.vo.review.ReviewSummaryRespVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.framework.common.pojo.CommonResult.success;
import static com.clothsell.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.clothsell.framework.security.core.util.SecurityFrameworkUtils.getUserName;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.LOGIN_BAD;

@Tag(name = "顾客端 - 商品评价")
@RestController("clientReviewController")
@RequestMapping("/mall/client/review")
public class ReviewController {
    @Resource
    private ReviewService reviewService;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('mall:client-review:query')")
    public CommonResult<PageResult<ReviewRespVO>> getReviewPage(@Valid ReviewPageReqVO pageReqVO) {
        pageReqVO.setPublished(true);
        PageResult<ReviewDO> page = reviewService.getReviewPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream().map(this::toVo).toList(), page.getTotal()));
    }

    @GetMapping("/summary")
    @PreAuthorize("@ss.hasPermission('mall:client-review:query')")
    public CommonResult<ReviewSummaryRespVO> getSummary(@RequestParam("productId") Long productId) {
        return success(reviewService.getSummary(productId));
    }

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('mall:client-review:create')")
    public CommonResult<Long> createReview(@Valid @RequestBody ReviewSaveReqVO createReqVO) {
        Long userId = getLoginUserId();
        if (userId == null) {
            throw exception(LOGIN_BAD);
        }
        return success(reviewService.createReview(createReqVO, userId, getUserName()));
    }

    private ReviewRespVO toVo(ReviewDO row) {
        return BeanUtils.toBean(row, ReviewRespVO.class);
    }
}
