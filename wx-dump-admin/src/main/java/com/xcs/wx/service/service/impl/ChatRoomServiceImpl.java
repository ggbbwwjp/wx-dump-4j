package com.xcs.wx.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xcs.wx.domain.ChatRoomInfo;
import com.xcs.wx.domain.Msg;
import com.xcs.wx.domain.dto.ChatRoomDTO;
import com.xcs.wx.domain.vo.*;
import com.xcs.wx.mapping.ChatRoomMapping;
import com.xcs.wx.mapping.MsgMapping;
import com.xcs.wx.msg.MsgStrategy;
import com.xcs.wx.msg.MsgStrategyFactory;
import com.xcs.wx.protobuf.ChatRoomProto;
import com.xcs.wx.repository.ChatRoomInfoRepository;
import com.xcs.wx.repository.ChatRoomRepository;
import com.xcs.wx.repository.ContactHeadImgUrlRepository;
import com.xcs.wx.repository.ContactRepository;
import com.xcs.wx.repository.MsgRepository;
import com.xcs.wx.service.ChatRoomService;
import com.xcs.wx.util.DirUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * 群聊服务实现类
 *
 * @author xcs
 * @date 2023年12月31日18:18:58
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ContactRepository contactRepository;
    private final ChatRoomInfoRepository chatRoomInfoRepository;
    private final ContactHeadImgUrlRepository contactHeadImgUrlRepository;
    private final MsgRepository msgRepository;
    private final MsgMapping msgMapping;
    private final ChatRoomMapping chatRoomMapping;
    
    @Override
    public PageVO<ChatRoomVO> queryChatRoom(ChatRoomDTO chatRoomDTO) {
    	
    	List<Msg> msgList = msgRepository.exportMsg("talker", chatRoomDTO);
        // 根据时间排序
        AtomicInteger indexNo = new AtomicInteger(1); // KAKU ADD 20241026
        
        List<ExportChatRoomVO> exportChatRoomVOS = chatRoomRepository.exportChatRoom(); // KAKU ADD 20241026
         
        
        List<MsgVO> msgVOList = msgMapping.convert(msgList).stream().sorted(Comparator.comparing(MsgVO::getCreateTime))
                // 遍历数据
                .peek(msgVO -> {
                    msgVO.setLocalId(indexNo.getAndIncrement());  // KAKU ADD 20241026
                    
                    String ChatRoomTitle  = ""; // KAKU ADD 20241026
                    Optional<ExportChatRoomVO> resultCtrm = exportChatRoomVOS.stream() // KAKU ADD 20241026
                    .filter(ExportChatRoomVO -> ExportChatRoomVO.getChatRoomName().equals(msgVO.getStrTalker())) // KAKU ADD 20241026
                    .findFirst();
                    ChatRoomTitle  = resultCtrm.get().getChatRoomTitle();
                     
                    msgVO.setMsgSvrId(ChatRoomTitle);
                    //msgVO.setWxId(getChatWxId("talker", msgVO));

                    // 设置处理日期
                    msgVO.setStrCreateTime(DateUtil.formatDateTime(new Date(msgVO.getCreateTime() * 1000)));
                    // 读取消息类型策略
                    MsgStrategy strategy = MsgStrategyFactory.getStrategy(msgVO.getType(), msgVO.getSubType());
                    // 根据对应的策略进行处理
                    if (strategy != null) {
                        strategy.process(msgVO);
                    }
                }).collect(Collectors.toList());
        // 聊天人的昵称
        String nickname = contactRepository.getContactNickname("talker");
        // 分隔符
        String separator = FileSystems.getDefault().getSeparator();
        // 文件路径
        String filePath = System.getProperty("user.dir") + separator + "data" + separator + "export";
        // 创建文件
        FileUtil.mkdir(filePath);
        // 文件路径+文件名
        String pathName = filePath + separator + "全IT群聊天记录" +DateUtil.currentSeconds()+ ".xlsx";
        // 导出
        List<ExportMsgVO> exportMsgVOList =msgMapping.convertToExportMsgVO(msgVOList);
        
        try (var writer = EasyExcel.write(pathName, ExportMsgVO.class).build()) {
            // シート1: 「情報」
            WriteSheet sheet1 = EasyExcel.writerSheet(0, "情報").build();
            writer.write(exportMsgVOList, sheet1);

            // シート2: 「マッチ」
            WriteSheet sheet2 = EasyExcel.writerSheet(1, "マッチ").build();
            writer.write(exportMsgVOList, sheet2);
        }
   //     EasyExcel.write(pathName, ExportMsgVO.class).sheet("情報").doWrite(() -> ExportMsgVOList);
    //    EasyExcel.write(pathName, ExportMsgVO.class).sheet("マッチ").doWrite(() -> ExportMsgVOList);
        // 返回写入后的文件
        //return pathName;
    	
    	
    	
        // 查询群聊
        return Opt.ofNullable(chatRoomRepository.queryChatRoom(chatRoomDTO))
                // 设置群聊人数
                .map(page -> {
                    for (ChatRoomVO chatRoom : page.getRecords()) {
                        chatRoom.setMemberCount(handleMembersCount(chatRoom.getRoomData()));
                    }
                    return page;
                })
                // 处理头像为空问题
                .map(page -> {
                    for (ChatRoomVO chatRoom : page.getRecords()) {
                        // 如果有头像则不处理
                        if (!StrUtil.isBlank(chatRoom.getHeadImgUrl())) {
                            continue;
                        }
                        // 设置联系人头像路径
                        chatRoom.setHeadImgUrl("/api/contact/headImg/avatar?userName=" + chatRoom.getChatRoomName());
                    }
                    return page;
                })
                // 返回分页数据
                .map(page -> new PageVO<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords()))
                // 默认值
                .orElse(new PageVO<>(chatRoomDTO.getCurrent(), chatRoomDTO.getPageSize(), 0L, null));
    }

    @Override
    public ChatRoomDetailVO queryChatRoomDetail(String chatRoomName) {
        // 查询群聊详情
        return Opt.ofNullable(chatRoomRepository.queryChatRoomDetail(chatRoomName))
                // 转换参数
                .map(chatRoomMapping::convert)
                // 填充其他参数
                .ifPresent(this::populateChatRoomDetails)
                // 填充群公告
                .ifPresent(this::populateChatRoomInfo)
                // 填充群成员
                .ifPresent(this::populateChatRoomMember)
                // 设置默认值
                .orElse(null);
    }

    @Override
    public String exportChatRoom() {
        // 文件路径
        String filePath = DirUtil.getExportDir("群聊.xlsx");
        // 创建文件
        FileUtil.mkdir(new File(filePath).getParent());
        // 导出
        EasyExcel.write(filePath, ExportChatRoomVO.class)
                .sheet("sheet1")
                .doWrite(() -> {
                    List<ExportChatRoomVO> exportChatRoomVOS = chatRoomRepository.exportChatRoom();
                    // 设置群聊人数
                    for (ExportChatRoomVO exportChatRoomVO : exportChatRoomVOS) {
                        exportChatRoomVO.setMemberCount(handleMembersCount(exportChatRoomVO.getRoomData()));
                    }
                    return exportChatRoomVOS;
                });
        // 返回写入后的文件
        return filePath;
    }

    /**
     * 填充群聊信息
     *
     * @param chatRoomDetailVo 群聊详情VO
     */
    private void populateChatRoomDetails(ChatRoomDetailVO chatRoomDetailVo) {
        // 群标题
        chatRoomDetailVo.setChatRoomTitle(contactRepository.getContactNickname(chatRoomDetailVo.getChatRoomName()));
        // 创建人
        chatRoomDetailVo.setCreateBy(contactRepository.getContactNickname(chatRoomDetailVo.getReserved2()));
        // 群头像
        chatRoomDetailVo.setHeadImgUrl(contactHeadImgUrlRepository.queryHeadImgUrlByUserName(chatRoomDetailVo.getChatRoomName()));
    }

    /**
     * 填充群公告
     *
     * @param chatRoomDetailVo 群聊详情VO
     */
    private void populateChatRoomInfo(ChatRoomDetailVO chatRoomDetailVo) {
        // 查询群公告
        ChatRoomInfo chatRoomInfo = chatRoomInfoRepository.queryChatRoomInfo(chatRoomDetailVo.getChatRoomName());
        // 转换参数
        ChatRoomInfoVO chatRoomInfoVO = chatRoomMapping.convert(chatRoomInfo);
        // 发布时间
        Long announcementPublishTime = chatRoomInfoVO.getAnnouncementPublishTime();
        // 处理发布时间
        if (ObjUtil.isNotEmpty(announcementPublishTime) && announcementPublishTime > 0) {
            chatRoomInfoVO.setStrAnnouncementPublishTime(DateUtil.formatDateTime(new Date(announcementPublishTime * 1000L)));
        }
        // 发布人
        chatRoomInfoVO.setAnnouncementPublisher(contactRepository.getContactNickname(chatRoomInfoVO.getAnnouncementEditor()));
        // 设置群聊公告
        chatRoomDetailVo.setChatRoomInfo(chatRoomInfoVO);
    }

    /**
     * 填充群成员
     *
     * @param chatRoomDetailVo 群聊详情VO
     */
    private void populateChatRoomMember(ChatRoomDetailVO chatRoomDetailVo) {
        try {
            // 使用protobuf解析RoomData字段
            ChatRoomProto.ChatRoom chatRoom = ChatRoomProto.ChatRoom.parseFrom(chatRoomDetailVo.getRoomData());
            // 获得群成员
            List<ChatRoomProto.Member> membersList = chatRoom.getMembersList();
            // 群成员的微信Id
            List<String> memberWxIds = membersList.stream().map(ChatRoomProto.Member::getWxId).collect(Collectors.toList());
            // 群成员头像
            Map<String, String> headImgUrlMap = contactHeadImgUrlRepository.queryHeadImgUrl(memberWxIds);
            // 群成员昵称
            Map<String, String> contactNicknameMap = contactRepository.getContactNickname(memberWxIds);
            // 群成员
            chatRoomDetailVo.setMembers(chatRoomMapping.convert(membersList, headImgUrlMap, contactNicknameMap));
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse RoomData", e);
        }
    }

    /**
     * 获取群聊人数
     *
     * @param roomData 群聊数据
     * @return 群聊人数
     */
    private Integer handleMembersCount(byte[] roomData) {
        // 使用protobuf解析RoomData字段
        ChatRoomProto.ChatRoom chatRoomProto = null;
        try {
            chatRoomProto = ChatRoomProto.ChatRoom.parseFrom(roomData);
        } catch (InvalidProtocolBufferException e) {
            log.error("parse roomData failed", e);
        }
        // 空校验
        if (chatRoomProto == null) {
            return 0;
        }
        return chatRoomProto.getMembersList().size();
    }
}
