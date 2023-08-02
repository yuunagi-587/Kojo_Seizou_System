package com.seizou.kojo.domain.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.seizou.kojo.domain.dto.BelongingShowDto;
import com.seizou.kojo.domain.entity.UserInfoEntity;
import com.seizou.kojo.domain.form.BelongingForm;
import com.seizou.kojo.domain.service.Bfmk07Service;

/**
 * 部署情報 Controller
 * @author Y.Koyanagi
 */
@Controller
@RequestMapping("/b-forme_Kojo")
public class Bfmk07Controller {

	@Autowired
	Bfmk07Service service;

	// ユーザーID仮決め
	private String userId = "itns001";
	// フォーム初期化フラグ
	private boolean formInitializeFlg = true;
	// メッセージ取得用の変数を宣言
	private static final String MESSAGE_NAME = "resources_ja";
	private static ResourceBundle mes = ResourceBundle.getBundle(MESSAGE_NAME);

	/**
	 * 初期画面
	 * @param 部署情報フォーム
	 * @param モデル
	 * @return 画面名
	 */
	@GetMapping("/pc/207")
	public String init(BelongingForm form
			,Model model) {

		// ユーザー情報の取得
		UserInfoEntity userInfo = service.searchUserInfo(userId);

		// 参照権限の確認 true：権限あり false：権限なし 以降同条件
		if(userInfo.isWatchAuthFlg()) {
			// 画面表示の処理を呼び出す
			BelongingShowDto dto = service.pagination(1, "defaultPage");

			// 以下の値をモデルへ格納
			model.addAttribute("belongingList", dto.getBelongingList());	// 部署情報リスト
			model.addAttribute("nowPage", dto.getNowPage());				// 現在のページ
			model.addAttribute("totalPage", dto.getTotalPage());			// ページ総数
			model.addAttribute("totalBelonging", dto.getTotalBelonging());	// 部署総数
		} else {
			// 権限が無い場合、以下の値をモデルへ格納
			model.addAttribute("belongingList", null);						// 部署情報リスト
			model.addAttribute("nowPage", "0");								// 現在のページ
			model.addAttribute("totalPage", "0");							// ページ総数
			model.addAttribute("totalBelonging", "0");						// 部署総数
		}

		// 操作権限の確認
		if(userInfo.isOprAuthFlg()) {
			model.addAttribute("disabledFlg", false);	// フォーム・ボタン有効
		} else {
			model.addAttribute("disabledFlg", true);	// フォーム・ボタン無効
		}

		// 日付の取得とフォーマットの設定
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();

		// 初期化の確認
		if(formInitializeFlg) {
			// フォームを初期化する
			form.setAffilicateId("");					// 所属ID
			form.setAffilicateName("");					// 部署名
			form.setAffilicateNameR("");				// 部署名略称
			form.setApplyStrtDate(sdf.format(date));	// 適用日(FROM)
			form.setApplyFinDate("");					// 適用日(TO)
		}

		// フォームをモデルへ格納
		model.addAttribute("belongingForm", form);

		return "bfmk07View";
	}

	/**
	 * 部署情報登録処理
	 * @param 部署情報フォーム
	 * @param モデル
	 * @return 画面名
	 */
	@PostMapping(value="/pc/207", params="register")
	public String register(BelongingForm form
			,Model model) {

		// 登録処理を実行
		String getMessage = service.registerBelonging(userId, form);

		// プロパティファイルからメッセージを取得
		String processMessage = mes.getString(getMessage);

		// 処理メッセージをモデルへ格納
		model.addAttribute("processMessage", processMessage);

		// 登録成否による処理の分岐
		if(getMessage.equals("msdisu001")) { // ｢msdisu001｣は成功時のメッセージID
			// フォームを初期化する
			formInitializeFlg = true;
			// 画面表示のため、自クラスinitを呼び出す
			init(form, model);
			// 処理メッセージを黒色にする
			model.addAttribute("colorCode", "black");
		} else {
			// フォームを初期化せずに値を保持
			formInitializeFlg = false;
			// 画面表示のため、自クラスinitを呼び出す
			init(form, model);
			// 処理メッセージを赤色にする
			model.addAttribute("colorCode", "red");
		}

		return "bfmk07View";
	}

	/**
	 * 画面クリア処理
	 * @param 部署情報フォーム
	 * @param モデル
	 * @return 画面名
	 */
	@PostMapping(value="/pc/207", params="clear")
	public String clear(BelongingForm form
			,Model model) {

		// フォームを初期化する
		formInitializeFlg = true;

		// 画面表示のため、自クラスinitを呼び出す
		init(form, model);

		return "bfmk07View";
	}

