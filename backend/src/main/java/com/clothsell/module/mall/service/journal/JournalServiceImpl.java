package com.clothsell.module.mall.service.journal;

import com.clothsell.framework.common.pojo.PageParam;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.module.mall.dal.dataobject.journal.JournalDO;
import com.clothsell.module.mall.dal.mysql.journal.JournalMapper;
import com.clothsell.module.mall.service.product.ProductServiceImpl;
import com.clothsell.module.mall.vo.journal.JournalPageReqVO;
import com.clothsell.module.mall.vo.journal.JournalSaveReqVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.COVER_BAD;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.JOURNAL_NOT_EXISTS;

@Service
@Validated
public class JournalServiceImpl implements JournalService {
    @Resource
    private JournalMapper journalMapper;

    @Override
    public Long createJournal(JournalSaveReqVO createReqVO) {
        JournalDO row = new JournalDO();
        row.setTag(createReqVO.getTag().trim());
        row.setTitle(createReqVO.getTitle().trim());
        row.setSummary(blank(createReqVO.getSummary()));
        row.setContent(blank(createReqVO.getContent()));
        row.setCoverUrl(cover(createReqVO.getCoverUrl()));
        row.setPublished(createReqVO.getPublished());
        journalMapper.insert(row);
        return row.getId();
    }

    @Override
    public void updateJournal(JournalSaveReqVO updateReqVO) {
        validateJournalExists(updateReqVO.getId());
        JournalDO update = new JournalDO();
        update.setId(updateReqVO.getId());
        update.setTag(updateReqVO.getTag().trim());
        update.setTitle(updateReqVO.getTitle().trim());
        update.setSummary(blank(updateReqVO.getSummary()));
        update.setContent(blank(updateReqVO.getContent()));
        update.setCoverUrl(cover(updateReqVO.getCoverUrl()));
        update.setPublished(updateReqVO.getPublished());
        journalMapper.updateById(update);
    }

    @Override
    public void deleteJournal(Long id) {
        validateJournalExists(id);
        journalMapper.deleteById(id);
    }

    @Override
    public JournalDO getJournal(Long id) {
        JournalDO row = journalMapper.selectById(id);
        if (row == null) {
            throw exception(JOURNAL_NOT_EXISTS);
        }
        return row;
    }

    @Override
    public PageResult<JournalDO> getJournalPage(JournalPageReqVO pageReqVO) {
        pageReqVO.toOrderBySql(PageParam.allow("createTime", "create_time", "id", "id"));
        return journalMapper.selectPage(pageReqVO);
    }

    private void validateJournalExists(Long id) {
        if (id == null || journalMapper.selectById(id) == null) {
            throw exception(JOURNAL_NOT_EXISTS);
        }
    }

    private String cover(String value) {
        String url = blank(value);
        if (url == null) {
            return null;
        }
        if (ProductServiceImpl.safeCover(url) == null) {
            throw exception(COVER_BAD);
        }
        return url;
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
