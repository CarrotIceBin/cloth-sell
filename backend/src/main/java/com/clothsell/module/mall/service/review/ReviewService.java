package com.clothsell.module.mall.service.review;

import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.module.mall.dal.dataobject.review.ReviewDO;
import com.clothsell.module.mall.vo.review.ReviewPageReqVO;
import com.clothsell.module.mall.vo.review.ReviewSaveReqVO;
import com.clothsell.module.mall.vo.review.ReviewSummaryRespVO;
import jakarta.validation.Valid;

public interface ReviewService {
    Long createReview(@Valid ReviewSaveReqVO createReqVO, Long userId, String author);

    void updatePublished(Long id, Boolean published);

    void deleteReview(Long id);

    PageResult<ReviewDO> getReviewPage(@Valid ReviewPageReqVO pageReqVO);

    ReviewSummaryRespVO getSummary(Long productId);
}
