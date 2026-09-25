package com.clothsell.module.mall.dal.dataobject.journal;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("mall_journal")
@Data
@EqualsAndHashCode(callSuper = true)
public class JournalDO extends BaseDO {
    @TableId
    private Long id;
    private String tag;
    private String title;
    private String summary;
    private String content;
    private String coverUrl;
    private Boolean published;
}
