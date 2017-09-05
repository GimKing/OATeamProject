package z_tknight.oa.service.baseServiceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import z_tknight.oa.commons.util.ExceptionUtil;
import z_tknight.oa.commons.util.ResponeResult;
import z_tknight.oa.model.entity.TBoard;
import z_tknight.oa.model.entity.TBoardSpace;
import z_tknight.oa.model.entity.TBoardUser;
import z_tknight.oa.model.entity.TBoardUserExample;
import z_tknight.oa.model.entity.TList;
import z_tknight.oa.model.entity.TBoardUserExample.Criteria;
import z_tknight.oa.model.entity.TCard;
import z_tknight.oa.persist.mapper.TBoardMapper;
import z_tknight.oa.persist.mapper.TBoardSpaceMapper;
import z_tknight.oa.persist.mapper.TBoardUserMapper;
import z_tknight.oa.persist.mapper.TCardMapper;
import z_tknight.oa.persist.mapper.TListMapper;
import z_tknight.oa.service.baseService.CardService;

/**
 * 
* @ClassName: CardServiceImpl 
* @Description: 对卡片的各种操作
* @author：XHX
* @date 2017年9月4日 下午5:07:09 
*
 */
@Service
public class CardServiceImpl implements CardService {

	@Autowired
	private TCardMapper cardMapper;
	@Autowired
	private TBoardMapper boardMapper;
	@Autowired
	private TListMapper listMapper;
	@Autowired
	private TBoardUserMapper boardUserMapper;
	@Autowired
	private TBoardSpaceMapper boardSpaceMapper;
	
	/**
	 * @Description: 判断列表编号是否存在
	 * 判断用户是否是面板的所有人，或成员，或看板空间的所有人
	 * 新建卡片
	 * 修改列表的卡片顺序
	 * @author：XHX
	 */
	@Override
	public ResponeResult addCard(String cardName, Integer userNo, Integer listNo) {

		try{
			
			TList list = listMapper.selectByPrimaryKey(listNo);
			//判断列表编号是否存在
			if(list == null) {
				return ResponeResult.build(400, "数据不合法");
			}
			TBoard board = boardMapper.selectByPrimaryKey(list.getBoardNo());
			//判断用户是否是面板的所有人
			if(board.getUserNo() != userNo) {
				TBoardUserExample boardUserExample = new TBoardUserExample();
				Criteria criteria = boardUserExample.createCriteria();
				criteria.andUserNoEqualTo(userNo);
				criteria.andBoardNoEqualTo(board.getBoardNo());
				List<TBoardUser> boardUserList = boardUserMapper.selectByExample(boardUserExample);
				//判断用户是否是面板成员
				TBoardSpace boardSpace = boardSpaceMapper.selectByPrimaryKey(board.getBoardSpaceNo());
				if(boardUserList == null || boardUserList.size() == 0) {
					//判断用户是否是面板空间的所有人
					if(boardSpace.getUserNo() != userNo) {
						return ResponeResult.build(400, "没权限创建卡片");
					}
				}
			}
			//新增列表
			TCard card = new TCard();
			card.setBoardNo(board.getBoardNo());
			card.setBoardSpaceNo(board.getBoardSpaceNo());
			card.setCardTitle(cardName);
			card.setIsDelete(false);
			card.setListName(list.getListName());
			card.setListNo(listNo);
			cardMapper.insertSelective(card);
			//修改列表的列表顺序
			if(list.getCardOrder() == null || list.getCardOrder().equals("")) {
				list.setCardOrder(card.getCardNo().toString());
			}
			else {
				list.setCardOrder(list.getCardOrder() + "," + card.getCardNo().toString());
			}
			listMapper.updateByPrimaryKeySelective(list);
			return ResponeResult.ok(card);
			
		}catch(Exception e) {
			return ResponeResult.build(500, ExceptionUtil.getStackTrace(e));
		}
	}

	/**
	 * @Description: 判断用户是否看板的所有人或成员，或看板空间的所有人
	 * 根据卡片编号删除列表
	 * 修改列表的卡片顺序
	 * @author：XHX
	 */
	@Override
	public ResponeResult deleteCard(Integer cardNo, Integer userNo) {
		
		try {
			
			TCard card = cardMapper.selectByPrimaryKey(cardNo);
			
			if(card == null) {
				return ResponeResult.build(400, "数据不合法");
			}
			
			TBoard board = boardMapper.selectByPrimaryKey(card.getBoardNo());
			//判断用户是否是面板的所有人
			if(board.getUserNo() != userNo) {
				TBoardSpace boardSpace = boardSpaceMapper.selectByPrimaryKey(board.getBoardSpaceNo());
				TBoardUserExample boardUserExample = new TBoardUserExample();
				Criteria criteria = boardUserExample.createCriteria();
				criteria.andUserNoEqualTo(userNo);
				criteria.andBoardNoEqualTo(board.getBoardNo());
				List<TBoardUser> boardUserList = boardUserMapper.selectByExample(boardUserExample);
				//判断用户是否是面板成员
				if(boardUserList == null || boardUserList.size() == 0) {
					//判断用户是否是面板空间的所有人
					if(boardSpace.getUserNo() != userNo) {
						return ResponeResult.build(400, "没权限修改列表名称");
					}
				}
			}
			TList list = listMapper.selectByPrimaryKey(card.getListNo());
			//删除列表
			cardMapper.deleteByPrimaryKey(cardNo);
			//修改列表的顺序
			String cardOrder = list.getCardOrder();
			String [] order= cardOrder.split(",");
			//如果看板中只有一个列表，直接设置为null
			if(order.length<=1) {
				list.setCardOrder(null);
			}
			//如果看板中有多个列表，则找出要删除的哪个列表，用后面的编号覆盖掉
			else {
				for(int i=0;i<order.length;i++) {
					if(order[i].equals(cardNo.toString())) {
						for(int j=i+1;j<order.length;j++) {
							order[j-1] = order[j];
						}
					}
				}
				String a = order[0];
				for(int i=1;i<order.length-1;i++) {
					a = a + "," + order[i];
				}
				list.setCardOrder(a);
			}
			listMapper.updateByPrimaryKey(list);
			
			return ResponeResult.ok("删除列表成功");
		}catch(Exception e) {
			return ResponeResult.build(500, ExceptionUtil.getStackTrace(e));
		}
	}

