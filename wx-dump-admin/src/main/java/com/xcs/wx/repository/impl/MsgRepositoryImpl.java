package com.xcs.wx.repository.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcs.wx.constant.DataSourceType;
import com.xcs.wx.domain.Msg;
import com.xcs.wx.domain.dto.ChatRoomDTO;
import com.xcs.wx.domain.vo.CountRecentMsgsVO;
import com.xcs.wx.domain.vo.ExportChatRoomVO;
import com.xcs.wx.domain.vo.MsgTypeDistributionVO;
import com.xcs.wx.domain.vo.TopContactsVO;
import com.xcs.wx.mapper.MsgMapper;
import com.xcs.wx.repository.ChatRoomRepository;
import com.xcs.wx.repository.MsgRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import cn.hutool.core.date.DateUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息 Repository 实现类
 *
 * @author xcs
 * @date 2023年12月25日 15时29分
 **/
@Slf4j
@Repository
@RequiredArgsConstructor
public class MsgRepositoryImpl extends ServiceImpl<MsgMapper, Msg> implements MsgRepository {

    
	@Override
	public List<Msg> queryMsgByTalker(String talker, Long nextSequence) {
		List<Msg> msgList = new ArrayList<>();
		List<String> msgDbList = DataSourceType.getMsgDb().stream().sorted(Comparator.reverseOrder())
				.collect(Collectors.toList());
		int offset = 20;
		for (String poolName : msgDbList) {
			if (offset <= 0)
				break;
			DynamicDataSourceContextHolder.push(poolName);
			List<Msg> queryResultList = super.list(
					Wrappers.<Msg>lambdaQuery().eq(Msg::getStrTalker, talker).orderByDesc(Msg::getSequence)
							.lt((nextSequence != null && nextSequence > 0), Msg::getSequence, nextSequence)
							.last("limit " + offset));
			DynamicDataSourceContextHolder.clear();
			offset -= queryResultList.size();
			msgList.addAll(queryResultList);
		}
		return msgList;
	}

	@Override
    public List<Msg> exportMsg(String talker,ChatRoomDTO chatRoomDTO) {
        List<Msg> msgList = new ArrayList<>();
        List<String> msgDbList = DataSourceType.getMsgDb();
        //String dateStr = DateUtil.today();  /*KAKU　ADD 2024/10/17*/
       // String dateStr = chatRoomDTO.getChatRoomTitle(); //"2024-10-25 01:06:01";  /*KAKU　ADD 2024/10/17*/
        String sakanoboriHours = chatRoomDTO.getChatRoomTitle(); //"2024-10-25 01:06:01";  /*KAKU　ADD 2024/10/17*/
        String dateStr = ""; //"2024-10-25 01:06:01";  /*KAKU　ADD 2024/10/17*/
        if (sakanoboriHours == null || sakanoboriHours.isEmpty())  {
        	dateStr=DateUtil.today();
        }else {
        	//dateStr= DateUtil.offsetHour( new Date(), Integer.parseInt(sakanoboriHours)).toDateStr();
        	dateStr=sakanoboriHours;
        }
        Date dateFrom = DateUtil.parse(dateStr);   /*KAKU　ADD 2024/10/17*/
        long dateFromTime = dateFrom.getTime()/1000; /*KAKU　ADD 2024/10/17*/
        List <String> TalkerList = Arrays.asList( 
        		  "48732360290@chatroom"
        		, "26083504311@chatroom"
        		, "26389708426@chatroom"
        		, "27732418078@chatroom"
        		, "20808083236@chatroom"
        		, "26893706352@chatroom"
        		, "18667772672@chatroom"
        		, "21788841141@chatroom"
        		, "25379739629@chatroom"
        		, "26622704985@chatroom"
        		, "25856705924@chatroom"
        		, "44949807927@chatroom"
        		, "27546508078@chatroom"
        		, "6033254884@chatroom"
        		, "27752908121@chatroom"
        		, "26724907956@chatroom"
        		, "6035042924@chatroom"
        		, "27968415000@chatroom"
        		, "11888020036@chatroom"
        		, "19383676230@chatroom"
        		, "27931308144@chatroom"
        		, "211752786@chatroom"
        		, "17714836956@chatroom"
        		, "26082816565@chatroom"
        		, "1471975489@chatroom"
        		, "27629110327@chatroom"
        		, "5671177664@chatroom"
        		, "22124035058@chatroom"
        		, "26759106129@chatroom"
        		, "19514240533@chatroom"
        		, "43735298482@chatroom"
        		, "18041878078@chatroom"
        		, "580715194@chatroom"
        		, "27771504983@chatroom"
        		, "27982817821@chatroom"
        		, "19083906009@chatroom"
        		, "26828106144@chatroom"
        		, "27027216055@chatroom"
        		, "27805617893@chatroom"
        		, "26922103833@chatroom"
        		, "27010311206@chatroom"
        		, "23296252992@chatroom"
        		, "5974236249@chatroom"
        		, "26038708243@chatroom"
        		, "26191422868@chatroom"
        		, "19210980955@chatroom"
        		, "6154077261@chatroom"
        		, "541781087@chatroom"
        		, "27854017316@chatroom"
        		, "26588711599@chatroom"
        		, "25562101004@chatroom"
        		, "26988712321@chatroom"
        		, "26905911407@chatroom"
        		, "27389903786@chatroom"
        		, "27810303867@chatroom"
        		, "6168270051@chatroom"
        		, "6234316610@chatroom"
        		, "285694786@chatroom"
        		, "26361818228@chatroom"
        		, "47732070626@chatroom"
        		, "5981188797@chatroom"
        		, "6170993050@chatroom"
        		, "6013171785@chatroom"
        		, "24591528746@chatroom");  /*KAKU　ADD 2024/10/25*/
        

        for (String poolName : msgDbList) {
            DynamicDataSourceContextHolder.push(poolName);
            List<Msg> queryResultList = super.list(Wrappers.<Msg>lambdaQuery()
            		//.eq(Msg::getStrTalker, talker)         /*KAKU　DEL 2024/10/17*/
            		.ge(Msg::getCreateTime, dateFromTime)     /*KAKU　ADD 2024/10/17*/
            		.in(Msg::getStrTalker,TalkerList)
                    .orderByDesc(Msg::getSequence));
            
            List<Msg> distinctList = queryResultList.stream()
            	    .collect(Collectors.toMap(Msg::getStrContent, msg -> msg, (existing, replacement) -> existing))
            	    .values()
            	    .stream()
            	    .collect(Collectors.toList());  /*KAKU　ADD 2024/10/25
         
            DynamicDataSourceContextHolder.clear();
           // msgList.addAll(queryResultList);     /*KAKU　DEL 2024/10/25*/
            msgList.addAll(distinctList);     /*KAKU　ADD 2024/10/25*/
        }
        return msgList;
        //.eq(Msg::getStrTalker, talker)
        /*KAKU　ADD */
        //  /* .eq(Msg::getCreateTime, "2024-10-16 09:41:40")  */ 1722579041 , 1721986577
    }

