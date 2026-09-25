package com.clothsell.module.mall.controller.client.journal;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.dal.dataobject.journal.JournalDO;
import com.clothsell.module.mall.service.journal.JournalService;
import com.clothsell.module.mall.vo.journal.JournalPageReqVO;
import com.clothsell.module.mall.vo.journal.JournalRespVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.framework.common.pojo.CommonResult.success;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.JOURNAL_NOT_EXISTS;

@Tag(name = "顾客端 - 期刊")
@RestController("clientJournalController")
@RequestMapping("/mall/client/journal")
public class JournalController {
    @Resource
    private JournalService journalService;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('mall:client-journal:query')")
    public CommonResult<PageResult<JournalRespVO>> getJournalPage(@Valid JournalPageReqVO pageReqVO) {
        pageReqVO.setPublished(true);
        PageResult<JournalDO> page = journalService.getJournalPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream().map(this::toVo).toList(), page.getTotal()));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('mall:client-journal:query')")
    public CommonResult<JournalRespVO> getJournal(@RequestParam("id") Long id) {
        JournalDO row = journalService.getJournal(id);
        if (!Boolean.TRUE.equals(row.getPublished())) {
            throw exception(JOURNAL_NOT_EXISTS);
        }
        return success(toVo(row));
    }

    private JournalRespVO toVo(JournalDO row) {
        return BeanUtils.toBean(row, JournalRespVO.class);
    }
}
