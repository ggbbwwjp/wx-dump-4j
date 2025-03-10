package com.xcs.wx.mapping;

import com.xcs.wx.domain.Msg;
import com.xcs.wx.domain.vo.ExportMsgVO;
import com.xcs.wx.domain.vo.MatchMsgVO;
import com.xcs.wx.domain.vo.MsgVO;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 消息 Mapping
 *
 * @author xcs
 * @date 2023年12月21日 18时46分
 **/
@Mapper(componentModel = "spring")
public interface MsgMapping {

    /**
     * 参数转换
     *
     * @param msgs 消息
     * @return MsgDTO
     */
    List<MsgVO> convert(List<Msg> msgs);

    /**
     * 参数转换
     *
     * @param msgVOList 参数
     * @return ExportMsgVO
     */
    @Mapping(target = "strDevLang", expression = "java(\"Unknown Language\")")
    @Mapping(target = "strMsgType", expression = "java(\"Default Type\")")
    List<ExportMsgVO> convertToExportMsgVO(List<MsgVO> msgVOList);
    

    ExportMsgVO msgVOToExportMsgVO(MsgVO msgVO) ;
    @AfterMapping
    default void setAdditionalFields(@MappingTarget ExportMsgVO exportMsgVO, MsgVO msgVO) {
        //exportMsgVO.setStrContent( msgVO.getStrContent() );
        Pattern pattern = Pattern.compile("\\bjava|SQL\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(msgVO.getStrContent());
        if (matcher.find()) {  // 使用 find() 而不是 matches()，因为正则是断言，不匹配整个字符串
            exportMsgVO.setStrDevLang("JAVA");
        } else {
        	exportMsgVO.setStrDevLang("");
        }
        Pattern pattern2 = Pattern.compile("募集|急募|急聘|要員(交代|替换)|[\\p{L}\\p{N}]{1,10}案件|件(概要|名)|(找|求|招|聘|需|要).*?(人|員|名|手|者|SE|PG|PM|PL)|(人|員|名|手|者|SE|PG|PM|PL)有(么|吗)|必須スキル|必須条件|スキル要件|期.*?間|场.*?所|場.*?所|面.*?談[0-9０-９]回|要日语|(?<!希望)(基本在宅|半在宅|リモート頻度)|外籍可", Pattern.CASE_INSENSITIVE);
        Matcher matcher2 = pattern2.matcher(msgVO.getStrContent());
        Pattern pattern3 = Pattern.compile("(営|营|稼动).*?(人|員|名|手|者|SE|PG|PM|PL)|(人|員|名|手|者|SE|PG|PM|PL)[0-9０-９]+|月稼働|(求|找|合适).*?(案件|現場|现场)|氏.*?名|性.*?別|(?=.*(歳|岁|年))(?=.*(男|女|籍))", Pattern.CASE_INSENSITIVE);
        Matcher matcher3 = pattern3.matcher(msgVO.getStrContent());
        if (matcher2.find()) {
        	exportMsgVO.setStrMsgType("案件");
        }else if (matcher3.find()) {
        	exportMsgVO.setStrMsgType("要員");
        }else if (matcher2.find() && matcher3.find()) {  // 使用 find() 而不是 matches()，因为正则是断言，不匹配整个字符串
            exportMsgVO.setStrMsgType("案件・要員");
        } else {
        	exportMsgVO.setStrMsgType("その他");
        }
    }
    
    /**
     * 参数转换
     *
     * @param msgVOList 参数
     * @return ExportMsgVO
     */
    //List<ExportMsgVO> convertToMatchMsgVO(List<ExportMsgVO> exptMsgVOListAnken,List<ExportMsgVO> exptMsgVOListYouin);
        
}
