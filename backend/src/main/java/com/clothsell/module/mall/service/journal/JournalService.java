package com.clothsell.module.mall.service.journal;

import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.module.mall.dal.dataobject.journal.JournalDO;
import com.clothsell.module.mall.vo.journal.JournalPageReqVO;
import com.clothsell.module.mall.vo.journal.JournalSaveReqVO;
import jakarta.validation.Valid;

public interface JournalService {
    Long createJournal(@Valid JournalSaveReqVO createReqVO);

    void updateJournal(@Valid JournalSaveReqVO updateReqVO);

    void deleteJournal(Long id);

    JournalDO getJournal(Long id);

    PageResult<JournalDO> getJournalPage(@Valid JournalPageReqVO pageReqVO);
}