	/**
	 * 部署情報削除処理
	 * @param 部署情報フォーム
	 * @param 所属IDリスト
	 * @param モデル
	 * @return 画面名
	 */
	@PostMapping(value="/pc/207", params="delete")
	public ModelAndView delete(BelongingForm form
			,Model model
			,ModelAndView mav) {

		// 削除する所属IDを格納するリスト
		List<String> affilicateIdList = form.getAffilicateIdList();

		// 削除処理を実行
		String getMessage = service.deleteBelonging(affilicateIdList, userId);

		// プロパティファイルからメッセージを取得
		String processMessage = mes.getString(getMessage);

		// 処理メッセージを格納
		mav.addObject("processMessage", processMessage);

		// 削除成否判定
		if(getMessage.equals("msdisu002")) { // ｢msdisu002｣は成功時のメッセージID
			// 処理メッセージを黒色にする
			model.addAttribute("colorCode", "black");
		} else {
			// 処理メッセージを赤色にする
			model.addAttribute("colorCode", "red");
		}

		// フォームを初期化する
		formInitializeFlg = true;

		// 画面表示のため、自クラスinitを呼び出す
		init(form, model);

		// Viewを設定
		mav.setViewName("bfmk07View");

		return mav;
	}

	/**
	 * メニュー画面へ戻る
	 * @return 画面名
	 */
	@PostMapping(value="/pc/207", params="back")
	public String back() {
		// フォーム初期化フラグの設定
		formInitializeFlg = true;

		return "bfkt02View";
	}

	/**
	 * 次のページへ
	 * @param 部署情報フォーム
	 * @param 現在のページ
	 * @param ページ総数
	 * @param モデル
	 * @return 画面名
	 */
	@PostMapping(value="/pc/207", params="nextPage")
	public String nextPage(@ModelAttribute BelongingForm form
			,@RequestParam("nowPage") String nowPage
			,@RequestParam("totalPage") String totalPage
			,Model model) {

		// ユーザー情報の取得
		UserInfoEntity userInfo = service.searchUserInfo(userId);

		// 参照権限の確認
		if(userInfo.isWatchAuthFlg()) {
			int nowPageInt = Integer.parseInt(nowPage);			// 現在のページ
			int totalPageInt = Integer.parseInt(totalPage);		// ページ総数
			BelongingShowDto dto = new BelongingShowDto();

			// 最後のページかどうか確認
			if(nowPageInt + 1 >= totalPageInt) {
				dto = service.pagination(nowPageInt, "lastPage");	// 最後のページを表示
			} else {
				dto = service.pagination(nowPageInt, "nextPage");	// 次のページを表示
			}

			// 以下の値をモデルへ格納
			model.addAttribute("belongingList", dto.getBelongingList());	// 部署情報リスト
			model.addAttribute("nowPage", dto.getNowPage());				// 現在のページ
			model.addAttribute("totalPage", dto.getTotalPage());			// ページ総数
			model.addAttribute("totalBelonging", dto.getTotalBelonging());	// 部署総数
		} else {
			// 権限が無い場合、以下の値をモデルへ格納
			model.addAttribute("belongingList", null);						// 部署情報リスト
			model.addAttribute("nowPage", "0");								// 現在のページ
			model.addAttribute("totalPage", "0");							// ページ総数
			model.addAttribute("totalBelonging", "0");						// 部署総数
		}

		// 操作権限の確認
		if(userInfo.isOprAuthFlg()) {
			model.addAttribute("disabledFlg", false);	// フォーム・ボタン有効
		} else {
			model.addAttribute("disabledFlg", true);	// フォーム・ボタン無効
		}

		return "bfmk07View";
	}

