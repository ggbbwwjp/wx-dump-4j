package com.xcs.wx.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import lombok.Data;

/**
 * ExportMsgVO
 *
 * @author xcs
 * @date 2024年01月25日 16时1ExptMsgVO6分
 **/
@Data
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
public class MatchMsgVO {

    /**
     * 消息服务器 ID
     */
    @ColumnWidth(25)
    @ExcelProperty("消息Id")
    private String msgSvrId;
    
 // KAKU　DEL ST 2024/10/17
//    /**
//     * 聊天人Id
//     */
//    @ColumnWidth(25)
//    @ExcelProperty("聊天人Id")
//    private String wxId;
 // KAKU　DEL ED 2024/10/17  
    /**
     * 聊天者字符串  KAKU　ADD 2024/10/17 
     */
    @ColumnWidth(25)   /*KAKU　ADD 2024/10/17*/
    @ExcelProperty("聊天者字符串")   /*KAKU　ADD 2024/10/17*/
    private String strTalker;     /*KAKU　ADD 2024/10/17*/
// KAKU　DEL ST 2024/10/17
//    /**
//     * 消息类型
//     */
//    @ColumnWidth(10)   
//    @ExcelProperty("类型")
//    private Integer type;
//
//    /**
//     * 消息子类型
//     */
//    @ColumnWidth(10)
//    @ExcelProperty("子类型")
//    private Integer subType;
//
//    /**
//     * 是否为发送者
//     */
//    @ColumnWidth(15)
//    @ExcelProperty("是否发送者")
//    private Integer isSender;
 // KAKU　DEL ED 2024/10/17
    /**
     * 消息创建时间
     */
    @ColumnWidth(25)
    @ExcelProperty("创建时间")
    private String strCreateTime;
    
    /**
     * 本地唯一标识符，自增 No
     */
    @ColumnWidth(10)
    @ExcelProperty("No")
    private Integer localId;
    
    /**
     * 消息内容字符串
     */
    @ColumnWidth(80)
    @ExcelProperty("消息内容")
    private String strContent;
    
    /**
     * 消息种类
     */
    @ColumnWidth(25)
    @ExcelProperty("消息种类")
    private String strMsgType;
    /**
     * 开发语言
     */
    @ColumnWidth(25)
    @ExcelProperty("开发语言")
    private String strDevLang;
}