	/**
	 * @Description: 判断用户是否看板的所有人或成员，或看板空间的所有人
	 * 根据卡片编号修改卡片名称
	 * @author：XHX
	 */
	@Override
	public ResponeResult updateCardName(Integer userNo, String newCardName, Integer cardNo) {

		try {
			
			TCard card = cardMapper.selectByPrimaryKey(cardNo);
			
			TBoard board = boardMapper.selectByPrimaryKey(card.getBoardNo());
			//判断用户是否是面板的所有人
			if(board.getUserNo() != userNo) {
				TBoardSpace boardSpace = boardSpaceMapper.selectByPrimaryKey(board.getBoardSpaceNo());
				TBoardUserExample boardUserExample = new TBoardUserExample();
				Criteria criteria = boardUserExample.createCriteria();
				criteria.andUserNoEqualTo(userNo);
				criteria.andBoardNoEqualTo(board.getBoardNo());
				List<TBoardUser> boardUserList = boardUserMapper.selectByExample(boardUserExample);
				//判断用户是否是面板成员
				if(boardUserList == null || boardUserList.size() == 0) {
					//判断用户是否是面板空间的所有人
					if(boardSpace.getUserNo() != userNo) {
						return ResponeResult.build(400, "没权限修改卡片名称");
					}
				}
			}
			//修改列表名称
			card.setCardTitle(newCardName);
			cardMapper.updateByPrimaryKeySelective(card);
			return ResponeResult.ok("修改卡片名称成功");
		}catch(Exception e) {
			return ResponeResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		
	}

	
	@Override
	public ResponeResult updateCardOrder(Integer listNoFrom, Integer listNoTo, String cardOrderFrom, String cardOrderTo,
			String newcardOrderFrom, String newcardOrderTo, Integer userNo) {

		try {
			TList listFrom = listMapper.selectByPrimaryKey(listNoFrom);
			TList listTo = listMapper.selectByPrimaryKey(listNoTo);
			//判断列表是否已经被删除
			if(listFrom == null || listTo == null) {
				return ResponeResult.build(400, "数据不合法");
			}
			//判断两个列表是否在同一个面板里面
			if(listFrom.getBoardNo() != listTo.getBoardNo()) {
				return ResponeResult.build(400, "数据不合法");
			}
			//判断用户是否是面板的所有人
			TBoard board = boardMapper.selectByPrimaryKey(listFrom.getBoardNo());
			if(board.getUserNo() != userNo) {
				TBoardUserExample boardUserExample = new TBoardUserExample();
				Criteria criteria = boardUserExample.createCriteria();
				criteria.andUserNoEqualTo(userNo);
				criteria.andBoardNoEqualTo(board.getBoardNo());
				List<TBoardUser> boardUserList = boardUserMapper.selectByExample(boardUserExample);
				//判断用户是否是面板成员
				if(boardUserList == null || boardUserList.size() == 0) {
					TBoardSpace boardSpace = boardSpaceMapper.selectByPrimaryKey(board.getBoardSpaceNo());
					//判断用户是否是面板空间的所有人
					if(boardSpace.getUserNo() != userNo) {
						return ResponeResult.build(400, "没权限修改卡片顺序");
					}
				}
			}
			//判断原面板顺序是否正确
			if(!listFrom.getCardOrder().equals(cardOrderFrom) || !listTo.getCardOrder().equals(cardOrderTo)) {
				return ResponeResult.build(400, "卡片顺序出错");
			}else {
				listFrom.setCardOrder(newcardOrderFrom);
				listTo.setCardOrder(newcardOrderTo);
				listMapper.updateByPrimaryKeySelective(listFrom);
				listMapper.updateByPrimaryKeySelective(listTo);
				return ResponeResult.ok("修改卡片顺序成功");
			}
		}catch(Exception e) {
			return ResponeResult.build(500, ExceptionUtil.getStackTrace(e));
		}
	}

}