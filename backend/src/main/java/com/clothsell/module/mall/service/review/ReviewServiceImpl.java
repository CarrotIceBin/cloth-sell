package com.clothsell.module.mall.service.review;

import com.clothsell.framework.common.pojo.PageParam;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.module.mall.dal.dataobject.review.ReviewDO;
import com.clothsell.module.mall.dal.mysql.product.ProductMapper;
import com.clothsell.module.mall.dal.mysql.review.ReviewMapper;
import com.clothsell.module.mall.vo.review.ReviewPageReqVO;
import com.clothsell.module.mall.vo.review.ReviewSaveReqVO;
import com.clothsell.module.mall.vo.review.ReviewSummaryRespVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.REVIEW_EXISTS;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.REVIEW_NOT_EXISTS;

@Service
@Validated
public class ReviewServiceImpl implements ReviewService {
    @Resource
    private ReviewMapper reviewMapper;
    @Resource
    private ProductMapper productMapper;

    @Override
    public Long createReview(ReviewSaveReqVO createReqVO, Long userId, String author) {
        if (productMapper.selectById(createReqVO.getProductId()) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        if (reviewMapper.existsByProductAndUser(createReqVO.getProductId(), userId)) {
            throw exception(REVIEW_EXISTS);
        }
        ReviewDO row = new ReviewDO();
        row.setProductId(createReqVO.getProductId());
        row.setUserId(userId);
        row.setAuthor(mask(author));
        row.setRating(createReqVO.getRating());
        row.setContent(blank(createReqVO.getContent()));
        row.setPublished(true);
        reviewMapper.insert(row);
        return row.getId();
    }

    @Override
    public void updatePublished(Long id, Boolean published) {
        validateReviewExists(id);
        ReviewDO update = new ReviewDO();
        update.setId(id);
        update.setPublished(published);
        reviewMapper.updateById(update);
    }

    @Override
    public void deleteReview(Long id) {
        validateReviewExists(id);
        reviewMapper.deleteById(id);
    }

    @Override
    public PageResult<ReviewDO> getReviewPage(ReviewPageReqVO pageReqVO) {
        pageReqVO.toOrderBySql(PageParam.allow("createTime", "create_time", "id", "id"));
        return reviewMapper.selectPage(pageReqVO);
    }

    @Override
    public ReviewSummaryRespVO getSummary(Long productId) {
        List<ReviewDO> rows = reviewMapper.selectByProductId(productId);
        ReviewSummaryRespVO vo = new ReviewSummaryRespVO();
        for (int star = 5; star >= 1; star--) {
            vo.getStars().put(star, 0L);
        }
        long sum = 0;
        for (ReviewDO row : rows) {
            star(vo, row.getRating());
            sum += row.getRating();
        }
        vo.setTotal((long) rows.size());
        vo.setAverage(rows.isEmpty()
                ? BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(rows.size()), 1, RoundingMode.HALF_UP));
        return vo;
    }

    private void star(ReviewSummaryRespVO vo, Integer rating) {
        Long count = vo.getStars().get(rating);
        if (count != null) {
            vo.getStars().put(rating, count + 1);
        }
    }

    private void validateReviewExists(Long id) {
        if (id == null || reviewMapper.selectById(id) == null) {
            throw exception(REVIEW_NOT_EXISTS);
        }
    }
    private String mask(String value) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            return "匿名顾客";
        }
        if (text.length() == 11) {
            return text.substring(0, 3) + "****" + text.substring(7);
        }
        return text;
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