	@Override
	public List<MsgTypeDistributionVO> msgTypeDistribution() {
		Optional<String> poolNameOptional = DataSourceType.getMsgDb().stream().max(Comparator.naturalOrder());
		if (poolNameOptional.isPresent()) {
			DynamicDataSourceContextHolder.push(poolNameOptional.get());
			List<MsgTypeDistributionVO> currentMsgsList = super.getBaseMapper().msgTypeDistribution();
			DynamicDataSourceContextHolder.clear();
			return currentMsgsList;
		}
		return Collections.emptyList();
	}

	@Override
	public List<CountRecentMsgsVO> countRecentMsgs() {
		Optional<String> poolNameOptional = DataSourceType.getMsgDb().stream().max(Comparator.naturalOrder());
		if (poolNameOptional.isPresent()) {
			DynamicDataSourceContextHolder.push(poolNameOptional.get());
			List<CountRecentMsgsVO> currentMsgsList = super.getBaseMapper().countRecentMsgs();
			DynamicDataSourceContextHolder.clear();
			return currentMsgsList.stream().sorted(Comparator.comparing(CountRecentMsgsVO::getType).reversed())
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public List<TopContactsVO> topContacts() {
		Optional<String> poolNameOptional = DataSourceType.getMsgDb().stream().max(Comparator.naturalOrder());
		if (poolNameOptional.isPresent()) {
			DynamicDataSourceContextHolder.push(poolNameOptional.get());
			List<TopContactsVO> currentContactsList = super.getBaseMapper().topContacts();
			DynamicDataSourceContextHolder.clear();

			return currentContactsList.stream().sorted(Comparator.comparing(TopContactsVO::getTotal).reversed())
					.limit(10).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public int countSent() {
		Optional<String> poolNameOptional = DataSourceType.getMsgDb().stream().max(Comparator.naturalOrder());
		if (poolNameOptional.isPresent()) {
			DynamicDataSourceContextHolder.push(poolNameOptional.get());
			int count = getBaseMapper().countSent();
			DynamicDataSourceContextHolder.clear();
			return count;
		}
		return 0;
	}

	@Override
	public int countReceived() {
		Optional<String> poolNameOptional = DataSourceType.getMsgDb().stream().max(Comparator.naturalOrder());
		if (poolNameOptional.isPresent()) {
			DynamicDataSourceContextHolder.push(poolNameOptional.get());
			int count = getBaseMapper().countReceived();
			DynamicDataSourceContextHolder.clear();
			return count;
		}
		return 0;
	}
}