	/**
	 * 前のページへ
	 * @param 部署情報フォーム
	 * @param 現在のページ
	 * @param モデル
	 * @return 画面名
	 */
	@PostMapping(value="/pc/207", params="backPage")
	public String backPage(@ModelAttribute BelongingForm form
			,@RequestParam("nowPage") String nowPage
			,Model model) {

		// ユーザー情報の取得
		UserInfoEntity userInfo = service.searchUserInfo(userId);

		// 参照権限の確認
		if(userInfo.isWatchAuthFlg()) {
			int nowPageInt = Integer.parseInt(nowPage);
			BelongingShowDto dto = new BelongingShowDto();

			// 最初のページかどうか確認
			if(nowPageInt - 1 <= 1) {
				dto = service.pagination(nowPageInt, "defaultPage");	// 最初のページを表示
			} else {
				dto = service.pagination(nowPageInt, "backPage");		// 前のページを表示
			}

			// 以下の値をモデルへ格納
			model.addAttribute("belongingList", dto.getBelongingList());	// 部署情報リスト
			model.addAttribute("nowPage", dto.getNowPage());				// 現在のページ
			model.addAttribute("totalPage", dto.getTotalPage());			// ページ総数
			model.addAttribute("totalBelonging", dto.getTotalBelonging());	// 部署総数
		} else {
			// 権限が無い場合、以下の値をモデルへ格納
			model.addAttribute("belongingList", null);						// 部署情報リスト
			model.addAttribute("nowPage", "0");								// 現在のページ
			model.addAttribute("totalPage", "0");							// ページ総数
			model.addAttribute("totalBelonging", "0");						// 部署総数
		}

		// 操作権限の確認
		if(userInfo.isOprAuthFlg()) {
			model.addAttribute("disabledFlg", false);	// フォーム・ボタン有効
		} else {
			model.addAttribute("disabledFlg", true);	// フォーム・ボタン無効
		}

		return "bfmk07View";
	}

	/**
	 * 最初のページへ
	 * @param 部署情報フォーム
	 * @param 現在のページ
	 * @param モデル
	 * @return 画面名
	 */
	@PostMapping(value="/pc/207", params="firstPage")
	public String firstPage(@ModelAttribute BelongingForm form
			,@RequestParam("nowPage") String nowPage
			,Model model) {

		// ユーザー情報の取得
		UserInfoEntity userInfo = service.searchUserInfo(userId);

		// 参照権限の確認
		if(userInfo.isWatchAuthFlg()) {
			int nowPageInt = Integer.parseInt(nowPage);

			// 最初のページを表示
			BelongingShowDto dto = service.pagination(nowPageInt, "defaultPage");

			// 以下の値をモデルへ格納
			model.addAttribute("belongingList", dto.getBelongingList());	// 部署情報リスト
			model.addAttribute("nowPage", dto.getNowPage());				// 現在のページ
			model.addAttribute("totalPage", dto.getTotalPage());			// ページ総数
			model.addAttribute("totalBelonging", dto.getTotalBelonging());	// 部署総数
		} else {
			// 権限が無い場合、以下の値をモデルへ格納
			model.addAttribute("belongingList", null);						// 部署情報リスト
			model.addAttribute("nowPage", "0");								// 現在のページ
			model.addAttribute("totalPage", "0");							// ページ総数
			model.addAttribute("totalBelonging", "0");						// 部署総数
		}

		// 操作権限の確認
		if(userInfo.isOprAuthFlg()) {
			model.addAttribute("disabledFlg", false);	// フォーム・ボタン有効
		} else {
			model.addAttribute("disabledFlg", true);	// フォーム・ボタン無効
		}

		return "bfmk07View";
	}

	/**
	 * 最後のページへ
	 * @param 部署情報フォーム
	 * @param 現在のページ
	 * @param モデル
	 * @return 画面名
	 */
	@PostMapping(value="/pc/207", params="lastPage")
	public String lastPage(@ModelAttribute BelongingForm form
			,@RequestParam("nowPage") String nowPage
			,Model model) {

		// ユーザー情報の取得
		UserInfoEntity userInfo = service.searchUserInfo(userId);

		// 参照権限の確認
		if(userInfo.isWatchAuthFlg()) {
			int nowPageInt = Integer.parseInt(nowPage);

			// 最後のページを表示
			BelongingShowDto dto = service.pagination(nowPageInt, "lastPage");

			// 以下の値をモデルへ格納
			model.addAttribute("belongingList", dto.getBelongingList());	// 部署情報リスト
			model.addAttribute("nowPage", dto.getNowPage());				// 現在のページ
			model.addAttribute("totalPage", dto.getTotalPage());			// ページ総数
			model.addAttribute("totalBelonging", dto.getTotalBelonging());	// 部署総数
		} else {
			// 権限が無い場合、以下の値をモデルへ格納
			model.addAttribute("belongingList", null);						// 部署情報リスト
			model.addAttribute("nowPage", "0");								// 現在のページ
			model.addAttribute("totalPage", "0");							// ページ総数
			model.addAttribute("totalBelonging", "0");						// 部署総数
		}

		// 操作権限の確認
		if(userInfo.isOprAuthFlg()) {
			model.addAttribute("disabledFlg", false);	// フォーム・ボタン有効
		} else {
			model.addAttribute("disabledFlg", true);	// フォーム・ボタン無効
		}

		return "bfmk07View";
	}

}
