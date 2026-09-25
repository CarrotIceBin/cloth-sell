package com.clothsell.module.mall.controller.admin.journal;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.dal.dataobject.journal.JournalDO;
import com.clothsell.module.mall.service.journal.JournalService;
import com.clothsell.module.mall.vo.journal.JournalPageReqVO;
import com.clothsell.module.mall.vo.journal.JournalRespVO;
import com.clothsell.module.mall.vo.journal.JournalSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 期刊")
@RestController
@RequestMapping("/mall/journal")
@Validated
public class JournalController {
    @Resource
    private JournalService journalService;

    @PostMapping("/create")
    @Operation(summary = "创建文章")
    @PreAuthorize("@ss.hasPermission('mall:journal:create')")
    public CommonResult<Long> createJournal(@Valid @RequestBody JournalSaveReqVO createReqVO) {
        return success(journalService.createJournal(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文章")
    @PreAuthorize("@ss.hasPermission('mall:journal:update')")
    public CommonResult<Boolean> updateJournal(@Valid @RequestBody JournalSaveReqVO updateReqVO) {
        journalService.updateJournal(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文章")
    @PreAuthorize("@ss.hasPermission('mall:journal:delete')")
    public CommonResult<Boolean> deleteJournal(@RequestParam("id") Long id) {
        journalService.deleteJournal(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "文章详情")
    @PreAuthorize("@ss.hasPermission('mall:journal:query')")
    public CommonResult<JournalRespVO> getJournal(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(journalService.getJournal(id), JournalRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "文章分页")
    @PreAuthorize("@ss.hasPermission('mall:journal:query')")
    public CommonResult<PageResult<JournalRespVO>> getJournalPage(@Valid JournalPageReqVO pageReqVO) {
        PageResult<JournalDO> page = journalService.getJournalPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream()
                .map(row -> BeanUtils.toBean(row, JournalRespVO.class)).toList(), page.getTotal()));
    }
}
