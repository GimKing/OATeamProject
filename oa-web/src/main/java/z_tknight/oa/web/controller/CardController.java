package z_tknight.oa.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import z_tknight.oa.commons.util.CaseUtil;
import z_tknight.oa.commons.util.ResponeResult;
import z_tknight.oa.service.baseService.CardService;
import z_tknight.oa.web.annotation.LogInfo;

@Controller
@RequestMapping("/card")
public class CardController {

	@Autowired
	private CardService cardService;
	
	@LogInfo("新增卡片")
	@RequestMapping(value="/addCard", method={RequestMethod.POST})
	@ResponseBody
	public ResponeResult addCard(HttpServletRequest request, String cardName, Integer listNo){
		if(cardName ==null || cardName.equals("")) {
			return ResponeResult.build(400, "卡片名称不能为空");
		}
		Integer userNo = CaseUtil.caseInt(request.getSession().getAttribute("userNo"), null);
		
		return cardService.addCard(cardName, userNo, listNo);
	}
	
	@LogInfo("删除卡片")
	@RequestMapping(value="/deleteCard", method={RequestMethod.POST})
	@ResponseBody
	public ResponeResult deleteCard(HttpServletRequest request, Integer cardNo){
		
		Integer userNo = CaseUtil.caseInt(request.getSession().getAttribute("userNo"), null);
		
		return cardService.deleteCard(cardNo, userNo);
	}
	
	@LogInfo("修改卡片名称")
	@RequestMapping(value="/updateCardName", method={RequestMethod.POST})
	@ResponseBody
	public ResponeResult updateCardName(HttpServletRequest request, Integer cardNo, String newCardName) {
		
		if(newCardName ==null || newCardName.equals("")) {
			return ResponeResult.build(400, "卡片名称不能为空");
		}
		
		Integer userNo = CaseUtil.caseInt(request.getSession().getAttribute("userNo"), null);
		
		return cardService.updateCardName(userNo, newCardName, cardNo);
	}
	
	@LogInfo("修改卡片顺序")
	@RequestMapping(value="/updateCardOrder", method={RequestMethod.POST})
	@ResponseBody
	public ResponeResult updateCardOrder(HttpServletRequest request, Integer listNoFrom, Integer listNoTo, String cardOrderFrom, 
			String cardOrderTo, String newcardOrderFrom, String newcardOrderTo) {
		
		Integer userNo = CaseUtil.caseInt(request.getSession().getAttribute("userNo"), null);
		
		return cardService.updateCardOrder(listNoFrom, listNoTo, cardOrderFrom, cardOrderTo, newcardOrderFrom, newcardOrderTo, userNo);
	}
	
}
